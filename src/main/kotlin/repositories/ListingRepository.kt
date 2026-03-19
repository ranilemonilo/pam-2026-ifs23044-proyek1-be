package org.delcom.repositories

import org.delcom.dao.ListingDAO
import org.delcom.dao.PetDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Listing
import org.delcom.helpers.listingDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ListingTable
import org.delcom.tables.PetTable
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.math.BigDecimal
import java.util.UUID

class ListingRepository(private val baseUrl: String) : IListingRepository {

    override suspend fun getAll(
        search: String,
        species: String,
        minPrice: Double?,
        maxPrice: Double?,
        status: String,
        page: Int,
        size: Int,
    ): List<Listing> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        if (status.isNotBlank())
            conditions.add(ListingTable.status eq status)
        if (search.isNotBlank())
            conditions.add(ListingTable.title.lowerCase() like "%${search.lowercase()}%")
        if (minPrice != null)
            conditions.add(ListingTable.price greaterEq BigDecimal(minPrice))
        if (maxPrice != null)
            conditions.add(ListingTable.price lessEq BigDecimal(maxPrice))
        if (species.isNotBlank()) {
            val petIds = PetDAO.find { PetTable.species eq species }.map { it.id.value }
            if (petIds.isNotEmpty())
                conditions.add(ListingTable.petId inList petIds)
        }

        val query = if (conditions.isEmpty()) {
            ListingDAO.all()
        } else {
            ListingDAO.find { conditions.reduce { acc, op -> acc and op } }
        }

        query
            .orderBy(ListingTable.createdAt to SortOrder.DESC)
            .limit(size)
            .offset(((page - 1) * size).toLong())
            .map { dao ->
                val petName = PetDAO
                    .find { PetTable.id eq dao.petId }
                    .firstOrNull()?.name ?: ""
                val sellerName = UserDAO
                    .find { UserTable.id eq dao.sellerId }
                    .firstOrNull()?.name ?: ""
                listingDAOToModel(dao, baseUrl, petName, sellerName)
            }
    }

    override suspend fun getById(listingId: String): Listing? = suspendTransaction {
        ListingDAO
            .find { ListingTable.id eq UUID.fromString(listingId) }
            .limit(1)
            .firstOrNull()
            ?.let { dao ->
                val petName = PetDAO
                    .find { PetTable.id eq dao.petId }
                    .firstOrNull()?.name ?: ""
                val sellerName = UserDAO
                    .find { UserTable.id eq dao.sellerId }
                    .firstOrNull()?.name ?: ""
                listingDAOToModel(dao, baseUrl, petName, sellerName)
            }
    }

    override suspend fun create(listing: Listing): String = suspendTransaction {
        ListingDAO.new {
            petId       = UUID.fromString(listing.petId)
            sellerId    = UUID.fromString(listing.sellerId)
            title       = listing.title
            description = listing.description
            price       = BigDecimal(listing.price)
            photo       = listing.photo
            status      = listing.status
            createdAt   = listing.createdAt
            updatedAt   = listing.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(sellerId: String, listingId: String, newListing: Listing): Boolean = suspendTransaction {
        val dao = ListingDAO
            .find {
                (ListingTable.id eq UUID.fromString(listingId)) and
                        (ListingTable.sellerId eq UUID.fromString(sellerId))
            }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.title       = newListing.title
            dao.description = newListing.description
            dao.price       = BigDecimal(newListing.price)
            dao.updatedAt   = newListing.updatedAt
            true
        } else false
    }

    override suspend fun updatePhoto(sellerId: String, listingId: String, photo: String): Boolean = suspendTransaction {
        val dao = ListingDAO
            .find {
                (ListingTable.id eq UUID.fromString(listingId)) and
                        (ListingTable.sellerId eq UUID.fromString(sellerId))
            }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.photo     = photo
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }

    override suspend fun updateStatus(listingId: String, status: String): Boolean = suspendTransaction {
        val dao = ListingDAO
            .find { ListingTable.id eq UUID.fromString(listingId) }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.status    = status
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }

    override suspend fun delete(sellerId: String, listingId: String): Boolean = suspendTransaction {
        val rows = ListingTable.deleteWhere {
            (ListingTable.id eq UUID.fromString(listingId)) and
                    (ListingTable.sellerId eq UUID.fromString(sellerId))
        }
        rows >= 1
    }
}
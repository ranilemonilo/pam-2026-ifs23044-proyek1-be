package org.delcom.repositories

import org.delcom.dao.AdoptionDAO
import org.delcom.dao.PetDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.AdoptionRequest
import org.delcom.helpers.adoptionDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.AdoptionTable
import org.delcom.tables.PetTable
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import java.util.UUID

class AdoptionRepository : IAdoptionRepository {

    override suspend fun getAll(
        userId: String,
        status: String,
        page: Int,
        size: Int,
    ): List<AdoptionRequest> = suspendTransaction {
        val uid = UUID.fromString(userId)

        val ownedPetIds = PetDAO
            .find { PetTable.ownerId eq uid }
            .map { it.id.value }

        val baseCondition: Op<Boolean> = if (ownedPetIds.isEmpty()) {
            AdoptionTable.requesterId eq uid
        } else {
            (AdoptionTable.requesterId eq uid) or (AdoptionTable.petId inList ownedPetIds)
        }

        val finalCondition: Op<Boolean> = if (status.isNotBlank()) {
            baseCondition and (AdoptionTable.status eq status)
        } else {
            baseCondition
        }

        AdoptionDAO
            .find { finalCondition }
            .orderBy(AdoptionTable.createdAt to SortOrder.DESC)
            .limit(size)
            .offset(((page - 1) * size).toLong())
            .map { dao ->
                val petName = PetDAO
                    .find { PetTable.id eq dao.petId }
                    .firstOrNull()?.name ?: ""
                val requesterName = UserDAO
                    .find { UserTable.id eq dao.requesterId }
                    .firstOrNull()?.name ?: ""
                adoptionDAOToModel(dao, petName, requesterName)
            }
    }

    override suspend fun getById(adoptionId: String): AdoptionRequest? = suspendTransaction {
        AdoptionDAO
            .find { AdoptionTable.id eq UUID.fromString(adoptionId) }
            .limit(1)
            .firstOrNull()
            ?.let { dao ->
                val petName = PetDAO
                    .find { PetTable.id eq dao.petId }
                    .firstOrNull()?.name ?: ""
                val requesterName = UserDAO
                    .find { UserTable.id eq dao.requesterId }
                    .firstOrNull()?.name ?: ""
                adoptionDAOToModel(dao, petName, requesterName)
            }
    }

    override suspend fun hasPending(requesterId: String, petId: String): Boolean = suspendTransaction {
        AdoptionDAO
            .find {
                (AdoptionTable.requesterId eq UUID.fromString(requesterId)) and
                        (AdoptionTable.petId eq UUID.fromString(petId)) and
                        (AdoptionTable.status eq "pending")
            }
            .limit(1)
            .firstOrNull() != null
    }

    override suspend fun create(adoption: AdoptionRequest): String = suspendTransaction {
        AdoptionDAO.new {
            petId       = UUID.fromString(adoption.petId)
            requesterId = UUID.fromString(adoption.requesterId)
            message     = adoption.message
            status      = adoption.status
            createdAt   = adoption.createdAt
            updatedAt   = adoption.updatedAt
        }.id.value.toString()
    }

    override suspend fun updateStatus(adoptionId: String, status: String): Boolean = suspendTransaction {
        val dao = AdoptionDAO
            .find { AdoptionTable.id eq UUID.fromString(adoptionId) }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.status    = status
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }

    override suspend fun delete(requesterId: String, adoptionId: String): Boolean = suspendTransaction {
        val rows = AdoptionTable.deleteWhere {
            (AdoptionTable.id eq UUID.fromString(adoptionId)) and
                    (AdoptionTable.requesterId eq UUID.fromString(requesterId))
        }
        rows >= 1
    }
}
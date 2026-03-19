package org.delcom.repositories

import org.delcom.dao.PetDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Pet
import org.delcom.helpers.petDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.PetTable
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class PetRepository(private val baseUrl: String) : IPetRepository {

    override suspend fun getAll(
        search: String,
        species: String,
        gender: String,
        status: String,
        forAdoption: Boolean?,
        forSale: Boolean?,
        page: Int,
        size: Int,
    ): List<Pet> = suspendTransaction {
        val conditions = mutableListOf<Op<Boolean>>()

        if (search.isNotBlank())
            conditions.add(PetTable.name.lowerCase() like "%${search.lowercase()}%")
        if (species.isNotBlank()) conditions.add(PetTable.species eq species)
        if (gender.isNotBlank())  conditions.add(PetTable.gender eq gender)
        if (status.isNotBlank())  conditions.add(PetTable.status eq status)
        if (forAdoption != null)  conditions.add(PetTable.isForAdoption eq forAdoption)
        if (forSale != null)      conditions.add(PetTable.isForSale eq forSale)

        val query = if (conditions.isEmpty()) {
            PetDAO.all()
        } else {
            PetDAO.find { conditions.reduce { acc, op -> acc and op } }
        }

        query
            .orderBy(PetTable.createdAt to SortOrder.DESC)
            .limit(size)
            .offset(((page - 1) * size).toLong())
            .map { dao ->
                val ownerName = UserDAO
                    .find { UserTable.id eq dao.ownerId }
                    .firstOrNull()?.name ?: ""
                petDAOToModel(dao, baseUrl, ownerName)
            }
    }

    override suspend fun getById(petId: String): Pet? = suspendTransaction {
        PetDAO
            .find { PetTable.id eq UUID.fromString(petId) }
            .limit(1)
            .firstOrNull()
            ?.let { dao ->
                val ownerName = UserDAO
                    .find { UserTable.id eq dao.ownerId }
                    .firstOrNull()?.name ?: ""
                petDAOToModel(dao, baseUrl, ownerName)
            }
    }

    override suspend fun create(pet: Pet): String = suspendTransaction {
        PetDAO.new {
            ownerId       = UUID.fromString(pet.ownerId)
            name          = pet.name
            species       = pet.species
            breed         = pet.breed
            age           = pet.age
            gender        = pet.gender
            description   = pet.description
            photo         = pet.photo
            isForAdoption = pet.isForAdoption
            isForSale     = pet.isForSale
            createdAt     = pet.createdAt
            updatedAt     = pet.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(ownerId: String, petId: String, newPet: Pet): Boolean = suspendTransaction {
        val dao = PetDAO
            .find {
                (PetTable.id eq UUID.fromString(petId)) and
                        (PetTable.ownerId eq UUID.fromString(ownerId))
            }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.name          = newPet.name
            dao.species       = newPet.species
            dao.breed         = newPet.breed
            dao.age           = newPet.age
            dao.gender        = newPet.gender
            dao.description   = newPet.description
            dao.isForAdoption = newPet.isForAdoption
            dao.isForSale     = newPet.isForSale
            dao.updatedAt     = newPet.updatedAt
            true
        } else false
    }

    override suspend fun updatePhoto(ownerId: String, petId: String, photo: String): Boolean = suspendTransaction {
        val dao = PetDAO
            .find {
                (PetTable.id eq UUID.fromString(petId)) and
                        (PetTable.ownerId eq UUID.fromString(ownerId))
            }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.photo     = photo
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }

    override suspend fun updateStatus(petId: String, status: String): Boolean = suspendTransaction {
        val dao = PetDAO
            .find { PetTable.id eq UUID.fromString(petId) }
            .limit(1)
            .firstOrNull()
        if (dao != null) {
            dao.status    = status
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }

    override suspend fun delete(ownerId: String, petId: String): Boolean = suspendTransaction {
        val rows = PetTable.deleteWhere {
            (PetTable.id eq UUID.fromString(petId)) and
                    (PetTable.ownerId eq UUID.fromString(ownerId))
        }
        rows >= 1
    }
}
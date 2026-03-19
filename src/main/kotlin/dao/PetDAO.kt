package org.delcom.dao

import org.delcom.tables.PetTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class PetDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, PetDAO>(PetTable)

    var ownerId       by PetTable.ownerId
    var name          by PetTable.name
    var species       by PetTable.species
    var breed         by PetTable.breed
    var age           by PetTable.age
    var gender        by PetTable.gender
    var description   by PetTable.description
    var photo         by PetTable.photo
    var status        by PetTable.status
    var isForAdoption by PetTable.isForAdoption
    var isForSale     by PetTable.isForSale
    var createdAt     by PetTable.createdAt
    var updatedAt     by PetTable.updatedAt
}
package org.delcom.dao

import org.delcom.tables.AdoptionTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class AdoptionDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, AdoptionDAO>(AdoptionTable)

    var petId       by AdoptionTable.petId
    var requesterId by AdoptionTable.requesterId
    var message     by AdoptionTable.message
    var status      by AdoptionTable.status
    var createdAt   by AdoptionTable.createdAt
    var updatedAt   by AdoptionTable.updatedAt
}
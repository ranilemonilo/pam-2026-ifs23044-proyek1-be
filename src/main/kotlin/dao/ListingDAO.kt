package org.delcom.dao

import org.delcom.tables.ListingTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ListingDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, ListingDAO>(ListingTable)

    var petId       by ListingTable.petId
    var sellerId    by ListingTable.sellerId
    var title       by ListingTable.title
    var description by ListingTable.description
    var price       by ListingTable.price
    var photo       by ListingTable.photo
    var status      by ListingTable.status
    var createdAt   by ListingTable.createdAt
    var updatedAt   by ListingTable.updatedAt
}
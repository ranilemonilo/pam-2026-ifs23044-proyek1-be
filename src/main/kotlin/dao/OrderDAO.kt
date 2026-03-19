package org.delcom.dao

import org.delcom.tables.OrderTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class OrderDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, OrderDAO>(OrderTable)

    var listingId  by OrderTable.listingId
    var buyerId    by OrderTable.buyerId
    var totalPrice by OrderTable.totalPrice
    var status     by OrderTable.status
    var note       by OrderTable.note
    var createdAt  by OrderTable.createdAt
    var updatedAt  by OrderTable.updatedAt
}
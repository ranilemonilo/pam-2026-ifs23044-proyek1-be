package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrderTable : UUIDTable("orders") {
    val listingId  = uuid("listing_id")
    val buyerId    = uuid("buyer_id")
    val totalPrice = decimal("total_price", 12, 2)
    val status     = varchar("status", 20).default("pending")
    val note       = text("note").nullable()
    val createdAt  = timestamp("created_at")
    val updatedAt  = timestamp("updated_at")
}
package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.math.BigDecimal

object ListingTable : UUIDTable("listings") {
    val petId       = uuid("pet_id")
    val sellerId    = uuid("seller_id")
    val title       = varchar("title", 200)
    val description = text("description").nullable()
    val price       = decimal("price", 12, 2)
    val photo       = varchar("photo", 255).nullable()
    val status      = varchar("status", 20).default("active")
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
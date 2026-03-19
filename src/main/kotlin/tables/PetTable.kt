package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PetTable : UUIDTable("pets") {
    val ownerId      = uuid("owner_id")
    val name         = varchar("name", 100)
    val species      = varchar("species", 50)
    val breed        = varchar("breed", 100).nullable()
    val age          = integer("age").nullable()
    val gender       = varchar("gender", 10)
    val description  = text("description").nullable()
    val photo        = varchar("photo", 255).nullable()
    val status       = varchar("status", 20).default("available")
    val isForAdoption = bool("is_for_adoption").default(true)
    val isForSale    = bool("is_for_sale").default(false)
    val createdAt    = timestamp("created_at")
    val updatedAt    = timestamp("updated_at")
}
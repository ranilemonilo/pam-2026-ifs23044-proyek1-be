package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AdoptionTable : UUIDTable("adoption_requests") {
    val petId       = uuid("pet_id")
    val requesterId = uuid("requester_id")
    val message     = text("message").nullable()
    val status      = varchar("status", 20).default("pending")
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
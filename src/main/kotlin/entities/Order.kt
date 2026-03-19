package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Order(
    var id: String = UUID.randomUUID().toString(),
    var listingId: String,
    var listingTitle: String = "",
    var buyerId: String,
    var buyerName: String = "",
    var totalPrice: Double,
    var status: String = "pending",
    var note: String? = null,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
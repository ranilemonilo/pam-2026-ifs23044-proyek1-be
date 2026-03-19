package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Listing(
    var id: String = UUID.randomUUID().toString(),
    var petId: String,
    var petName: String = "",
    var sellerId: String,
    var sellerName: String = "",
    var title: String,
    var description: String? = null,
    var price: Double,
    var photo: String? = null,
    var urlPhoto: String = "",
    var status: String = "active",

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
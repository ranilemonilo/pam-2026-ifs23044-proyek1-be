package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Pet(
    var id: String = UUID.randomUUID().toString(),
    var ownerId: String,
    var ownerName: String = "",
    var name: String,
    var species: String,
    var breed: String? = null,
    var age: Int? = null,
    var gender: String,
    var description: String? = null,
    var photo: String? = null,
    var urlPhoto: String = "",
    var status: String = "available",
    var isForAdoption: Boolean = true,
    var isForSale: Boolean = false,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
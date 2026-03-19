package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AdoptionRequest(
    var id: String = UUID.randomUUID().toString(),
    var petId: String,
    var petName: String = "",
    var requesterId: String,
    var requesterName: String = "",
    var message: String? = null,
    var status: String = "pending",

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
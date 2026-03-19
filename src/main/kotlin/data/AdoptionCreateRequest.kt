package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.AdoptionRequest

@Serializable
data class AdoptionCreateRequest(
    var petId: String = "",
    var requesterId: String = "",
    var message: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "petId"       to petId,
        "requesterId" to requesterId,
    )

    fun toEntity(): AdoptionRequest = AdoptionRequest(
        petId       = petId,
        requesterId = requesterId,
        message     = message,
        updatedAt   = Clock.System.now(),
    )
}

@Serializable
data class AdoptionStatusRequest(
    var status: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf("status" to status)
}
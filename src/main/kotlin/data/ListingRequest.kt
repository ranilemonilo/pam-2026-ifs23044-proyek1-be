package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Listing

@Serializable
data class ListingRequest(
    var petId: String = "",
    var sellerId: String = "",
    var title: String = "",
    var description: String? = null,
    var price: Double = 0.0,
    var photo: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "petId"       to petId,
        "sellerId"    to sellerId,
        "title"       to title,
        "description" to description,
        "price"       to price,
    )

    fun toEntity(): Listing = Listing(
        petId       = petId,
        sellerId    = sellerId,
        title       = title,
        description = description,
        price       = price,
        photo       = photo,
        updatedAt   = Clock.System.now(),
    )
}
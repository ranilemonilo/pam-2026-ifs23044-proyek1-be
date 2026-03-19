package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Order

@Serializable
data class OrderRequest(
    var listingId: String = "",
    var buyerId: String = "",
    var totalPrice: Double = 0.0,
    var note: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "listingId" to listingId,
        "buyerId"   to buyerId,
    )

    fun toEntity(): Order = Order(
        listingId  = listingId,
        buyerId    = buyerId,
        totalPrice = totalPrice,
        note       = note,
        updatedAt  = Clock.System.now(),
    )
}

@Serializable
data class OrderStatusRequest(
    var status: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf("status" to status)
}
package org.delcom.repositories

import org.delcom.entities.Order

interface IOrderRepository {
    suspend fun getAll(
        buyerId: String,
        status: String,
        page: Int,
        size: Int,
    ): List<Order>

    suspend fun getById(orderId: String): Order?
    suspend fun create(order: Order): String
    suspend fun updateStatus(orderId: String, status: String): Boolean
}
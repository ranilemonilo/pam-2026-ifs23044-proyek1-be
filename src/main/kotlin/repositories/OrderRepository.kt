package org.delcom.repositories

import org.delcom.dao.ListingDAO
import org.delcom.dao.OrderDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Order
import org.delcom.helpers.orderDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ListingTable
import org.delcom.tables.OrderTable
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import java.math.BigDecimal
import java.util.UUID

class OrderRepository : IOrderRepository {

    override suspend fun getAll(
        buyerId: String,
        status: String,
        page: Int,
        size: Int,
    ): List<Order> = suspendTransaction {
        val query = if (status.isNotBlank()) {
            OrderDAO.find {
                (OrderTable.buyerId eq UUID.fromString(buyerId)) and
                        (OrderTable.status eq status)
            }
        } else {
            OrderDAO.find { OrderTable.buyerId eq UUID.fromString(buyerId) }
        }

        query
            .orderBy(OrderTable.createdAt to SortOrder.DESC)
            .limit(size)
            .offset(((page - 1) * size).toLong())
            .map { dao ->
                val listingTitle = ListingDAO
                    .find { ListingTable.id eq dao.listingId }
                    .firstOrNull()?.title ?: ""
                val buyerName = UserDAO
                    .find { UserTable.id eq dao.buyerId }
                    .firstOrNull()?.name ?: ""
                orderDAOToModel(dao, listingTitle, buyerName)
            }
    }

    override suspend fun getById(orderId: String): Order? = suspendTransaction {
        OrderDAO
            .find { OrderTable.id eq UUID.fromString(orderId) }
            .limit(1)
            .firstOrNull()
            ?.let { dao ->
                val listingTitle = ListingDAO
                    .find { ListingTable.id eq dao.listingId }
                    .firstOrNull()?.title ?: ""
                val buyerName = UserDAO
                    .find { UserTable.id eq dao.buyerId }
                    .firstOrNull()?.name ?: ""
                orderDAOToModel(dao, listingTitle, buyerName)
            }
    }

    override suspend fun create(order: Order): String = suspendTransaction {
        OrderDAO.new {
            listingId  = UUID.fromString(order.listingId)
            buyerId    = UUID.fromString(order.buyerId)
            totalPrice = BigDecimal(order.totalPrice)
            status     = order.status
            note       = order.note
            createdAt  = order.createdAt
            updatedAt  = order.updatedAt
        }.id.value.toString()
    }

    override suspend fun updateStatus(orderId: String, status: String): Boolean = suspendTransaction {
        val dao = OrderDAO
            .find { OrderTable.id eq UUID.fromString(orderId) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.status    = status
            dao.updatedAt = kotlinx.datetime.Clock.System.now()
            true
        } else false
    }
}
package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.*
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IListingRepository
import org.delcom.repositories.IOrderRepository
import org.delcom.repositories.IUserRepository

class OrderService(
    private val userRepo: IUserRepository,
    private val listingRepo: IListingRepository,
    private val orderRepo: IOrderRepository,
) {

    suspend fun getAll(call: ApplicationCall) {
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val status = call.request.queryParameters["status"] ?: ""
        val page   = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val size   = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 50)  ?: 10

        val orders = orderRepo.getAll(user.id, status, page, size)

        call.respond(DataResponse("success", "Berhasil mengambil daftar order saya", mapOf("orders" to orders)))
    }

    suspend fun getById(call: ApplicationCall) {
        val orderId = call.parameters["id"] ?: throw AppException(400, "Data order tidak valid!")
        val user    = ServiceHelper.getAuthUser(call, userRepo)

        val order = orderRepo.getById(orderId) ?: throw AppException(404, "Data order tidak tersedia!")
        if (order.buyerId != user.id) throw AppException(403, "Akses ditolak!")

        call.respond(DataResponse("success", "Berhasil mengambil data order", mapOf("order" to order)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<OrderRequest>()
        request.buyerId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("listingId", "Listing ID tidak boleh kosong")
        validator.validate()

        val listing = listingRepo.getById(request.listingId)
            ?: throw AppException(404, "Data listing tidak tersedia!")
        if (listing.status != "active") throw AppException(400, "Listing sudah tidak tersedia!")
        if (listing.sellerId == user.id) throw AppException(400, "Tidak bisa membeli listing milik sendiri!")

        request.totalPrice = listing.price
        val orderId = orderRepo.create(request.toEntity())

        // Tandai listing sebagai sold
        listingRepo.updateStatus(request.listingId, "sold")

        call.respond(io.ktor.http.HttpStatusCode.Created, DataResponse("success", "Berhasil membuat order", mapOf("orderId" to orderId)))
    }

    suspend fun putStatus(call: ApplicationCall) {
        val orderId = call.parameters["id"] ?: throw AppException(400, "Data order tidak valid!")
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<OrderStatusRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("status", "Status tidak boleh kosong")
        validator.validate()

        if (request.status !in listOf("paid", "cancelled", "completed"))
            throw AppException(400, "Status harus 'paid', 'cancelled', atau 'completed'")

        val order = orderRepo.getById(orderId) ?: throw AppException(404, "Data order tidak tersedia!")
        if (order.buyerId != user.id) throw AppException(403, "Akses ditolak!")

        val isUpdated = orderRepo.updateStatus(orderId, request.status)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui status order!")

        call.respond(DataResponse(status = "success", "Berhasil mengubah status order",  data = null as String?))
    }
}
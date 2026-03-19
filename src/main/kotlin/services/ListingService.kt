package org.delcom.services

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.*
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IListingRepository
import org.delcom.repositories.IPetRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class ListingService(
    private val userRepo: IUserRepository,
    private val petRepo: IPetRepository,
    private val listingRepo: IListingRepository,
) {

    suspend fun getAll(call: ApplicationCall) {
        val search   = call.request.queryParameters["search"]   ?: ""
        val species  = call.request.queryParameters["species"]  ?: ""
        val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()
        val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()
        val status   = call.request.queryParameters["status"]   ?: "active"
        val page     = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val size     = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 50)  ?: 10

        val listings = listingRepo.getAll(search, species, minPrice, maxPrice, status, page, size)

        call.respond(DataResponse("success", "Berhasil mengambil daftar listing", mapOf("listings" to listings)))
    }

    suspend fun getById(call: ApplicationCall) {
        val listingId = call.parameters["id"] ?: throw AppException(400, "Data listing tidak valid!")
        val listing   = listingRepo.getById(listingId) ?: throw AppException(404, "Data listing tidak tersedia!")

        call.respond(DataResponse("success", "Berhasil mengambil data listing", mapOf("listing" to listing)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<ListingRequest>()
        request.sellerId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("petId", "Pet ID tidak boleh kosong")
        validator.required("title", "Judul tidak boleh kosong")
        validator.validate()

        if (request.price <= 0) throw AppException(400, "Harga harus lebih dari 0!")

        val pet = petRepo.getById(request.petId) ?: throw AppException(404, "Data pet tidak tersedia!")
        if (pet.ownerId != user.id) throw AppException(403, "Anda bukan pemilik pet ini!")

        val listingId = listingRepo.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil membuat listing", mapOf("listingId" to listingId)))
    }

    suspend fun put(call: ApplicationCall) {
        val listingId = call.parameters["id"] ?: throw AppException(400, "Data listing tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)
        val request   = call.receive<ListingRequest>()
        request.sellerId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul tidak boleh kosong")
        validator.validate()

        if (request.price <= 0) throw AppException(400, "Harga harus lebih dari 0!")

        val oldListing = listingRepo.getById(listingId)
        if (oldListing == null || oldListing.sellerId != user.id)
            throw AppException(404, "Data listing tidak tersedia!")

        request.photo = oldListing.photo
        val isUpdated = listingRepo.update(user.id, listingId, request.toEntity())
        if (!isUpdated) throw AppException(400, "Gagal memperbarui data listing!")

        call.respond(DataResponse(status = "success", "Berhasil mengubah data listing",  data = null as String?))
    }

    suspend fun putPhoto(call: ApplicationCall) {
        val listingId = call.parameters["id"] ?: throw AppException(400, "Data listing tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)

        val oldListing = listingRepo.getById(listingId)
        if (oldListing == null || oldListing.sellerId != user.id)
            throw AppException(404, "Data listing tidak tersedia!")

        var newPhoto: String? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext      = part.originalFileName?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/listings/$fileName"
                    val file     = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    newPhoto = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        if (newPhoto == null) throw AppException(404, "Foto listing tidak tersedia!")
        val newFile = File(newPhoto!!)
        if (!newFile.exists()) throw AppException(404, "Foto listing gagal diunggah!")

        val isUpdated = listingRepo.updatePhoto(user.id, listingId, newPhoto!!)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui foto listing!")

        if (oldListing.photo != null) {
            val oldFile = File(oldListing.photo!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse(status = "success", "Berhasil mengubah foto listing",  data = null as String?))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val listingId = call.parameters["id"] ?: throw AppException(400, "Data listing tidak valid!")
        val listing   = listingRepo.getById(listingId) ?: throw AppException(404, "Data listing tidak tersedia!")

        if (listing.photo == null) throw AppException(404, "Listing belum memiliki foto")
        val file = File(listing.photo!!)
        if (!file.exists()) throw AppException(404, "Foto listing tidak tersedia")

        call.respondFile(file)
    }

    suspend fun delete(call: ApplicationCall) {
        val listingId = call.parameters["id"] ?: throw AppException(400, "Data listing tidak valid!")
        val user      = ServiceHelper.getAuthUser(call, userRepo)

        val oldListing = listingRepo.getById(listingId)
        if (oldListing == null || oldListing.sellerId != user.id)
            throw AppException(404, "Data listing tidak tersedia!")

        val isDeleted = listingRepo.delete(user.id, listingId)
        if (!isDeleted) throw AppException(400, "Gagal menghapus data listing!")

        if (oldListing.photo != null) {
            val oldFile = File(oldListing.photo!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse(status = "success", "Berhasil menghapus data listing",  data = null as String?))
    }
}
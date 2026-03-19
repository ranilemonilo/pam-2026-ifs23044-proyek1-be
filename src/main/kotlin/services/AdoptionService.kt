package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.AdoptionCreateRequest
import org.delcom.data.AdoptionStatusRequest
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IAdoptionRepository
import org.delcom.repositories.IPetRepository
import org.delcom.repositories.IUserRepository

class AdoptionService(
    private val userRepo: IUserRepository,
    private val petRepo: IPetRepository,
    private val adoptionRepo: IAdoptionRepository,
) {

    suspend fun getAll(call: ApplicationCall) {
        val user   = ServiceHelper.getAuthUser(call, userRepo)
        val status = call.request.queryParameters["status"] ?: ""
        val page   = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val size   = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 50)  ?: 10

        val adoptions = adoptionRepo.getAll(user.id, status, page, size)
        call.respond(DataResponse("success", "Berhasil mengambil daftar adoption request", mapOf("adoptions" to adoptions)))
    }

    suspend fun getById(call: ApplicationCall) {
        val adoptionId = call.parameters["id"] ?: throw AppException(400, "Data adoption tidak valid!")
        val user       = ServiceHelper.getAuthUser(call, userRepo)

        val adoption = adoptionRepo.getById(adoptionId)
            ?: throw AppException(404, "Data adoption tidak tersedia!")
        val pet = petRepo.getById(adoption.petId)
            ?: throw AppException(404, "Data pet tidak tersedia!")
        if (adoption.requesterId != user.id && pet.ownerId != user.id)
            throw AppException(403, "Akses ditolak!")

        call.respond(DataResponse("success", "Berhasil mengambil data adoption", mapOf("adoption" to adoption)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AdoptionCreateRequest>()
        request.requesterId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("petId", "Pet ID tidak boleh kosong")
        validator.validate()

        val pet = petRepo.getById(request.petId)
            ?: throw AppException(404, "Data pet tidak tersedia!")
        if (!pet.isForAdoption)
            throw AppException(400, "Pet ini tidak tersedia untuk adopsi!")
        if (adoptionRepo.hasPending(user.id, request.petId))
            throw AppException(409, "Kamu sudah memiliki request adopsi yang pending untuk pet ini!")

        val adoptionId = adoptionRepo.create(request.toEntity())
        call.respond(io.ktor.http.HttpStatusCode.Created, DataResponse("success", "Berhasil mengajukan adoption request", mapOf("adoptionId" to adoptionId)))
    }

    suspend fun putStatus(call: ApplicationCall) {
        val adoptionId = call.parameters["id"] ?: throw AppException(400, "Data adoption tidak valid!")
        val user       = ServiceHelper.getAuthUser(call, userRepo)
        val request    = call.receive<AdoptionStatusRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("status", "Status tidak boleh kosong")
        validator.validate()

        if (request.status !in listOf("approved", "rejected"))
            throw AppException(400, "Status harus 'approved' atau 'rejected'")

        val adoption = adoptionRepo.getById(adoptionId)
            ?: throw AppException(404, "Data adoption tidak tersedia!")
        val pet = petRepo.getById(adoption.petId)
            ?: throw AppException(404, "Data pet tidak tersedia!")
        if (pet.ownerId != user.id)
            throw AppException(403, "Hanya pemilik pet yang bisa mengubah status!")

        val isUpdated = adoptionRepo.updateStatus(adoptionId, request.status)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui status adoption!")

        if (request.status == "approved") petRepo.updateStatus(adoption.petId, "adopted")

        val response = DataResponse(status = "success", message = "Berhasil mengubah status adoption request", data = null as String?)
        call.respond(response)
    }

    suspend fun delete(call: ApplicationCall) {
        val adoptionId = call.parameters["id"] ?: throw AppException(400, "Data adoption tidak valid!")
        val user       = ServiceHelper.getAuthUser(call, userRepo)

        val adoption = adoptionRepo.getById(adoptionId)
            ?: throw AppException(404, "Data adoption tidak tersedia!")
        if (adoption.requesterId != user.id)
            throw AppException(403, "Hanya pemohon yang bisa membatalkan request!")

        val isDeleted = adoptionRepo.delete(user.id, adoptionId)
        if (!isDeleted) throw AppException(400, "Gagal menghapus adoption request!")

        val response = DataResponse(status = "success", message = "Berhasil membatalkan adoption request", data = null as String?)
        call.respond(response)
    }
}
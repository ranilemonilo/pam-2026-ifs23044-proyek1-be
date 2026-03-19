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
import org.delcom.repositories.IPetRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class PetService(
    private val userRepo: IUserRepository,
    private val petRepo: IPetRepository,
) {

    suspend fun getAll(call: ApplicationCall) {
        val search      = call.request.queryParameters["search"]      ?: ""
        val species     = call.request.queryParameters["species"]     ?: ""
        val gender      = call.request.queryParameters["gender"]      ?: ""
        val status      = call.request.queryParameters["status"]      ?: ""
        val forAdoption = call.request.queryParameters["forAdoption"]?.toBooleanStrictOrNull()
        val forSale     = call.request.queryParameters["forSale"]?.toBooleanStrictOrNull()
        val page        = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val size        = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 50)  ?: 10

        val pets = petRepo.getAll(search, species, gender, status, forAdoption, forSale, page, size)

        call.respond(DataResponse("success", "Berhasil mengambil daftar pet", mapOf("pets" to pets)))
    }

    suspend fun getById(call: ApplicationCall) {
        val petId = call.parameters["id"] ?: throw AppException(400, "Data pet tidak valid!")
        val pet   = petRepo.getById(petId) ?: throw AppException(404, "Data pet tidak tersedia!")

        call.respond(DataResponse("success", "Berhasil mengambil data pet", mapOf("pet" to pet)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<PetRequest>()
        request.ownerId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("name",    "Nama pet tidak boleh kosong")
        validator.required("species", "Spesies tidak boleh kosong")
        validator.required("gender",  "Gender tidak boleh kosong")
        validator.validate()

        val petId = petRepo.create(request.toEntity())

        call.respond(io.ktor.http.HttpStatusCode.Created, DataResponse("success", "Berhasil menambahkan data pet", mapOf("petId" to petId)))
    }

    suspend fun put(call: ApplicationCall) {
        val petId   = call.parameters["id"] ?: throw AppException(400, "Data pet tidak valid!")
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<PetRequest>()
        request.ownerId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("name",    "Nama pet tidak boleh kosong")
        validator.required("species", "Spesies tidak boleh kosong")
        validator.required("gender",  "Gender tidak boleh kosong")
        validator.validate()

        val oldPet = petRepo.getById(petId)
        if (oldPet == null || oldPet.ownerId != user.id)
            throw AppException(404, "Data pet tidak tersedia!")

        request.photo = oldPet.photo
        val isUpdated = petRepo.update(user.id, petId, request.toEntity())
        if (!isUpdated) throw AppException(400, "Gagal memperbarui data pet!")

        call.respond(DataResponse(status = "success", "Berhasil mengubah data pet",  data = null as String?))
    }

    suspend fun putPhoto(call: ApplicationCall) {
        val petId = call.parameters["id"] ?: throw AppException(400, "Data pet tidak valid!")
        val user  = ServiceHelper.getAuthUser(call, userRepo)

        val oldPet = petRepo.getById(petId)
        if (oldPet == null || oldPet.ownerId != user.id)
            throw AppException(404, "Data pet tidak tersedia!")

        var newPhoto: String? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext      = part.originalFileName?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/pets/$fileName"
                    val file     = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    newPhoto = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        if (newPhoto == null) throw AppException(404, "Foto pet tidak tersedia!")
        val newFile = File(newPhoto!!)
        if (!newFile.exists()) throw AppException(404, "Foto pet gagal diunggah!")

        val isUpdated = petRepo.updatePhoto(user.id, petId, newPhoto!!)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui foto pet!")

        if (oldPet.photo != null) {
            val oldFile = File(oldPet.photo!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse(status = "success", "Berhasil mengubah foto pet",  data = null as String?))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val petId = call.parameters["id"] ?: throw AppException(400, "Data pet tidak valid!")
        val pet   = petRepo.getById(petId) ?: throw AppException(404, "Data pet tidak tersedia!")

        if (pet.photo == null) throw AppException(404, "Pet belum memiliki foto")
        val file = File(pet.photo!!)
        if (!file.exists()) throw AppException(404, "Foto pet tidak tersedia")

        call.respondFile(file)
    }

    suspend fun delete(call: ApplicationCall) {
        val petId = call.parameters["id"] ?: throw AppException(400, "Data pet tidak valid!")
        val user  = ServiceHelper.getAuthUser(call, userRepo)

        val oldPet = petRepo.getById(petId)
        if (oldPet == null || oldPet.ownerId != user.id)
            throw AppException(404, "Data pet tidak tersedia!")

        val isDeleted = petRepo.delete(user.id, petId)
        if (!isDeleted) throw AppException(400, "Gagal menghapus data pet!")

        if (oldPet.photo != null) {
            val oldFile = File(oldPet.photo!!)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse(status = "success", "Berhasil menghapus data pet",  data = null as String?))
    }
}
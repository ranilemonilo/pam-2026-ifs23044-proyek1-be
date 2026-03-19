package org.delcom.repositories

import org.delcom.entities.Pet

interface IPetRepository {
    suspend fun getAll(
        search: String,
        species: String,
        gender: String,
        status: String,
        forAdoption: Boolean?,
        forSale: Boolean?,
        page: Int,
        size: Int,
    ): List<Pet>

    suspend fun getById(petId: String): Pet?
    suspend fun create(pet: Pet): String
    suspend fun update(ownerId: String, petId: String, newPet: Pet): Boolean
    suspend fun updatePhoto(ownerId: String, petId: String, photo: String): Boolean
    suspend fun updateStatus(petId: String, status: String): Boolean
    suspend fun delete(ownerId: String, petId: String): Boolean
}
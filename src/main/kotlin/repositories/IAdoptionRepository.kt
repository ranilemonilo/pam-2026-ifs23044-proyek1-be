package org.delcom.repositories

import org.delcom.entities.AdoptionRequest

interface IAdoptionRepository {
    suspend fun getAll(
        userId: String,
        status: String,
        page: Int,
        size: Int,
    ): List<AdoptionRequest>

    suspend fun getById(adoptionId: String): AdoptionRequest?
    suspend fun hasPending(requesterId: String, petId: String): Boolean
    suspend fun create(adoption: AdoptionRequest): String
    suspend fun updateStatus(adoptionId: String, status: String): Boolean
    suspend fun delete(requesterId: String, adoptionId: String): Boolean
}
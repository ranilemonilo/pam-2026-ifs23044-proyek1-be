package org.delcom.repositories

import org.delcom.entities.Listing

interface IListingRepository {
    suspend fun getAll(
        search: String,
        species: String,
        minPrice: Double?,
        maxPrice: Double?,
        status: String,
        page: Int,
        size: Int,
    ): List<Listing>

    suspend fun getById(listingId: String): Listing?
    suspend fun create(listing: Listing): String
    suspend fun update(sellerId: String, listingId: String, newListing: Listing): Boolean
    suspend fun updatePhoto(sellerId: String, listingId: String, photo: String): Boolean
    suspend fun updateStatus(listingId: String, status: String): Boolean
    suspend fun delete(sellerId: String, listingId: String): Boolean
}
package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.*
import org.delcom.entities.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO, baseUrl: String) = User(
    id        = dao.id.value.toString(),
    name      = dao.name,
    username  = dao.username,
    password  = dao.password,
    photo     = dao.photo,
    urlPhoto  = buildImageUrl(baseUrl, dao.photo ?: "/uploads/defaults/user.png"),
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt,
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id           = dao.id.value.toString(),
    userId       = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken    = dao.authToken,
    createdAt    = dao.createdAt,
)

fun petDAOToModel(dao: PetDAO, baseUrl: String, ownerName: String = "") = Pet(
    id           = dao.id.value.toString(),
    ownerId      = dao.ownerId.toString(),
    ownerName    = ownerName,
    name         = dao.name,
    species      = dao.species,
    breed        = dao.breed,
    age          = dao.age,
    gender       = dao.gender,
    description  = dao.description,
    photo        = dao.photo,
    urlPhoto     = buildImageUrl(baseUrl, dao.photo ?: "/uploads/defaults/pet.png"),
    status       = dao.status,
    isForAdoption = dao.isForAdoption,
    isForSale    = dao.isForSale,
    createdAt    = dao.createdAt,
    updatedAt    = dao.updatedAt,
)

fun adoptionDAOToModel(
    dao: AdoptionDAO,
    petName: String = "",
    requesterName: String = "",
) = AdoptionRequest(
    id            = dao.id.value.toString(),
    petId         = dao.petId.toString(),
    petName       = petName,
    requesterId   = dao.requesterId.toString(),
    requesterName = requesterName,
    message       = dao.message,
    status        = dao.status,
    createdAt     = dao.createdAt,
    updatedAt     = dao.updatedAt,
)

fun listingDAOToModel(
    dao: ListingDAO,
    baseUrl: String,
    petName: String = "",
    sellerName: String = "",
) = Listing(
    id          = dao.id.value.toString(),
    petId       = dao.petId.toString(),
    petName     = petName,
    sellerId    = dao.sellerId.toString(),
    sellerName  = sellerName,
    title       = dao.title,
    description = dao.description,
    price       = dao.price.toDouble(),
    photo       = dao.photo,
    urlPhoto    = buildImageUrl(baseUrl, dao.photo ?: "/uploads/defaults/listing.png"),
    status      = dao.status,
    createdAt   = dao.createdAt,
    updatedAt   = dao.updatedAt,
)

fun orderDAOToModel(
    dao: OrderDAO,
    listingTitle: String = "",
    buyerName: String = "",
) = Order(
    id           = dao.id.value.toString(),
    listingId    = dao.listingId.toString(),
    listingTitle = listingTitle,
    buyerId      = dao.buyerId.toString(),
    buyerName    = buyerName,
    totalPrice   = dao.totalPrice.toDouble(),
    status       = dao.status,
    note         = dao.note,
    createdAt    = dao.createdAt,
    updatedAt    = dao.updatedAt,
)

/**
 * Membangun URL publik gambar dari path relatif.
 * Contoh: "uploads/pets/uuid.png" → "http://host:port/static/pets/uuid.png"
 */
fun buildImageUrl(baseUrl: String, pathGambar: String): String {
    val relativePath = pathGambar.removePrefix("uploads/")
    return "$baseUrl/static/$relativePath"
}
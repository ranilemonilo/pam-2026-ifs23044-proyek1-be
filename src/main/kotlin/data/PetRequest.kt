package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Pet

@Serializable
data class PetRequest(
    var ownerId: String = "",
    var name: String = "",
    var species: String = "",
    var breed: String? = null,
    var age: Int? = null,
    var gender: String = "",
    var description: String? = null,
    var photo: String? = null,
    var isForAdoption: Boolean = true,
    var isForSale: Boolean = false,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "ownerId"      to ownerId,
        "name"         to name,
        "species"      to species,
        "breed"        to breed,
        "age"          to age,
        "gender"       to gender,
        "description"  to description,
        "isForAdoption" to isForAdoption,
        "isForSale"    to isForSale,
    )

    fun toEntity(): Pet = Pet(
        ownerId      = ownerId,
        name         = name,
        species      = species,
        breed        = breed,
        age          = age,
        gender       = gender,
        description  = description,
        photo        = photo,
        isForAdoption = isForAdoption,
        isForSale    = isForSale,
        updatedAt    = Clock.System.now(),
    )
}
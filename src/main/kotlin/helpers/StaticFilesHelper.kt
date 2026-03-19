package org.delcom.helpers

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

/**
 * Mendaftarkan folder "uploads/" sebagai direktori file statis
 * yang dapat diakses melalui URL "/static/...".
 *
 * Contoh akses:
 *   File di disk  : uploads/pets/uuid.png
 *   URL publik    : http://host:port/static/pets/uuid.png
 *   File di disk  : uploads/users/me.png
 *   URL publik    : http://host:port/static/users/me.png
 */
fun Application.configureStaticFiles() {
    val uploadDir = File("uploads")
    if (!uploadDir.exists()) uploadDir.mkdirs()

    // Buat sub-folder default
    listOf("users", "pets", "listings", "defaults").forEach {
        File("uploads/$it").mkdirs()
    }

    routing {
        staticFiles("/static", uploadDir)
    }
}
package org.delcom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService: AuthService         by inject()
    val userService: UserService         by inject()
    val petService: PetService           by inject()
    val adoptionService: AdoptionService by inject()
    val listingService: ListingService   by inject()
    val orderService: OrderService       by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                status  = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status  = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data    = if (dataMap.isEmpty()) null else dataMap.toString(),
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status  = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status  = "error",
                    message = cause.message ?: "Unknown error",
                    data    = "",
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Pet Adoption & Marketplace API berjalan. Dibuat oleh ifs23044.")
        }

        // ── Auth ──────────────────────────────────────────────────────────────
        route("/auth") {
            post("/register") {
                authService.postRegister(call)
            }
            post("/login") {
                authService.postLogin(call)
            }
            post("/refresh-token") {
                authService.postRefreshToken(call)
            }
            post("/logout") {
                authService.postLogout(call)
            }
        }

        authenticate(JWTConstants.NAME) {

            // ── Users ─────────────────────────────────────────────────────────
            route("/users") {
                get("/me") {
                    userService.getMe(call)
                }
                put("/me") {
                    userService.putMe(call)
                }
                put("/me/password") {
                    userService.putMyPassword(call)
                }
                put("/me/photo") {
                    userService.putMyPhoto(call)
                }
            }

            // ── Pets ──────────────────────────────────────────────────────────
            route("/pets") {
                get {
                    petService.getAll(call)
                }
                post {
                    petService.post(call)
                }
                get("/{id}") {
                    petService.getById(call)
                }
                put("/{id}") {
                    petService.put(call)
                }
                put("/{id}/photo") {
                    petService.putPhoto(call)
                }
                delete("/{id}") {
                    petService.delete(call)
                }
            }

            // ── Adoptions ─────────────────────────────────────────────────────
            route("/adoptions") {
                get {
                    adoptionService.getAll(call)
                }
                post {
                    adoptionService.post(call)
                }
                get("/{id}") {
                    adoptionService.getById(call)
                }
                put("/{id}/status") {
                    adoptionService.putStatus(call)
                }
                delete("/{id}") {
                    adoptionService.delete(call)
                }
            }

            // ── Listings ──────────────────────────────────────────────────────
            route("/listings") {
                get {
                    listingService.getAll(call)
                }
                post {
                    listingService.post(call)
                }
                get("/{id}") {
                    listingService.getById(call)
                }
                put("/{id}") {
                    listingService.put(call)
                }
                put("/{id}/photo") {
                    listingService.putPhoto(call)
                }
                delete("/{id}") {
                    listingService.delete(call)
                }
            }

            // ── Orders ────────────────────────────────────────────────────────
            route("/orders") {
                get {
                    orderService.getAll(call)
                }
                post {
                    orderService.post(call)
                }
                get("/{id}") {
                    orderService.getById(call)
                }
                put("/{id}/status") {
                    orderService.putStatus(call)
                }
            }
        }

        // ── Images (public) ───────────────────────────────────────────────────
        route("/images") {
            get("/users/{id}") {
                userService.getPhoto(call)
            }
            get("/pets/{id}") {
                petService.getPhoto(call)
            }
            get("/listings/{id}") {
                listingService.getPhoto(call)
            }
        }
    }
}
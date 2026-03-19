package org.delcom.module

import io.ktor.server.application.*
import io.ktor.server.application.*
import org.delcom.repositories.*
import org.delcom.services.*
import org.koin.dsl.module

fun appModule(application: Application) = module {
    val baseUrl = application.environment.config
        .property("ktor.app.baseUrl")
        .getString()
        .trimEnd('/')

    val jwtSecret = application.environment.config
        .property("ktor.jwt.secret")
        .getString()

    // ── Repositories ──────────────────────────────────────────────────────────
    single<IUserRepository> {
        UserRepository(baseUrl)
    }

    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }

    single<IPetRepository> {
        PetRepository(baseUrl)
    }

    single<IAdoptionRepository> {
        AdoptionRepository()
    }

    single<IListingRepository> {
        ListingRepository(baseUrl)
    }

    single<IOrderRepository> {
        OrderRepository()
    }

    // ── Services ──────────────────────────────────────────────────────────────
    single {
        AuthService(jwtSecret, get(), get())
    }

    single {
        UserService(get(), get())
    }

    single {
        PetService(get(), get())
    }

    single {
        AdoptionService(get(), get(), get())
    }

    single {
        ListingService(get(), get(), get())
    }

    single {
        OrderService(get(), get(), get())
    }
}
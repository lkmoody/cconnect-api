package com.constituentconnect.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.constituentconnect.database.Users
import com.constituentconnect.models.User
import io.ktor.client.*
import io.ktor.http.cio.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Error
import java.net.URI
import java.util.concurrent.TimeUnit
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun Application.configureSecurity() {
    install(Authentication) {
        jwt {
            val issuer = this@configureSecurity.environment.config.property("jwt.issuer").getString()
            val audiences = this@configureSecurity.environment.config.property("jwt.audiences").getList()
            val jwkProvider = JwkProviderBuilder(issuer)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }

            validate { credential ->
                if (credential.payload.getClaim("aud").asString() in audiences && credential.payload.getClaim("token_use").asString() == "id") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getCurrentUser(): User {
    val username = getCurrentUsername()
    val user = transaction {
        Users.select {
            Users.authId eq username
        }
            .limit(1)
            .singleOrNull()
            ?.let {
                User(
                    it[Users.id].toString().toInt(),
                    it[Users.authId],
                    it[Users.firstName],
                    it[Users.lastName],
                    it[Users.displayName],
                    it[Users.phone],
                    it[Users.email],
                    it[Users.created],
                    it[Users.updated]
                )
            } ?: throw throw AuthenticationError()
    }
    return user
}

fun ApplicationCall.getCurrentUsername(): String {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationError()
    return principal.payload.claims["sub"]?.asString() ?: throw AuthenticationError()
}

fun ApplicationCall.getCurrentUserEmail(): String {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationError()
    return principal.payload.claims["email"]?.asString() ?: throw AuthenticationError()
}

class CurrentUser(
    name: String
)

class AuthenticationError: Error()

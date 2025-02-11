package com.constituentconnect.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.*
import io.ktor.http.cio.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.util.concurrent.TimeUnit
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
                println(credential.payload.getClaim("aud"))
                if (credential.payload.getClaim("aud").asString() in audiences && credential.payload.getClaim("token_use").asString() == "id") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getCurrentUsername(): String {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationException()
    return principal.payload.claims?.get("sub")?.asString() ?: throw AuthenticationException()
}

fun ApplicationCall.getCurrentUserRoles(): List<String>? {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationException()
    return principal.payload.claims?.get("cognito:groups")?.asArray(String::class.java)?.toList() ?: throw AuthenticationException()
}

fun ApplicationCall.getCurrentUserEmail(): String {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationException()
    return principal.payload.claims?.get("email")?.asString() ?: throw AuthenticationException()
}

fun Application.getUserStuff() {

}

class AuthenticationException: Exception()

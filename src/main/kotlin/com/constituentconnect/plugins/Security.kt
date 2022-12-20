package com.constituentconnect.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.lang.Error
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
                if (credential.payload.getClaim("client_id").asString() in audiences && credential.payload.getClaim("token_use").asString() == "access") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getCurrentUsernameOrNull(): String? {
    val principal = principal<JWTPrincipal>() ?: throw AuthenticationError()
    return principal.subject
}

class CurrentUser(
    name: String
)

class AuthenticationError: Error()

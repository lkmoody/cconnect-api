package com.example.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {
    authentication {
        jwt {

        }
    }
}

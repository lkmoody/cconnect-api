package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        val clientDomain = this@configureCORS.environment.config.property("ktor.cors.client_domain").getString()
        val clientURI = Url(clientDomain).toURI()

        val host = if (clientURI.port >= 0) "${clientURI.host}:${clientURI.port}" else clientURI.host
        val scheme = listOf(clientURI.scheme)

        allowHost(host, scheme)
        println(host)
    }
}
package com.constituentconnect.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        val clientDomain = this@configureCORS.environment.config.property("ktor.cors.client_domain").getString()
        val clientURI = Url(clientDomain).toURI()

        val julieDomain = "http://localhost:3001"
        val julieURI = Url(julieDomain).toURI()

        val host = if (clientURI.port >= 0) "${clientURI.host}:${clientURI.port}" else clientURI.host
        val scheme = listOf(clientURI.scheme)

        val julieHost = if (julieURI.port >= 0) "${julieURI.host}:${julieURI.port}" else julieURI.host
        val julieScheme = listOf(julieURI.scheme)

        allowHost(host, scheme)
        allowHost(julieHost, julieScheme)
    }
}
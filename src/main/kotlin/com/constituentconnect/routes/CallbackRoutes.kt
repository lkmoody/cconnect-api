package com.constituentconnect.routes

import com.constituentconnect.database.UserSettingEntity
import com.constituentconnect.database.UserSettings
import com.constituentconnect.database.UserTwitterEntity
import com.constituentconnect.database.UserTwitters
import com.constituentconnect.plugins.AuthenticationException
import com.constituentconnect.plugins.getApiUrl
import com.constituentconnect.plugins.getCurrentUser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.callbackRouting() {
    route("/callback") {
        route("/twitter") {
            twitterCallbackAuth()
        }
    }
}

fun Route.twitterCallbackAuth() {
    post {
        val clientErrorMessage = "There was an issue trying to authorize with Twitter"
        try {
            val user = call.getCurrentUser() ?: throw AuthenticationException()
            val callbackAuthRequest = call.receive<CallbackAuthRequest>()
            val requestAccessToken = transaction {
                UserTwitterEntity.find { UserTwitters.userId eq user.id }.firstOrNull()?.requestAccessToken
            }

            if (requestAccessToken == callbackAuthRequest.oauthToken) {
                val twitterApiUrl = call.getApiUrl()
                val client = HttpClient(CIO)
                val response =
                    client.post("$twitterApiUrl/oauth/access_token?oauth_verifier=${callbackAuthRequest.oauthVerifier}&oauth_token=${callbackAuthRequest.oauthToken}")

                val oauthResults = HashMap<String, String>()
                println(response.bodyAsText())
                val splitValues = response.bodyAsText().split("&")
                for (value in splitValues) {
                    val keyValue = value.split("=")
                    oauthResults[keyValue[0]] = keyValue[1]
                }

                if (response.status == HttpStatusCode.OK) {
                    val oauthToken = oauthResults["oauth_token"] ?: throw AuthenticationException()
                    val oauthTokenSecret = oauthResults["oauth_token_secret"] ?: throw AuthenticationException()
                    transaction {
                        val userTwitter = UserTwitterEntity.find { UserTwitters.userId eq user.id }.firstOrNull()
                        if (userTwitter == null) {
                            UserTwitterEntity.new {
                                userId = user.id
                                token = oauthToken
                                secret = oauthTokenSecret
                            }
                        } else {
                            userTwitter.token = oauthToken
                            userTwitter.secret = oauthTokenSecret
                        }
                    }

                    transaction {
                        val userSettings = UserSettingEntity.find { UserSettings.userID eq user.id }.firstOrNull()
                        if (userSettings == null) {
                            UserSettingEntity.new {
                                userID = user.id
                                twitterVotePostEnabled = true
                                voteTextNotificationEnabled = false
                            }
                        } else {
                            userSettings.twitterVotePostEnabled = true
                        }
                    }

                    val response = CallbackAuthResponse(status = "Success")

                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
                    println(response.status)
                }
            } else {
                call.respond(HttpStatusCode.InternalServerError, callbackAuthRequest)
                println("Token did not match the request token.")
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, clientErrorMessage)
            println(e.message)
        }
    }
}

@Serializable
data class CallbackAuthRequest(
    val oauthToken: String,
    val oauthVerifier: String
)

@Serializable
data class CallbackAuthResponse(
    val status: String
)
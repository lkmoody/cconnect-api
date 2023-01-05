package com.constituentconnect.routes

import com.constituentconnect.database.*
import com.constituentconnect.plugins.AuthenticationError
import com.constituentconnect.plugins.getApiUrl
import com.constituentconnect.plugins.getCurrentUsername
import com.constituentconnect.plugins.postTweet
import com.twitter.clientlib.model.User
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
import java.util.HashMap

fun Route.callbackRouting() {
    route("/callback") {
        route("/twitter") {
            twitterCallbackAuth()
        }
    }
}

fun Route.twitterCallbackAuth() {
    post {
        println("Start")
        val callbackAuthRequest = call.receive<CallbackAuthRequest>()
        val twitterApiUrl = call.getApiUrl()
        try {
            val client = HttpClient(CIO)
            val response = client.post("$twitterApiUrl/oauth/access_token?oauth_verifier=${callbackAuthRequest.oauthVerifier}&oauth_token=${callbackAuthRequest.oauthToken}")

            var oauthResults = HashMap<String, String>()
            println(response.bodyAsText())
            val splitValues = response.bodyAsText().split("&")
            for(value in splitValues) {
                val keyValue = value.split("=")
                oauthResults[keyValue[0]] = keyValue[1]
            }

            if(response.status == HttpStatusCode.OK) {
                val oauthToken = oauthResults["oauth_token"] ?: throw AuthenticationError()
                val oauthTokenSecret = oauthResults["oauth_token_secret"] ?: throw AuthenticationError()
                transaction {
                    val userTwitter = UserTwitterEntity.find { UserTwitters.userId eq callbackAuthRequest.userId }.firstOrNull()
                    if(userTwitter == null) {
                        UserTwitterEntity.new {
                            userId = callbackAuthRequest.userId
                            token = oauthToken
                            secret = oauthTokenSecret
                        }
                    } else {
                        userTwitter.token = oauthToken
                        userTwitter.secret = oauthTokenSecret
                    }
                }

                transaction {
                    val userSettings = UserSettingEntity.find { UserSettings.userID eq callbackAuthRequest.userId }.firstOrNull()
                    if (userSettings == null) {
                        UserSettingEntity.new {
                            userID = callbackAuthRequest.userId
                            twitterVotePostEnabled = true
                            voteTextNotificationEnabled = false
                        }
                    } else {
                        userSettings.twitterVotePostEnabled = true
                    }
                }

                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(response.status)
            }

        } catch (e: Error) {

        }
        call.respond(HttpStatusCode.OK)
    }
}

@Serializable
data class CallbackAuthRequest(
    val userId: Int,
    val oauthToken: String,
    val oauthVerifier: String
)
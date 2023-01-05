package com.constituentconnect.plugins

import com.constituentconnect.database.UserSettingEntity
import com.constituentconnect.database.UserSettings
import com.constituentconnect.database.UserTwitterEntity
import com.constituentconnect.database.UserTwitters
import com.constituentconnect.routes.computeSignature
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.util.*
import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.exposed.sql.transactions.transaction

fun ApplicationCall.getApiUrl(): String {
    return this@getApiUrl.application.environment.config.property("twitter.api_url").getString()
}

fun ApplicationCall.getConsumerKey(): String {
    return this@getConsumerKey.application.environment.config.property("twitter.consumer_api_key").getString()
}

fun ApplicationCall.getConsumerSecretKey(): String {
    return this@getConsumerSecretKey.application.environment.config.property("twitter.consumer_secret_key").getString()
}

fun ApplicationCall.getCallbackUrl(): String {
    return this@getCallbackUrl.application.environment.config.property("twitter.callback_url").getString()
}

fun ApplicationCall.getNonce(): String {
    return UUID.randomUUID().toString().replace("-", "")
}

fun ApplicationCall.voteTweetEnabled(): Boolean {
    return transaction {
        UserSettingEntity.find { UserSettings.userID eq getCurrentUser().id }.first().twitterVotePostEnabled
    }
}

fun ApplicationCall.getTwitterTokens(): Pair<String, String> {
    val user = getCurrentUser()
    val userTweet = transaction {
        UserTwitterEntity.find {UserTwitters.userId eq user.id}.first() ?: throw Error()
    }
    return Pair(userTweet.token,userTweet.secret)
}

suspend fun ApplicationCall.postTweet(tweet: String) {
    val oauthSignatureMethod = "HMAC-SHA1"
    val oauthConsumerKey = getConsumerKey()
    val oauthConsumerSecretKey = getConsumerSecretKey()
    val oauthNonce = getNonce()
    var oauthTimestamp = (System.currentTimeMillis() / 1000).toString()
    val (oauthToken, oauthSecret) = getTwitterTokens()

    // Create the parameter string used to create the signature. Be careful modifying this. The string needs to be constructed in a very particular manner.
    var parameterString = URLEncoder.encode("oauth_consumer_key=$oauthConsumerKey", StandardCharsets.UTF_8.toString())
    parameterString += URLEncoder.encode("&oauth_nonce=$oauthNonce", StandardCharsets.UTF_8.toString())
    parameterString += URLEncoder.encode("&oauth_signature_method=$oauthSignatureMethod", StandardCharsets.UTF_8.toString())
    parameterString += URLEncoder.encode("&oauth_timestamp=$oauthTimestamp", StandardCharsets.UTF_8.toString())
    parameterString += URLEncoder.encode("&oauth_token=$oauthToken", StandardCharsets.UTF_8.toString())
    parameterString += URLEncoder.encode("&oauth_version=1.0", StandardCharsets.UTF_8.toString())
    println(parameterString)

    val signatureBaseString = "POST&" + URLEncoder.encode("https://api.twitter.com/2/tweets", StandardCharsets.UTF_8.toString()) + "&$parameterString"

    println(signatureBaseString)
    var oauthSignature = ""
    try {
        oauthSignature =
            computeSignature(signatureBaseString, "$oauthConsumerSecretKey&$oauthSecret") // The "&" symbol needs to be at the end. Something to do with a possible auth token you can add to the end. We are not doing that but leave the symbol
        println(oauthSignature)
    } catch (e: GeneralSecurityException) {

    }

    val urlEncodedOauthSignature = URLEncoder.encode(oauthSignature, StandardCharsets.UTF_8.toString())
    println(urlEncodedOauthSignature)

    val authorizationHeader = "OAuth oauth_consumer_key=\"$oauthConsumerKey\",oauth_token=\"$oauthToken\",oauth_signature_method=\"$oauthSignatureMethod\",oauth_timestamp=\"$oauthTimestamp\",oauth_nonce=\"$oauthNonce\",oauth_version=\"1.0\",oauth_signature=\"$urlEncodedOauthSignature\""
    println(authorizationHeader)

    try {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val response: HttpResponse = client.post("https://api.twitter.com/2/tweets") {
            headers {
                append(HttpHeaders.Authorization, authorizationHeader)
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            setBody(Tweet(tweet))
        }

        println(response)

    } catch (e: Error) {
        throw Error()
    }
}

@Serializable
data class Tweet(
    val text: String
)
package com.constituentconnect.plugins.integrations

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest
import com.constituentconnect.plugins.getCurrentUsername
import io.ktor.server.application.*
import io.ktor.util.*

class AWS(configuration: PluginConfiguration) {
    val accessKey = configuration.accessKey
    val secretKey = configuration.secretKey
    val cognitoPool = configuration.cognitoPool

    private val cognitoProvider: AWSCognitoIdentityProvider

    class PluginConfiguration {
        var accessKey = ""
        var secretKey = ""
        var cognitoPool = ""
    }

    init {
        println("Initializing AWS integration")
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        cognitoProvider = AWSCognitoIdentityProviderClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_WEST_2)
            .build()
    }

    fun addUserToRole(userName: String, roleName: String) {
        val roleRequest = AdminAddUserToGroupRequest()
        roleRequest.groupName = roleName
        roleRequest.username = userName
        roleRequest.userPoolId = cognitoPool

        cognitoProvider.adminAddUserToGroup(roleRequest)
    }

    companion object Feature : BaseApplicationPlugin<Application, PluginConfiguration, AWS> {
        override val key = AttributeKey<AWS>("cognito")

        override fun install(pipeline: Application, configure: PluginConfiguration.() -> Unit): AWS {
            val configuration = PluginConfiguration().apply(configure)

            return AWS(configuration)
        }
    }
}

fun Application.configureCognito() {
    val awsAccessKey = environment.config.property("aws.access_key").getString()
    val awsSecretKey = environment.config.property("aws.secret_key").getString()
    val awsCognitoPool = environment.config.property("aws.cognito_pool").getString()

    install(AWS) {
        accessKey = awsAccessKey
        secretKey = awsSecretKey
        cognitoPool = awsCognitoPool
    }
}

val Application.aws: AWS
    get() = this.plugin(AWS)


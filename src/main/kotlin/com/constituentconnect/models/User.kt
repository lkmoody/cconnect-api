package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
class User(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val authId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val phone: Int? = null,
    val email: String,
    var settings: UserSettings? = null,
    @Serializable(UUIDSerializer::class)
    var groupId: UUID? = null,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)

@Serializable
class UserSettings(
    val voteTextNotificationEnabled: Boolean,
    val twitterVotePostEnabled: Boolean
)

@Serializable
class UpdateUserRequest(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val phone: Int? = null,
    val email: String
)
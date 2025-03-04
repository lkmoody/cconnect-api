package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

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
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)
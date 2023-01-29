package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

@Serializable
class Group(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val displayName: String,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)
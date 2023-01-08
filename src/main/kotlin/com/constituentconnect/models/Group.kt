package com.constituentconnect.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class Group(
    val id: Int,
    val name: String,
    val displayName: String,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)
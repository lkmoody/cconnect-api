package com.example.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Vote(
    val id: Int,
    val billId: Int,
    val vote: String,
    val pros: String,
    val cons: String,
    val reasoning: String,
    @Serializable(InstantSerializer::class)
    val created: Instant,
    @Serializable(InstantSerializer::class)
    val updated: Instant
)
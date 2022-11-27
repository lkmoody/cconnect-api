package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Vote(val id: String, val billId: String)

val billVoteStorage = mutableListOf<Vote>()
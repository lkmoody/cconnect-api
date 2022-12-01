package com.example.database

import com.example.models.Vote
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class VoteEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VoteEntity>(Votes)

    var billId by Votes.billId
    var vote by Votes.vote
    var pros by Votes.pros
    var cons by Votes.cons
    var reasoning by Votes.reasoning
    var created by Votes.created
    var updated by Votes.updated
}

object Votes : IntIdTable("core.votes") {
    val billId = integer("billId")
    val vote = text("vote")
    val pros = text("pros")
    val cons = text("cons")
    val reasoning = text("reasoning")
    val created = timestamp("created")
    val updated = timestamp("updated")
}

fun voteToApi(voteEntity: VoteEntity): Vote = Vote(
    voteEntity.id.value,
    voteEntity.billId,
    voteEntity.vote,
    voteEntity.pros,
    voteEntity.cons,
    voteEntity.reasoning,
    voteEntity.created,
    voteEntity.updated
)
package com.constituentconnect.database

import com.constituentconnect.models.Vote
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

class VoteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<VoteEntity>(Votes)

    var billId by Votes.billId
    var userId by Votes.userId
    var voteDetailId by Votes.voteDetailId
    var created by Votes.created
    var updated by Votes.updated
}

object Votes : UUIDTable("core.votes") {
    val billId = uuid("billId")
    val userId = uuid("userId")
    val voteDetailId = uuid("voteDetailId").nullable()
    val created = timestamp("created")
    val updated = timestamp("updated")
}
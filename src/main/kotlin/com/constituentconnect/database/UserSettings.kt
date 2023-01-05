package com.constituentconnect.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

class UserSettingEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserSettingEntity>(UserSettings)

    var userID by UserSettings.userID
    var voteTextNotificationEnabled by UserSettings.voteTextNotificationEnabled
    var twitterVotePostEnabled by UserSettings.twitterVotePostEnabled
    var created by UserSettings.created
    var updated by UserSettings.updated
}

object UserSettings : IntIdTable("core.user-settings") {
    val userID = integer("userId")
    val voteTextNotificationEnabled = bool("voteTextNotificationEnabled").default(false)
    val twitterVotePostEnabled = bool("twitterVotePostEnabled").default(false)
    val created = timestamp("created").default(Instant.now())
    val updated = timestamp("updated").default(Instant.now())
}

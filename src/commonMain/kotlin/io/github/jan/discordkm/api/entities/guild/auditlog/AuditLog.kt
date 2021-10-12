package io.github.jan.discordkm.api.entities.guild.auditlog

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.lists.UserList
import io.github.jan.discordkm.api.webhooks.Webhook
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class AuditLog(override val data: JsonObject, override val guild: Guild) : GuildEntity {

    /**
     * A list of users in this audit log
     */
    val users = UserList(client, data.getValue("users").jsonArray.map { UserData(client, it.jsonObject) as User }.associateBy { it.id })

    /**
     * A list of threads in this audit log
     */
    val threads = ThreadList(data.getValue("threads").jsonArray.map { ThreadData(guild, it.jsonObject) }.associateBy { it.id })

    /**
     * A list of webhooks in this audit log
     */
    val webhooks = data.getValue("webhooks").jsonArray.map { Webhook(client, it.jsonObject) }

    /**
     * A list of audit log entries
     */
    val entries = data.getValue("audit_log_entries").jsonArray.map { Json.decodeFromJsonElement<AuditLogEntry>(it.jsonObject) }

    //integrations

}
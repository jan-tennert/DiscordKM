/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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
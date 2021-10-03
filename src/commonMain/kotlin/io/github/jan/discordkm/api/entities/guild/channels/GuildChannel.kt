/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.minutes
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.api.webhooks.Webhook
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.Channel
import io.github.jan.discordkm.internal.entities.channels.IParent
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface GuildChannel : Channel, Reference<GuildChannel>, GuildEntity, Nameable {

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.channels[id]!!

    override val client: Client
        get() = guild.client

    override val id: Snowflake
        get() = data.getId()

    /**
     * The position of the channel in the hierarchy
     * Can be null if this guild channel is a [Thread]
     */
    val position: Int?
        get() = data.getOrNull<Int>("position")

    /**
     * The name of the channel
     */
    override val name: String
        get() = data.getOrThrow<String>("name")


    /**
     * The [PermissionOverride]s for this guild channel
     */
    val permissionOverrides: Set<PermissionOverride>

    override suspend fun retrieve() = guild.channels.retrieve(id) as GuildChannel

    //permission overrides
}

interface GuildMessageChannel : GuildChannel, MessageChannel {

    /**
     * Deletes multiple messages in this channel
     */
    suspend fun deleteMessages(ids: Iterable<Snowflake>) = client.buildRestAction<Unit> {
        route = Route.Message.BULK_DELETE(id.string).post(buildJsonObject {
            putJsonArray("messages") {
                ids.forEach { add(it.long) }
            }
        })
        transform {  }
        //TODO: Check permissions
    }

    /**
     * Creates a webhook in this channel
     */
    suspend fun createWebhook(name: String, avatar: Image? = null) = client.buildRestAction<Webhook> {
        route = Route.Webhook.CREATE_WEBHOOK(id).post(buildJsonObject {
            put("name", name)
            putOptional("avatar", avatar?.encodedData)
        })
        transform { Webhook(client, it.toJsonObject()) }
    }

    /**
     * Retrieves all webhooks in this channel
     */
    suspend fun retrieveWebhooks() = client.buildRestAction<List<Webhook>> {
        route = Route.Webhook.GET_CHANNEL_WEBHOOKS(id).get()
        transform { it.toJsonArray().map { Webhook(client, it.jsonObject) } }
    }

}

interface GuildTextChannel : GuildMessageChannel, Invitable, IParent {

    /**
     * The topic of the channel
     */
    val topic: String?
        get() = data.getOrNull<String>("topic")

    /**
     * The default time after threads get achieved in this channel
     */
    val defaultAutoArchiveDuration: Thread.ThreadDuration
        get() = if(data["default_auto_archive_duration"] != null) Thread.ThreadDuration.raw(data.getValue("default_auto_archive_duration").jsonPrimitive.int.minutes) else Thread.ThreadDuration.HOUR

    /**
     * Whether this channel is nsfw or not
     */
    val isNSFW: Boolean
        @get:JvmName("isNSFW")
        get() = data.getOrDefault("nsfw", false)

    /**
     * Returns a list of all [Thread]s in this channel
     */
    val threads
        get() = ThreadList(guild.threads.filter { it.parentId == id }.associateBy { it.id })

    /**
     * Retrieves all public achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    suspend fun retrievePublicArchivedThreads(limit: Int? = null, before: DateTimeTz? = null): List<Thread>

    /**
     * Retrieves all private achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    suspend fun retrievePrivateArchivedThreads(limit: Int? = null, before: DateTimeTz? = null): List<Thread>

    /**
     * Retrieves all joined private archived threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this id
     */

    suspend fun retrieveJoinedPrivateArchivedThreads(limit: Int? = null, before: Snowflake? = null): List<Thread>

}
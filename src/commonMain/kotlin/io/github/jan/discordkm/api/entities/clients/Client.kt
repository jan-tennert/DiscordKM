/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CommandContainer
import io.github.jan.discordkm.api.entities.containers.UserContainer
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.github.jan.discordkm.internal.caching.ClientCacheManager
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.Requester
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.SerializableEnum
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.serialization.serializers.UserSerializer
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.utils.safeValues
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.Encoding
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

sealed class Client(
    val config: ClientConfig
) : CoroutineScope, CommandHolder, BaseEntity {

    val requester = Requester(config)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    override val client: Client get() = this
    val cacheManager = ClientCacheManager(this)

    val mutex = Mutex()

    lateinit var selfUser: UserCacheEntry
        internal set

    override val commands: CommandContainer
        get() = CommandContainer(this, "/applications/${selfUser.id}/commands")
    val guilds: CacheGuildContainer
        get() = CacheGuildContainer(this, cacheManager.guildCache.safeValues)
    val channels: CacheChannelContainer
        get() = CacheChannelContainer(cacheManager.guildCache.safeValues.map { it.channels.values }.flatten())
    val members: CacheMemberContainer
        get() = CacheMemberContainer(cacheManager.guildCache.safeValues.map { it.members.values }.flatten())
    val users: UserContainer
        get() = UserContainer(this, members.map(MemberCacheEntry::user).distinctBy { it.id.long })
    val threads: CacheThreadContainer
        get() = CacheThreadContainer(cacheManager.guildCache.safeValues.map { it.threads.values }.flatten())

    /**
     * Retrieves a guild template
     */
    suspend fun retrieveGuildTemplate(id: Snowflake) = buildRestAction<GuildTemplate> {
        route = Route.Template.GET_GUILD_TEMPLATE(id).get()
        transform { GuildSerializer.deserializeGuildTemplate(it.toJsonObject(), this@Client) }
    }

    /**
     * Edits the bot's user
     */
    suspend fun modifySelfUser(builder: suspend SelfUserEdit.() -> Unit) = buildRestAction<UserCacheEntry> {
        val edit = SelfUserEdit()
        edit.builder()
        route = Route.User.MODIFY_SELF_USER.patch(buildJsonObject {
            edit.username?.let { put("username", it) }
            edit.image?.let { put("image", it.encodedData) }
        })
        transform { UserSerializer.deserialize(it.toJsonObject(), this@Client) }
        onFinish {
            client.mutex.withLock {
                client.selfUser = it
            }
        }
    }

    /**
     * Edits the bot's user
     */
    suspend fun modifySelfUser(username: String, image: Image? = null) = modifySelfUser {
        this.username = username
        this.image = image
    }

    /**
     * Disconnects the bot from the rest api and the websocket
     */
    abstract suspend fun disconnect()

    /**
     * Starts the requester
     */
    abstract suspend fun login()

    data class SelfUserEdit(var username: String? = null, var image: Image? = null)


}

/**
 * The intents specify which events you should receive. For example if you don't use VoiceStates remove the [Intent.GUILD_VOICE_STATES] intent
 */
enum class Intent(override val offset: Int) : SerializableEnum<Intent> {

    GUILDS(0),
    GUILD_MEMBERS(1),
    GUILD_BANS(2),
    GUILD_EMOJIS_AND_STICKERS(3),
    GUILD_INTEGRATIONS(4),
    GUILD_WEBHOOKS(5),
    GUILD_INVITES(6),
    GUILD_VOICE_STATES(7),
    GUILD_PRESENCES(8),
    GUILD_MESSAGES(9),
    GUILD_MESSAGE_REACTIONS(10),
    GUILD_MESSAGE_TYPING(11),
    DIRECT_MESSAGES(12),
    DIRECT_MESSAGE_REACTIONS(13),
    DIRECT_MESSAGE_TYPING(14),
    GUILD_SCHEDULED_EVENTS(16);

    companion object : FlagSerializer<Intent>(values())

}

data class ClientConfig(
    val token: String,
    val intents: Set<Intent> = emptySet(),
    val logging: LoggerConfig,
    val enabledCache: Set<CacheFlag> = emptySet(),
    val httpClientConfig: HttpClientConfig<*>.() -> Unit,
    val totalShards: Int = -1,
    val shards: Set<Int> = emptySet(),
    val reconnectDelay: TimeSpan = 5.seconds,
    val activity: Presence? = null,
    val status: PresenceStatus = PresenceStatus.ONLINE,
    val encoding: Encoding = Encoding.JSON,
    val compression: Compression = Compression.NONE,
)






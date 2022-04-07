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
import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CacheUserContainer
import io.github.jan.discordkm.api.entities.containers.CommandContainer
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheFlag
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
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.Encoding
import io.ktor.client.HttpClientConfig
import kotlinx.serialization.json.buildJsonObject

interface DiscordClient : CommandHolder, BaseEntity {

    override val client get() = this
    override val commands: CommandContainer
        get() = CommandContainer(this, "/applications/${selfUser.id}/commands")
    val config: ClientConfig
    val selfUser: User
    val guilds: CacheGuildContainer
    val users: CacheUserContainer
    val channels: CacheChannelContainer
    val members: CacheMemberContainer
    val threads: CacheThreadContainer
    val requester: Requester


    /**
     * Retrieves a guild template
     */
    suspend fun retrieveGuildTemplate(id: Snowflake) = buildRestAction<GuildTemplate> {
        route = Route.Template.GET_GUILD_TEMPLATE(id).get()
        transform { GuildSerializer.deserializeGuildTemplate(it.toJsonObject(), this@DiscordClient) }
    }

    /**
     * Edits the bot's user
     */
    suspend fun modifySelfUser(username: String, image: Image?) = buildRestAction<UserCacheEntry> {
        route = Route.User.MODIFY_SELF_USER.patch(buildJsonObject {
            putOptional("username", username)
            putOptional("image", image?.encodedData)
        })
        transform { UserSerializer.deserialize(it.toJsonObject(), this@DiscordClient) }
    }

    fun textFor(locale: DiscordLocale, key: String, vararg args: Any) = config.translationManager.get(locale, key, *args)

    suspend fun login()

    suspend fun disconnect()

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
    MESSAGE_CONTENT(15),
    GUILD_SCHEDULED_EVENTS(16);

    companion object : FlagSerializer<Intent>(values())

}

open class ClientConfig(
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
    val maxResumeTries: Int = 3,
    val translationManager: TranslationManager = TranslationManager.empty(),
)






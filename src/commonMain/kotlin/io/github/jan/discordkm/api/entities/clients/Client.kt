/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CommandContainer
import io.github.jan.discordkm.api.entities.containers.UserContainer
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.ClientCacheManager
import io.github.jan.discordkm.internal.entities.guilds.templates.GuildTemplateData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.RestClient
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.SerializableEnum
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.safeValues
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

sealed class Client(val token: String, val loggingLevel: Logger.Level) : CoroutineScope, CommandHolder, BaseEntity {

    val rest = RestClient(this)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    override val client = this
    val cacheManager = ClientCacheManager()

    lateinit var selfUser: User
        internal set

    override val commands: CommandContainer
        get() = CommandContainer(this, "/applications/${selfUser.id}/commands")
    val guilds: CacheGuildContainer
        get() = CacheGuildContainer(this, cacheManager.guildCache.toMap().values)
    val channels: CacheChannelContainer
        get() = CacheChannelContainer(cacheManager.guildCache.safeValues.map { it.channels.values }.flatten())
    val members: CacheMemberContainer
        get() = CacheMemberContainer(cacheManager.guildCache.safeValues.map { it.members.values }.flatten())

    val users: UserContainer
        get() = UserContainer(this, cacheManager.userCache.safeValues)
    val threads: CacheThreadContainer
        get() = CacheThreadContainer(cacheManager.guildCache.safeValues.map { it.threads.values }.flatten())

    /**
     * Retrieves a guild template
     */
    suspend fun retrieveGuildTemplate(id: Snowflake) = buildRestAction<GuildTemplate> {
        route = Route.Template.GET_GUILD_TEMPLATE(id).get()
        transform { GuildTemplateData(this@Client, it.toJsonObject()) }
    }

    /**
     * Edits the bot's user
     */
    suspend fun editSelfUser(builder: suspend SelfUserEdit.() -> Unit) = buildRestAction<User> {
        val edit = SelfUserEdit()
        edit.builder()
        route = Route.User.MODIFY_SELF_USER.patch(buildJsonObject {
            edit.username?.let { put("username", it) }
            edit.image?.let { put("image", it.encodedData) }
        })
        transform { it.toJsonObject().extractClientEntity(this@Client) }
        onFinish { selfUser = it }
    }

    /**
     * Edits the bot's user
     */
    suspend fun editSelfUser(username: String, image: Image? = null) = editSelfUser {
        this.username = username
        this.image = image
    }

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
    DIRECT_MESSAGE_TYPING(14);

    companion object : FlagSerializer<Intent>(values())

}






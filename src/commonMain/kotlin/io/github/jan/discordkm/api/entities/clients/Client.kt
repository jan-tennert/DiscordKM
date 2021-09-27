/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.SerializableEnum
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.ChannelList
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.api.entities.lists.GuildList
import io.github.jan.discordkm.api.entities.lists.MemberList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.lists.UserList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.guilds.templates.GuildTemplateData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.media.Image
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.RestClient
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

sealed class Client(val token: String, val loggingLevel: Logger.Level) : CoroutineScope, CommandHolder, BaseEntity {

    internal var guildCache = Cache<Guild>()
    val rest = RestClient(this)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    override val client = this

    override val commandCache = Cache<ApplicationCommand>()

    override val commands: CommandList
        get() = CommandList("/applications/${selfUser.id}/commands", this, commandCache.values)

    lateinit var selfUser: User
        internal set
    val guilds: GuildList
        get() = GuildList(this, guildCache.values.toList())
    val channels: ChannelList
        get() = ChannelList(this, guilds.map { it.channels.internalList }.flatten())
    val members: MemberList
        get() = MemberList(guilds.map { it.members.internalList }.flatten())
    internal val userCache
        get() = Cache(IsoMutableMap { members.map { it.user }.associateBy { it.id }.toMutableMap()})
    val users: UserList
        get() = UserList(this, userCache.values.toList())
    val threads: ThreadList
        get() = ThreadList(guilds.map { it.threads.internalList }.flatten())

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

    suspend fun retrieveRTCRegions() = buildRestAction<String> {
        route = Route.Voice.GET_VOICE_REGIONS.get()
        transform { it }
    }

    suspend fun retrieveInvite(code: String) = client.buildRestAction<Invite> {
        route = Route.Invite.GET_INVITE(code).get()
        transform { Invite(client, it.toJsonObject()) }
    }

    data class SelfUserEdit(var username: String? = null, var image: Image? = null)


}

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

    companion object : EnumSerializer<Intent> {
        override val values = values().toList()
    }

}






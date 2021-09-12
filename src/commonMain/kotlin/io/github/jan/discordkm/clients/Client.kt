/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.clients

import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.entities.EnumSerializer
import io.github.jan.discordkm.entities.SerializableEnum
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.lists.ChannelList
import io.github.jan.discordkm.entities.lists.GuildList
import io.github.jan.discordkm.entities.lists.MemberList
import io.github.jan.discordkm.entities.lists.ThreadList
import io.github.jan.discordkm.entities.lists.UserList
import io.github.jan.discordkm.entities.misc.Image
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.RestClient
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

sealed class Client(val token: String, val loggingLevel: Logger.Level) : CoroutineScope {

    internal var guildCache = Cache<Guild>()
    val rest = RestClient(this)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    lateinit var selfUser: User
        internal set
    val guilds: GuildList
        get() = GuildList(this, guildCache.values.toList())
    val channels: ChannelList
        get() = ChannelList(guilds.map { it.channels.internalList }.flatten())
    val members: MemberList
        get() = MemberList(guilds.map { it.members.internalList }.flatten())
    internal val userCache
        get() = Cache<User>(IsoMutableMap { members.map { it.user }.associateBy { it.id }.toMutableMap()})
    val users: UserList
        get() = UserList(this, userCache.values.toList())
    val threads: ThreadList
        get() = ThreadList(guilds.map { it.threads.internalList }.flatten())

    suspend fun editSelfUser(builder: suspend SelfUserEdit.() -> Unit) = buildRestAction<User> {
        val edit = SelfUserEdit()
        edit.builder()
        action = RestAction.Action.patch("/users/@me", buildJsonObject {
            edit.username?.let { put("username", it) }
            edit.image?.let { put("image", it.url) }
        })
        transform { it.toJsonObject().extractClientEntity(this@Client) }
        onFinish { selfUser = it }
    }

    suspend fun retrieveRTCRegions() = buildRestAction<String> {
        action = RestAction.Action.get("/voice/regions")
        transform { it }
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






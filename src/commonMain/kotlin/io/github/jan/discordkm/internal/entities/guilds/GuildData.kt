package io.github.jan.discordkm.internal.entities.guilds

import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.VoiceState
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.api.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildData(override val client: Client, override val data: JsonObject) : Guild {

    @PublishedApi
    internal var roleCache = Cache.fromSnowflakeEntityList(data.getValue("roles").jsonArray.map { it.jsonObject.extractGuildEntity<Role>(this) })
    @PublishedApi
    internal var memberCache = Cache.fromSnowflakeEntityList(data.getValue("members").jsonArray.map { it.jsonObject.extractGuildEntity<Member>(this) })
    @PublishedApi
    internal var channelCache = Cache.fromSnowflakeEntityList(data.getValue("channels").jsonArray.map { json ->
        when (ChannelType.values().first { it.id == json.jsonObject.getOrThrow<Int>("type") }) {
            ChannelType.GUILD_TEXT -> json.jsonObject.extractGuildEntity<TextChannel>(this)
            ChannelType.GUILD_VOICE -> json.jsonObject.extractGuildEntity<VoiceChannel>(this)
            ChannelType.GUILD_CATEGORY -> json.jsonObject.extractGuildEntity<Category>(this)
            ChannelType.GUILD_NEWS -> json.jsonObject.extractGuildEntity<NewsChannel>(this)
            ChannelType.GUILD_STORE -> TODO()
            ChannelType.GUILD_STAGE_VOICE -> json.jsonObject.extractGuildEntity<StageChannel>(this)
            else -> throw IllegalStateException()
        }
    })
    @PublishedApi
    internal var threadCache = Cache.fromSnowflakeEntityList(data.getValue("threads").jsonArray.map { ThreadData(this, it.jsonObject) as Thread })

    override val roles: RoleList
        get() = RoleList(this, roleCache.values.toList())

    override val voiceStates: MutableList<VoiceState>
        get() = super.voiceStates.toMutableList()

    override val members
        get() = RetrievableMemberList(this, memberCache.values.toList())

    override val channels: RetrievableChannelList
        get() = RetrievableChannelList(this, channelCache.values)

    override val threads: ThreadList
        get() = ThreadList(threadCache.values)

    override suspend fun retrieveActiveThreads() = client.buildRestAction<List<Thread>> {
        action = RestAction.get("/guilds/${id}/threads/active")
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(this@GuildData, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> threadCache[thread.id] = thread } }
    }

    override suspend fun retrieveBans() = client.buildRestAction<List<Guild.Ban>> {
        action = RestAction.get("/guilds/${id}/bans")
        transform { it.toJsonArray().map { ban -> Guild.Ban(this@GuildData, ban.jsonObject) }}
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild") }
    }


    override fun toString() = "Guild[id=$id,name=$name]"

    override fun equals(other: Any?): Boolean {
        if(other !is Guild) return false
        return other.id == id
    }


}
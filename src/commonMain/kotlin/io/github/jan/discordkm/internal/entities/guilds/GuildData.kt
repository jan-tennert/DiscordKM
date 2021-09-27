package io.github.jan.discordkm.internal.entities.guilds

import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordClient
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
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.api.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.api.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.entities.guilds.templates.GuildTemplateData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class GuildData(override val client: Client, override val data: JsonObject) : Guild, CommandHolder {

    @PublishedApi
    internal var roleCache = Cache.fromSnowflakeEntityList((data["roles"]?.jsonArray ?: buildJsonArray {  }).map { it.jsonObject.extractGuildEntity<Role>(this) })
    @PublishedApi
    internal var memberCache = Cache.fromSnowflakeEntityList((data["members"]?.jsonArray ?: buildJsonArray {  }).map { it.jsonObject.extractGuildEntity<Member>(this) })
    @PublishedApi
    internal var channelCache = Cache.fromSnowflakeEntityList((data["channels"]?.jsonArray ?: buildJsonArray {  }).map { json ->
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
    internal var threadCache = Cache.fromSnowflakeEntityList((data["threads"]?.jsonArray ?: buildJsonArray {  }).map { ThreadData(this, it.jsonObject) as Thread })

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
        route = Route.Thread.GET_ACTIVE_THREADS(id).get()
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(this@GuildData, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> threadCache[thread.id] = thread } }
    }

    override suspend fun retrieveBans() = client.buildRestAction<List<Guild.Ban>> {
        route = Route.Ban.GET_BANS(id).get()
        transform { it.toJsonArray().map { ban -> Guild.Ban(this@GuildData, ban.jsonObject) }}
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild") }
    }

    override suspend fun leaveVoiceChannel() = if(client is DiscordClient) {
        (client as DiscordClient).gateway.send(UpdateVoiceStatePayload(id, null, selfMember.isMuted, selfMember.isDeafened))
    } else {
        throw UnsupportedOperationException("You can't leave a voice channel without having a gateway connection!")
    }

    override suspend fun retrieveBan(userId: Snowflake) = client.buildRestAction<Guild.Ban> {
        route = Route.Ban.GET_BAN(id, userId).get()
        transform { Guild.Ban(this@GuildData, it.toJsonObject()) }
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild") }
    }

    override suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.User.LEAVE_GUILD(id).delete()
        onFinish {
            client.guildCache.remove(id)
        }
    }

    override suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Guild.DELETE_GUILD(id).delete()
        transform {  }
    }

    override suspend fun createInvite(channelId: Snowflake, builder: InviteBuilder.() -> Unit) = client.buildRestAction<Invite> {
        route = Route.Invite.CREATE_CHANNEL_INVITE(channelId).post(Json.encodeToJsonElement(InviteBuilder().apply(builder).build()))
        transform { Invite(client, it.toJsonObject()) }
    }

    override suspend fun retrieveTemplates() = client.buildRestAction<List<GuildTemplate>> {
        route = Route.Template.GET_GUILD_TEMPLATES(id).get()
        transform { it.toJsonArray().map { GuildTemplateData(client, it.jsonObject) } }
    }

    override suspend fun createTemplate(name: String, description: String?) = client.buildRestAction<GuildTemplate> {
        route = Route.Template.CREATE_GUILD_TEMPLATE(id).post(buildJsonObject {
            put("name", name)
            putOptional("description", description)
        })
        transform { GuildTemplateData(client, it.toJsonObject()) }
    }

    override fun toString() = "Guild[id=$id,name=$name]"

    override val commandCache = Cache<ApplicationCommand>()

    override val commands: CommandList
        get() = CommandList("/applications/${client.selfUser.id}/guilds/${id}/commands", this, commandCache.values)

    override fun equals(other: Any?): Boolean {
        if(other !is Guild) return false
        return other.id == id
    }


}
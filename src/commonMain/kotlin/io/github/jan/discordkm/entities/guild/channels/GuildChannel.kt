package io.github.jan.discordkm.entities.guild.channels

import com.soywiz.klock.minutes
import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.Channel
import io.github.jan.discordkm.entities.channels.ChannelType
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.guild.invites.Invite
import io.github.jan.discordkm.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonArray
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

sealed class GuildChannel(val guild: Guild, final override val data: JsonObject) : Channel, Reference<GuildChannel> {

    override fun getValue(ref: Any?, property: KProperty<*>): GuildChannel {
        TODO("Not yet implemented")
    }

    override val client = guild.client

    final override val id = data.getId()

    val position = data.getOrThrow<Int>("position")

    val name = data.getOrThrow<String>("name")

    val parentId = data.getOrNull<Snowflake>("parent_id")

//    val parent = guild.channels[parentId ?: Snowflake.empty()] as? Category

    /**
     * The [PermissionOverride]s for this guild channel
     */
    val permissionOverrides = data["permission_overwrites"]?.jsonArray?.map {
        val holder = when(it.jsonObject.getOrThrow<Int>("type")) {
            0 -> guild.roles[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            1 -> guild.members[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            else -> throw IllegalStateException()
        }
        val allow = it.jsonObject.getEnums("allow", Permission)
        val deny = it.jsonObject.getEnums("deny", Permission)
        PermissionOverride(holder, allow, deny)
    }?.toSet() ?: emptySet()

    override suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
        onFinish { guild.channelCache.remove(id) }
    }

    suspend fun createInvite(builder: InviteBuilder.() -> Unit = {}) = client.buildRestAction<Invite> {
        val inviteBuilder = InviteBuilder().apply(builder)
        if(inviteBuilder.target != null && type !in listOf(ChannelType.GUILD_STAGE_VOICE, ChannelType.GUILD_VOICE)) throw IllegalArgumentException("You can't add targets to text channel invites!")
        action = RestAction.Action.post("/channels/$id/invites", Json.encodeToString(inviteBuilder.build()))
        transform { it.toJsonObject().extractClientEntity(client) }
        //TODO: check permission
    }

    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        action = RestAction.Action.get("/channels/$id/invites")
        transform { json -> json.toJsonArray().map { it.jsonObject.extractClientEntity(client) } }
    }

    //retrieve parent?

    //permission overrides
}

sealed class GuildTextChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data), MessageChannel {

    val topic = data.getOrNull<String>("topic")
    val defaultAutoArchiveDuration = if(data["default_auto_archive_duration"] != null) Thread.ThreadDuration.raw(data.getValue("default_auto_archive_duration").jsonPrimitive.int.minutes) else Thread.ThreadDuration.ZERO
    @get:JvmName("isNSFW")
    val isNSFW = data.getOrDefault("nsfw", false)

    suspend fun deleteMessages(ids: Iterable<Snowflake>) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/channels/$id/messages/bulk-delete", buildJsonObject {
            putJsonArray("messages") {
                ids.forEach { add(it.long) }
            }
        })
        transform {  }
        //TODO: Check permissions
    }

}
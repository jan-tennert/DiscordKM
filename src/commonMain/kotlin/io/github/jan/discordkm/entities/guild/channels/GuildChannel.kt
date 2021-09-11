package io.github.jan.discordkm.entities.guild.channels

import com.soywiz.klock.minutes
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.channels.Channel
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
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

    val parentId = data.getOrNull<Long>("parent_id")

    override suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
        onFinish { guild.channelCache.remove(id) }
    }

    //retrieve parent?

    //permission overrides
}

sealed class GuildTextChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data), MessageChannel {

    val topic = data.getOrNull<String>("topic")
    val defaultAutoArchiveDuration = if(data["default_auto_archive_duration"] != null) Thread.ThreadDuration.raw(data.getValue("default_auto_archive_duration").jsonPrimitive.int.minutes) else Thread.ThreadDuration.ZERO
    @get:JvmName("isNSFW")
    val isNSFW = data.getOrDefault("nsfw", false)

}
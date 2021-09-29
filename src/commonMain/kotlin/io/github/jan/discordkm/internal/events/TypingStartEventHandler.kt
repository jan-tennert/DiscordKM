package io.github.jan.discordkm.internal.events

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.TypingStartEvent
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class TypingStartEventHandler(val client: Client) : InternalEventHandler<TypingStartEvent> {

    override fun handle(data: JsonObject): TypingStartEvent {
        val user = client.users[data.getOrThrow<Snowflake>("user_id")]!!
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val member = guild?.members?.get(user.id)
        val channel = (client.channels[data.getOrThrow<Snowflake>("channel_id")] ?: PrivateChannel.fromId(client, data.getOrThrow("channel_id"))) as MessageChannel
        val timestamp = DateTimeTz.Companion.fromUnixLocal(data.getOrThrow<Long>("timestamp"))
        return TypingStartEvent(channel, guild, user, member, timestamp)
    }

}
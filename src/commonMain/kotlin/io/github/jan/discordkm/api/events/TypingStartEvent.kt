package io.github.jan.discordkm.api.events

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.internal.entities.channels.MessageChannel

/**
 * Sent when a user starts typing in a channel
 *
 * Requires the intent [Intent.GUILD_MESSAGE_TYPING] or [Intent.DIRECT_MESSAGE_TYPING]
 */
class TypingStartEvent(val channel: MessageChannel, val guild: Guild?, val user: User, val member: Member?, val timestamp: DateTimeTz) : Event {

    override val client: Client
        get() = channel.client

}
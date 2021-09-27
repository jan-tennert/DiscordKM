package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Guild

sealed interface BanEvent : GuildEvent {

    val user: User

}

/**
 * Sent when a guild member gets banned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanAddEvent(override val guild: Guild, override val user: User) :
    BanEvent

/**
 * Sent when a user gets unbanned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanRemoveEvent(override val guild: Guild, override val user: User) :
    BanEvent
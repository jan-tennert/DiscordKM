package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.clients.Intent

sealed interface BanEvent : GuildEvent {

    override val guildId: Snowflake
    val user: User

}

/**
 * Sent when a guild member gets banned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanAddEvent(override val client: Client, override val guildId: Snowflake, override val user: User) : BanEvent

/**
 * Sent when a user gets unbanned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanRemoveEvent(override val client: Client, override val guildId: Snowflake, override val user: User) : BanEvent
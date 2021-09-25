package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.internal.entities.UserData

sealed interface BanEvent : GuildEvent {

    override val guildId: Snowflake
    val user: UserData

}

/**
 * Sent when a guild member gets banned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanAddEvent(override val client: Client, override val guildId: Snowflake, override val user: UserData) :
    BanEvent

/**
 * Sent when a user gets unbanned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanRemoveEvent(override val client: Client, override val guildId: Snowflake, override val user: UserData) :
    BanEvent
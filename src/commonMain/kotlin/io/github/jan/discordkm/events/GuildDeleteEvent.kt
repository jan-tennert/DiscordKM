package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake

/**
 * Sent when the bot was removed from a guild or the guild is unavailable due to an outage
 *
 * Requires the intent [Intent.GUILDS]
 */
class GuildDeleteEvent(override val client: Client, val id: Snowflake) : Event
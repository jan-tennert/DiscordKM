package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake

/**
 * Sent when the bot was removed from a guild or the guild is unavailable due to an outage
 *
 * Requires the intent [Intent.GUILDS]
 */
class GuildDeleteEvent(override val client: Client, val id: Snowflake) : Event
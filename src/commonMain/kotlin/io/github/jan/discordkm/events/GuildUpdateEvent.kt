package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.guild.Guild

/**
 * Sent when the guild was updated
 *
 * Requires the intent [Intent.GUILDS]
 */
class GuildUpdateEvent(override val client: Client, val guild: Guild) : Event
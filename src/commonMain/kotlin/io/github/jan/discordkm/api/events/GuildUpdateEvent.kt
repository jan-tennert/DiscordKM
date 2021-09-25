package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild

/**
 * Sent when the guild was updated
 *
 * Requires the intent [Intent.GUILDS]
 */
class GuildUpdateEvent(override val client: Client, val guild: Guild) : Event
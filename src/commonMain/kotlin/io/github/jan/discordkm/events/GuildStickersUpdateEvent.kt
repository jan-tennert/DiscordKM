package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.clients.Intent
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Sticker

/**
 * Sent when the guild stickers get updated
 *
 * Requires the intent [Intent.GUILD_EMOJIS_AND_STICKERS]
 */
class GuildStickersUpdateEvent(override val client: Client, override val guildId: Snowflake, val stickers: List<Sticker>) : GuildEvent
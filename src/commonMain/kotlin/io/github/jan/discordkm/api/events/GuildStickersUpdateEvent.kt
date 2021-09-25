package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Sticker

/**
 * Sent when the guild stickers get updated
 *
 * Requires the intent [Intent.GUILD_EMOJIS_AND_STICKERS]
 */
class GuildStickersUpdateEvent(override val client: Client, override val guildId: Snowflake, val stickers: List<Sticker>) :
    GuildEvent
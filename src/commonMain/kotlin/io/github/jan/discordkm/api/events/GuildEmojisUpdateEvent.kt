package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Emoji

/**
 * Sent when the guild emotes get updated
 *
 * Requires the intent [Intent.GUILD_EMOJIS_AND_STICKERS]
 */
class GuildEmojisUpdateEvent(override val client: Client, override val guildId: Snowflake, val emotes: List<Emoji.Emote>) :
    GuildEvent
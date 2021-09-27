package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild

/**
 * Sent when the guild emotes get updated
 *
 * Requires the intent [Intent.GUILD_EMOJIS_AND_STICKERS]
 */
class GuildEmojisUpdateEvent(
    override val guild: Guild,
    val emotes: List<Emoji.Emote>
) :
    GuildEvent
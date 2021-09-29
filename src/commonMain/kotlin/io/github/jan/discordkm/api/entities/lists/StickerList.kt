package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Sticker

class StickerList(override val internalMap: Map<Snowflake, Sticker>) : NameableSnowflakeList<Sticker>

class EmojiList(override val internalMap: Map<Snowflake, Emoji.Emote>) : NameableSnowflakeList<Emoji.Emote>
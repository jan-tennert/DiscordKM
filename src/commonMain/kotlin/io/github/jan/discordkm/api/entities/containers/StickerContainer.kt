package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.sticker.Sticker
import io.github.jan.discordkm.api.entities.modifiers.guild.StickerModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class StickerContainer(val guild: Guild) {

    /**
     * Retrieves all stickers in the guild.
     */
    suspend fun retrieveStickers() = guild.client.buildRestAction<List<Sticker>> {
        route = Route.Sticker.GET_GUILD_STICKERS(guild.id).get()
        transform { it.toJsonArray().map { item -> GuildSerializer.deserializeSticker(item.jsonObject, guild) } }
    }

    /**
     * Retrieves a sticker by its ID.
     */
    suspend fun retrieveSticker(id: Snowflake) = guild.client.buildRestAction<Sticker> {
        route = Route.Sticker.GET_GUILD_STICKER(guild.id, id).get()
        transform { GuildSerializer.deserializeSticker(it.toJsonObject(), guild) }
    }

    /**
     * Creates a new sticker.
     */
    suspend fun create(builder: StickerModifier.() -> Unit) = guild.client.buildRestAction<Sticker> {
        route = Route.Sticker.CREATE_GUILD_STICKER(guild.id).post(StickerModifier(false).apply(builder).data)
        transform { GuildSerializer.deserializeSticker(it.toJsonObject(), guild) }
    }

}

class CacheStickerContainer(guild: Guild, override val values: Collection<Sticker>) : StickerContainer(guild), SnowflakeContainer<Sticker>
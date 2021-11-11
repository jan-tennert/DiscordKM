package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.commands.ChannelTypeSerializer
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Serializable

interface Channel : SnowflakeEntity, BaseEntity, Mentionable {

    override val asMention: String
        get() = "<#$id>"

    val type: ChannelType

    /**
     * Deletes this channel
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Channel.DELETE_CHANNEL(id).delete()
    }

    fun <T : Channel>fromCache() = client.channels //TODO: Make list

}

interface ChannelCacheEntry : Channel

@Serializable(with = ChannelTypeSerializer::class)
enum class ChannelType(override val value: Int) : EnumWithValue<Int>{
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4),
    GUILD_NEWS(5),
    GUILD_STORE(6),
    GUILD_NEWS_THREAD(10),
    GUILD_PUBLIC_THREAD(11),
    GUILD_PRIVATE_THREAD(12),
    GUILD_STAGE_VOICE(13);

    companion object : EnumWithValueGetter<ChannelType, Int>(values())
}
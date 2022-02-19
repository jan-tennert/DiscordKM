package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.channels.guild.Category
import io.github.jan.discordkm.api.entities.channels.guild.NewsChannel
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.TextChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Serializable

interface Channel : SnowflakeEntity, BaseEntity, Mentionable, CacheEntity {

    override val asMention: String
        get() = "<#$id>"
    override val cache: ChannelCacheEntry?

    val type: ChannelType

    /**
     * Deletes this channel
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun delete(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Channel.DELETE_CHANNEL(id).delete()
        this.reason = reason
    }

    companion object {
        operator fun invoke(id: Snowflake, type: ChannelType, client: Client, guild: Guild? = null) = client.channels[id] ?: when (type) {
            ChannelType.GUILD_TEXT -> TextChannel(id, guild!!)
            ChannelType.GUILD_VOICE -> VoiceChannel(id, guild!!)
            ChannelType.GUILD_CATEGORY -> Category(id, guild!!)
            ChannelType.GUILD_NEWS -> NewsChannel(id, guild!!)
            ChannelType.GUILD_NEWS_THREAD -> Thread(id, guild!!, ChannelType.GUILD_NEWS_THREAD)
            ChannelType.GUILD_PUBLIC_THREAD -> Thread(id, guild!!, ChannelType.GUILD_PUBLIC_THREAD)
            ChannelType.GUILD_PRIVATE_THREAD -> Thread(id, guild!!, ChannelType.GUILD_PRIVATE_THREAD)
            ChannelType.GUILD_STAGE_VOICE -> StageChannel(id, guild!!)
            ChannelType.UNKNOWN -> client.channels[id]!!
            else -> throw IllegalArgumentException("Unknown channel type: $type")
        }
    }

}

interface ChannelCacheEntry : Channel, CacheEntry

@Serializable(with = ChannelType.Companion::class)
enum class ChannelType(override val value: Int) : EnumWithValue<Int>{
    UNKNOWN(-1),
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
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.caching.CacheEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RoleCacheEntry : Role, PermissionHolder, Nameable, CacheEntry {

    /**
     * The color of the role
     */
    val color: Color

    /**
     * Whether the role is pinned in the user list
     */
    val isHoist: Boolean

    /**
     * The icon appearing next to the roles name in the chat & in the user list
     */
    val iconHash: String?

    /**
     * The emoji appearing next to the roles name in the chat & in the user list
     */
    val unicodeEmoji: String?
    
    /**
     * The position of the role in the guild
     */
    val position: Int

    /**
     * Whether the role is managed by an integration
     */
    val isManagedByAnIntegration: Boolean

    /**
     * Whether the role can be mentioned in chat
     */
    val isMentionable: Boolean

    /**
     * The tags of the role
     */
    val tags: Tag?

    /**
     * @param botId The id of the bot the role belongs to
     * @param integrationId The id of the integration the role belongs to
     * @param isPremiumSubscriber Whether this is the guild's premium subscriber role
     */
    @Serializable
    data class Tag(
        @SerialName("bot_id") val botId: Snowflake? = null,
        @SerialName("integration_id") val integrationId: Snowflake? = null,
        @SerialName("premium_subscriber") val isPremiumSubscriber: Boolean? = null
    )

}

internal class RoleCacheEntryImpl(
    override val id: Snowflake,
    override val permissions: Set<Permission>,
    override val guild: Guild,
    override val name: String,
    override val color: Color,
    override val isHoist: Boolean,
    override val iconHash: String?,
    override val unicodeEmoji: String?,
    override val position: Int,
    override val isManagedByAnIntegration: Boolean,
    override val isMentionable: Boolean,
    override val tags: RoleCacheEntry.Tag?,
) : RoleCacheEntry {

    override val client: Client = guild.client

    override fun getPermissionsFor(channel: GuildChannelCacheEntry) = channel.permissionOverwrites.first { it.type == PermissionOverwrite.HolderType.ROLE && it.holderId == id }.allow

    override fun toString() = "RoleCacheEntry(id=$id, guildId= ${guild.id}, name=$name)"

    override fun equals(other: Any?): Boolean {
        if(other !is RoleCacheEntry && other is Role && other.id == id)
            return true
        if(other !is RoleCacheEntry)
            return false
        return other.id == id && other.guild.id == guild.id
    }

    override fun hashCode() = id.hashCode()

    override val type = PermissionOverwrite.HolderType.ROLE

}
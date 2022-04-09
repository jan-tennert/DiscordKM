/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.message

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.containers.CacheReactionContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.guild.sticker.StickerItem
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.ReactionCacheManager
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

interface MessageCacheEntry : CacheEntry, Message {

    /*
     * The user who sent this message
     */
    val author: User?

    /*
     * The [Member] object for the [author], if the message was sent in a guild
     */
    val member: Member?

    /*
     * Contents of the message
     */
    val content: String

    /*
     * The time this message was created
     */
    val timestamp: DateTimeTz

    /*
     * The time this message was edited
     */
    val editedTimestamp: DateTimeTz?

    /*
     * Whether this message is a tts (=text to speech) message
     */
    val isTTSMessage: Boolean

    /*
     * Whether this message mentions everyone
     */
    val mentionsEveryone: Boolean

    /*
     * The users that were mentioned in this message
     */
    val mentions: List<User>

    /*
     * The roles that were mentioned in this message
     */
    val mentionedRoles: List<Role>

    /*
     * The channels that were mentioned in this message
     */
    val mentionedChannels: List<GuildChannel>

    /*
     * The attachments in this message
     */
    val attachments: List<MessageAttachment>

    /*
     * The embeds in this message
     */
    val embeds: List<MessageEmbed>

    val nonce: String?

    /*
     * Whether this message is pinned
     */
    val isPinned: Boolean

    /*
     * The [MessageType] of this message
     */
    val type: MessageType

    /*
     * Sent with Rich-Presence related chat embeds
     */
    val activity: Activity?

    /*
     * The messages flags like crosspost etc.
     */
    val flags: Set<Message.Flag>

    /*
     * Stickers sent with this message
     */
    val stickers: List<StickerItem>

    /*
     * The message components of this message
     */
    val components: List<ActionRow>

    /*
     * If the message was sent in a thread this will be the [Thread] object
     */
    val thread: Thread?

    /*
     * Contains information about the interaction, if this is a interaction response
     */
    val interaction: Message.MessageInteraction?

    /*
     * Data showing the source of a crosspost, channel follow add, pin, or reply message
     */
    val reference: Message.Reference?

    /*
     * The message associated with the [reference]
     */
    val referencedMessage: MessageCacheEntry?

    /*
     * If this message was sent by a webhook, this is the id
     */
    val webhookId: Snowflake?

    /*
     * If this message was sent in a voice channel, this is the [VoiceChannel] object
     */
    @DiscordKMUnstable
    val channelAsVoiceChannel: VoiceChannel?

    override val reactions: CacheReactionContainer

}

internal class MessageCacheEntryImpl(
    override val id: Snowflake,
    override val guild: Guild? = null,
    override val channel: MessageChannel,
    override val author: User?,
    override val member: Member?,
    override val content: String,
    override val timestamp: DateTimeTz,
    override val editedTimestamp: DateTimeTz?,
    override val isTTSMessage: Boolean,
    override val mentionsEveryone: Boolean,
    override val mentions: List<User>,
    override val mentionedRoles: List<Role>,
    override val mentionedChannels: List<GuildChannel>,
    override val attachments: List<MessageAttachment>,
    override val embeds: List<MessageEmbed>,
    override val nonce: String?,
    override val isPinned: Boolean,
    override val type: MessageType,
    override val activity: Activity?,
    override val flags: Set<Message.Flag>,
    override val stickers: List<StickerItem>,
    override val components: List<ActionRow>,
    override val thread: Thread?,
    override val interaction: Message.MessageInteraction?,
    override val reference: Message.Reference?,
    override val referencedMessage: MessageCacheEntry?,
    override val webhookId: Snowflake?
) : MessageCacheEntry {

    val cacheManager = ReactionCacheManager(client)

    @DiscordKMUnstable
    override val channelAsVoiceChannel: VoiceChannel?
        get() = guild?.let { VoiceChannel(channel.id, it) }

    override val reactions: CacheReactionContainer
        get() = CacheReactionContainer(this, cacheManager.reactionCache.values.toList())

    @OptIn(DiscordKMInternal::class)
    fun copy() = DataMessage(
        content = content,
        tts = isTTSMessage,
        embeds = embeds,
        actionRows = components,
        reference = reference,
        stickerIds = stickers.map { it.id },
        oldAttachments = attachments,
        allowedMentions = AllowedMentions(),
    )

    override fun equals(other: Any?): Boolean {
        if(other !is MessageCacheEntry && other is Message && other.id == id)
            return true
        if(other !is MessageCacheEntry)
            return false
        return other.id == id && other.channel.id == channel.id
    }

    override fun hashCode() = id.hashCode()
    override fun toString() = "MessageCacheEntry(id=$id, channelId=${channel.id})"

}

enum class MessageType(override val value: Int) : EnumWithValue<Int> {
    DEFAULT(0),
    RECIPIENT_ADD(1),
    RECIPIENT_REMOVE(2),
    CALL(3),
    CHANNEL_NAME_CHANGE(4),
    CHANNEL_ICON_CHANGE(5),
    CHANNEL_PINNED_MESSAGE(6),
    GUILD_MEMBER_JOIN(7),
    USER_PREMIUM_GUILD_SUBSCRIPTION(8),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1(9),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2(10),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3(11),
    CHANNEL_FOLLOW_ADD(12),
    GUILD_DISCOVERY_DISQUALIFIED(14),
    GUILD_DISCOVERY_REQUALIFIED(15),
    GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING(16),
    GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING(17),
    THREAD_CREATED(18),
    REPLY(19),
    CHAT_INPUT_COMMAND(20),
    THREAD_STARTER_MESSAGE(21),
    GUILD_INVITE_REMINDER(22),
    CONTEXT_MENU_COMMAND(23);

    companion object : EnumWithValueGetter<MessageType, Int>(values())
}

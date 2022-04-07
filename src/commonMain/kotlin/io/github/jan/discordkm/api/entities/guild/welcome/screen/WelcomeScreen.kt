package io.github.jan.discordkm.api.entities.guild.welcome.screen

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The welcome screen which is shown, when a new user joins the guild
 * @param description The description of the welcome screen
 * @param channels The channels shown in the welcome screen
 * @see Channel
 */
data class WelcomeScreen(
    val description: String? = null,
    val channels: List<WelcomeScreenChannel> = emptyList()
) {

    /**
     * This is a welcome screen channels which is shown in the welcome screen to explain what this channel does
     * @param channelId The id of the channel
     * @param description The description shown on the welcome screen
     * @param emojiId The id of the emoji shown in the welcome screen
     * @param emojiName The name of the emoji shown in the welcome screen
     */

    data class WelcomeScreenChannel(
        val channel: GuildChannel,
        val description: String,
        val emoji: Emoji? = null
    )
}

class WelcomeScreenModifier(
    private val guild: Guild,
    var enabled: Boolean? = null,
    var description: String? = null,
    val channels: MutableList<WelcomeScreen.WelcomeScreenChannel> = mutableListOf()
) : JsonModifier {

    fun channel(channel: GuildChannel, description: String, emoji: Emoji? = null) {
        channels.add(WelcomeScreen.WelcomeScreenChannel(channel, description, emoji))
    }

    fun channel(channelId: Snowflake, description: String, emoji: Emoji? = null) {
        channels.add(WelcomeScreen.WelcomeScreenChannel(Channel(channelId, ChannelType.UNKNOWN, guild.client, guild), description, emoji))
    }

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("enabled", enabled)
            putOptional("description", description)
            putOptional("welcome_channels", channels.map {
                buildJsonObject {
                    put("channel_id", it.channel.id.string)
                    put("description", it.description)
                    putOptional("emoji_id", it.emoji?.id?.string)
                    putOptional("emoji_name", it.emoji?.name)
                }
            }.ifEmpty { null }?.toJsonArray())
        }

}
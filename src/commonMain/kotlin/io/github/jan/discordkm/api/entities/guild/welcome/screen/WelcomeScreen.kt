/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.welcome.screen

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.modifier.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
 * The welcome screen which is shown, when a new user joins the guild
 * @param description The description of the welcome screen
 * @param channels The channels shown in the welcome screen
 * @see Channel
 */
data class WelcomeScreen(
    val description: String? = null,
    val channels: List<WelcomeScreenChannel> = emptyList()
) {

    /*
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
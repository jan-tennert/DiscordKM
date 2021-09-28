/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName

@Serializable
class Emoji(
    val id: Snowflake? = null,
    val name: String,
    @SerialName("animated")
    val isAnimated: Boolean = false
) : Mentionable {

    class Emote @PublishedApi internal constructor(override val data: JsonObject, override val client: Client) : SnowflakeEntity, SerializableEntity {
        override val id = data.getId()
        val name = data.getOrThrow<String>("name")

        @get:JvmName("isAnimated")
        val isAnimated = data.getOrThrow<Boolean>("animated")
        //allowed users?
        //managed?
        //require colons?

        /**
         * If the emoji is available, can be null due to loss of server boosts
         */
        @get:JvmName("isAvailable")
        val isAvailable = data.getOrThrow<Boolean>("available")

        fun toEmoji() = Emoji(name = name, id = id)
    }

    companion object {

        fun fromEmoji(unicode: String)= Emoji(name = unicode)

        fun fromEmote(name: String, id: Snowflake) = Emoji(name = name, id = id)

        fun fromEmote(emote: Emote) = emote.toEmoji()

    }

    override val asMention = if(id != null) "$id:$name" else name

}
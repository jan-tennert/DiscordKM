/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.CategoryModifier
import io.github.jan.discordkm.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class Category(guild: Guild, data: JsonObject) : GuildChannel(guild, data) {

    /**
     * Modifies this category
     */
    @CallsTheAPI
    suspend fun modify(modifier: CategoryModifier.() -> Unit = {}): Category = client.buildRestAction<Category> {
        action = RestAction.Action.patch("/channels/$id", CategoryModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { guild.channelCache[id] = it }
    }

    companion object : GuildChannelBuilder<Category, CategoryModifier> {

        override fun create(builder: CategoryModifier.() -> Unit) = CategoryModifier().apply(builder).build()

    }

}
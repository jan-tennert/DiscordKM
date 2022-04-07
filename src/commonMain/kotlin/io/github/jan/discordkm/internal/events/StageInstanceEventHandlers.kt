/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.stage.StageInstance
import io.github.jan.discordkm.api.events.StageInstanceCreateEvent
import io.github.jan.discordkm.api.events.StageInstanceDeleteEvent
import io.github.jan.discordkm.api.events.StageInstanceUpdateEvent
import kotlinx.serialization.json.JsonObject

internal class StageInstanceCreateEventHandler(val client: Client) : InternalEventHandler<StageInstanceCreateEvent> {

    override suspend fun handle(data: JsonObject): StageInstanceCreateEvent {
        val stageInstance = StageInstance(data, client)
        stageInstance.guild.cache?.cacheManager?.stageInstanceCache?.set(stageInstance.id, stageInstance)
        return StageInstanceCreateEvent(stageInstance)
    }

}

internal class StageInstanceUpdateEventHandler(val client: Client) : InternalEventHandler<StageInstanceUpdateEvent> {

    override suspend fun handle(data: JsonObject): StageInstanceUpdateEvent {
        val stageInstance = StageInstance(data, client)
        val oldStageInstance = stageInstance.guild.cache?.stageInstances?.get(stageInstance.id)
        stageInstance.guild.cache?.cacheManager?.stageInstanceCache?.set(stageInstance.id, stageInstance)
        return StageInstanceUpdateEvent(stageInstance, oldStageInstance)
    }

}

internal class StageInstanceDeleteEventHandler(val client: Client) : InternalEventHandler<StageInstanceDeleteEvent> {

    override suspend fun handle(data: JsonObject): StageInstanceDeleteEvent {
        val stageInstance = StageInstance(data, client)
        stageInstance.guild.cache?.cacheManager?.stageInstanceCache?.remove(stageInstance.id)
        return StageInstanceDeleteEvent(stageInstance)
    }

}
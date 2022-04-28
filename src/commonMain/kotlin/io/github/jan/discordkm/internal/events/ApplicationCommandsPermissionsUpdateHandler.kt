package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.interactions.commands.permissions.ApplicationCommandPermissions
import io.github.jan.discordkm.api.events.ApplicationCommandsPermissionsUpdateEvent
import io.github.jan.discordkm.internal.serialization.serializers.ApplicationCommandPermissionSerializer
import kotlinx.serialization.json.JsonObject

internal class ApplicationCommandsPermissionsUpdateEventHandler(private val client: DiscordClient) : InternalEventHandler<ApplicationCommandsPermissionsUpdateEvent> {

    override suspend fun handle(data: JsonObject) = ApplicationCommandsPermissionsUpdateEvent(ApplicationCommandPermissionSerializer.deserialize(data, client))

}
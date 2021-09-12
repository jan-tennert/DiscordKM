package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel

class MessageBulkDeleteEvent(override val client: Client, val ids: List<Snowflake>, val channel: MessageChannel) : Event
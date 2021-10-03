package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client

class ShardCreateEvent(override val client: Client, val shardId: Int) : Event
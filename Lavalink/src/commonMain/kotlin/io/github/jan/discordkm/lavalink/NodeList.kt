package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient

class NodeList(private val client: DiscordWebSocketClient) : Iterable<LavalinkNode> {

    private val nodes = mutableListOf<LavalinkNode>()

    fun create(ip: String, port: Int, password: String, shardId: Int = 0) = LavalinkNode(ip, port, password, shardId, client).also { nodes += it }

    operator fun get(shardId: Int) = nodes.first { it.shardId == shardId }

    override fun iterator() = nodes.iterator()

}
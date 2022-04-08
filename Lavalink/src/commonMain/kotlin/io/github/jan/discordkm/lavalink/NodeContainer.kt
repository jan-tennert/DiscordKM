/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink

import co.touchlab.stately.collections.IsoMutableList
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient

sealed interface NodeContainer : Iterable<LavalinkNode> {

    /**
     * Creates a new [LavalinkNode] and adds it to the container.
     */
    fun create(ip: String, port: Int, password: String, shardId: Int = 0): LavalinkNode

    /**
     * Gets a [LavalinkNode] by its [shardId].
     */
    operator fun get(shardId: Int): LavalinkNode

}

internal class NodeContainerImpl(private val client: WSDiscordClient, private val nodes: IsoMutableList<LavalinkNode> = IsoMutableList<LavalinkNode>()) : NodeContainer, Iterable<LavalinkNode> by nodes {

    override fun create(ip: String, port: Int, password: String, shardId: Int) = LavalinkNodeImpl(ip, port, password, shardId, client).also { nodes += it }

    override operator fun get(shardId: Int) = nodes.first { it.shardId == shardId }

}
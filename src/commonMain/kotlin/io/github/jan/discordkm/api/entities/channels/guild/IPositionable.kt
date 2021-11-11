package io.github.jan.discordkm.api.entities.channels.guild

sealed interface IPositionable {

    /**
     * The position of this channel in the channel list
     */
    val position: Int

}
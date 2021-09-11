package io.github.jan.discordkm.entities

import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.misc.EnumList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface PermissionHolder : SnowflakeEntity {

    val permissions: EnumList<Permission>

    fun getPermissionsFor(channel: GuildChannel) : EnumList<Permission>

}


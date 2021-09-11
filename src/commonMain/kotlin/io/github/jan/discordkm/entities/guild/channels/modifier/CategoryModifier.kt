package io.github.jan.discordkm.entities.guild.channels.modifier

import io.github.jan.discordkm.entities.guild.channels.Category
import io.github.jan.discordkm.entities.guild.channels.PermissionOverride

class CategoryModifier : GuildChannelModifier<Category> {
    override var name: String? = null

    override var position: Int? = null

    override var permissionOverrides: MutableList<PermissionOverride> = mutableListOf()
}
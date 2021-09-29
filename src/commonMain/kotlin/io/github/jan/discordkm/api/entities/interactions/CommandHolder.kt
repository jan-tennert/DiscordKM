package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.internal.EntityCache

interface CommandHolder : BaseEntity {

    val commandCache: EntityCache<Snowflake, ApplicationCommand>
    val commands: CommandList

}
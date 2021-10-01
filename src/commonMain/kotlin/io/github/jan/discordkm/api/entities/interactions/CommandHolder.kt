package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.internal.EntityCache

interface CommandHolder : BaseEntity {

    val commandCache: EntityCache<Snowflake, ApplicationCommand>

    /**
     * Returns the command list of this command holder
     */
    val commands: CommandList

}
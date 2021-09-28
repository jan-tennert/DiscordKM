package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.CommandList

interface CommandHolder : BaseEntity {

    val commandCache: Cache<ApplicationCommand>
    val commands: CommandList

}
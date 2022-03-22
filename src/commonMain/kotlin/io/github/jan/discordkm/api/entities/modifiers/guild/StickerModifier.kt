package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.modifiers.MultipartModifier
import io.github.jan.discordkm.api.media.Attachment
import io.github.jan.discordkm.internal.restaction.Multipart
import io.github.jan.discordkm.internal.restaction.MultipartData
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class StickerModifier(private val edit: Boolean) : MultipartModifier {

    var name: String = ""
    var description: String = ""
    val tags = mutableListOf<String>()
    var file: Attachment? = null

    override val data: Multipart
        get() {
            if(file == null && !edit) throw IllegalArgumentException("You have to provide a file when creating a sticker")
            return Multipart(MultipartData.build(listOf(file!!), buildJsonObject {
                put("name", name)
                put("description", description)
                put("tags", tags.joinToString())
            }))
        }
}
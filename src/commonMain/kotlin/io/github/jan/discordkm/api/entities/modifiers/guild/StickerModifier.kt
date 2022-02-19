package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.modifiers.MultipartModifier
import io.github.jan.discordkm.api.media.Attachment
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class StickerModifier(private val edit: Boolean) : MultipartModifier {

    var name: String = ""
    var description: String = ""
    val tags = mutableListOf<String>()
    var file: Attachment? = null

    override val data: MultiPartFormDataContent
        get() {
            if(file == null && !edit) throw IllegalArgumentException("You have to provide a file when creating a sticker")
            return MultiPartFormDataContent(formData {
                appendInput("file", headers = Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=${file!!.fileName}")
                }, size = file!!.size) { buildPacket { writeFully(file!!.bytes) } }
                append("payload_json", buildJsonObject {
                    put("name", name)
                    put("description", description)
                    put("tags", tags.joinToString())
                }.toString())
            })
        }
}
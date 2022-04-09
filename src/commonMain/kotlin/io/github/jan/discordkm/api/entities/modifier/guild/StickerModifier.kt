/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifier.guild

import io.github.jan.discordkm.api.entities.modifier.MultipartModifier
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
package io.github.jan.discordkm.internal.restaction

import com.soywiz.korio.stream.AsyncStream
import com.soywiz.korio.stream.MemorySyncStreamToByteArray
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.stream.writeBytes
import com.soywiz.korio.stream.writeString
import io.github.jan.discordkm.api.media.Attachment
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

object MultipartData {

    fun build(attachments: List<Attachment>, json: JsonObject): AsyncStream {
        return MemorySyncStreamToByteArray {
            writeString("""
                --boundary
                Content-Disposition: form-data; name="payload_json"
                Content-Type: application/json
                
                ${Json.encodeToJsonElement(json)}
            """.trimIndent())
            attachments.forEach {
                writeString("""
                    --boundary
                    Content-Disposition: form-data; name="file"; filename="${it.fileName}"
                    Content-Type: ${it.contentType}
                    
                """.trimIndent())
                writeBytes(it.bytes)
            }
            writeString("--boundary--")
        }.also {
            println(it.decodeToString())
        }.openAsync()
    }

}

data class Multipart(val stream: AsyncStream)
package io.github.jan.discordkm.api.media

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.extension
import com.soywiz.krypto.encoding.Base64
import io.ktor.http.ContentType

class Image internal constructor(private val base64Data: String, private val contentType: ContentType) {

    val encodedData: String
        get() = "data:${contentType.contentType};base64,$base64Data"

    companion object {

        suspend fun fromFile(file: VfsFile) = Image(Base64.encode(file.readBytes()), when(file.extension) {
            "jpg" -> ContentType.Image.JPEG
            "gif" -> ContentType.Image.GIF
            "png" -> ContentType.Image.PNG
            else -> throw IllegalArgumentException("Invalid image")
        })

    }

}
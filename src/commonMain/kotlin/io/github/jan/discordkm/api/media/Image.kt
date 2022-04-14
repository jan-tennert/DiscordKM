/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.media

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.extension
import com.soywiz.krypto.encoding.Base64

class Image internal constructor(private val base64Data: String, private val contentType: String) {

    val encodedData: String
        get() = "data:${contentType};base64,$base64Data"

    companion object {

        suspend fun fromFile(file: VfsFile) = fromBytes(file.readBytes(), file.extension)

        fun fromBytes(bytes: ByteArray, extension: String) = Image(Base64.encode(bytes), when(extension) {
            "jpg" -> "image/jpeg"
            "gif" -> "image/gif"
            "png" -> "image/png"
            else -> throw IllegalArgumentException("Invalid image")
        })

    }

}
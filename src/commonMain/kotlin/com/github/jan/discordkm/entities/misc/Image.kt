package com.github.jan.discordkm.entities.misc

import com.soywiz.korio.file.VfsFile
import com.soywiz.krypto.encoding.Base64
import kotlin.jvm.JvmInline

@JvmInline
value class Image(val url: String) {

    companion object {

        suspend fun fromLocal(file: VfsFile) = Image("data:image/jpeg;base64,${Base64.encode(file.readBytes())}")

    }

}
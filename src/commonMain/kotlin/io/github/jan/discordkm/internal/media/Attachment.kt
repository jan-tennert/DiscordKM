package io.github.jan.discordkm.internal.media

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName


class Attachment internal constructor(val bytes: ByteArray, val fileName: String, val size: Long, val spoiler: Boolean = false) {

    companion object {

        suspend fun fromFile(file: VfsFile, spoiler: Boolean = false, fileName: String = file.baseName) = Attachment(file.readBytes(), fileName, file.size(), spoiler)

    }

}
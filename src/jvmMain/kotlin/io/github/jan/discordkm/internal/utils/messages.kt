package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.api.media.Attachment
import io.github.jan.discordkm.api.media.Image
import java.io.File
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.name
import kotlin.io.path.readBytes

fun Attachment.Companion.fromFile(file: File, fileName: String = file.name) = Attachment(file.readBytes(), fileName, file.length())

fun Attachment.Companion.fromFile(path: Path, fileName: String = path.name) = Attachment(path.readBytes(), fileName, path.fileSize())

fun Image.Companion.fromFile(path: Path) = fromBytes(path.readBytes(), path.extension)

fun Image.Companion.fromFile(file: File) = fromBytes(file.readBytes(), file.extension)
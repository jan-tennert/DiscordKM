/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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
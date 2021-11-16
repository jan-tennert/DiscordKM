/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.internal.caching.CacheFlag

/**
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
class RestOnlyClient @Deprecated("Use the method buildRestOnlyClient") internal constructor(token: String, loggingLevel: Logger.Level, enabledCache: Set<CacheFlag>) : Client(token, loggingLevel, enabledCache)

/**
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
fun buildRestOnlyClient(token: String, loggingLevel: Logger.Level, enabledCache: Set<CacheFlag> = CacheFlag.values().toSet()) =  RestOnlyClient(token, loggingLevel, enabledCache)

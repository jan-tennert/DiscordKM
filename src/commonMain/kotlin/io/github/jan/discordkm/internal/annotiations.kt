/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal

@RequiresOptIn(
    "This is internal for DiscordKM and can remove any time",
    RequiresOptIn.Level.WARNING
)
annotation class DiscordKMInternal

@RequiresOptIn(
    "This feature is unstable, not released or not properly tested and can change anytime",
    RequiresOptIn.Level.ERROR
)
annotation class DiscordKMUnstable
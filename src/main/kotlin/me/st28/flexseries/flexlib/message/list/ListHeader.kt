/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexlib.message.list

import me.st28.flexseries.flexlib.message.MasterMessageModule
import me.st28.flexseries.flexlib.message.MessageModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection

class ListHeader {

    val leftPrefix: String?
    val leftText: String?
    val leftSuffix: String?

    val rightPrefix: String?
    val rightText: String?
    val rightSuffix: String?

    val center: String

    constructor(config: ConfigurationSection) {
        leftPrefix = config.getString("left.prefix")
        leftText = config.getString("left.text")
        leftSuffix = config.getString("left.suffix")

        rightPrefix = config.getString("right.prefix")
        rightText = config.getString("right.text")
        rightSuffix = config.getString("right.suffix")

        center = config.getString("center", "")
    }

    fun getFormatted(page: Int, maxPages: Int, defaultHeader: ListHeader?, vararg replacements: String): String {
        val newCenter = ChatColor.translateAlternateColorCodes('&', center)
            .replace("{PAGE}", page.toString())
            .replace("{MAXPAGES}", maxPages.toString())

        return getFinalizedFormat(String.format(MessageModule.setupPatternReplace(newCenter), *replacements), defaultHeader)
    }

    private fun getFinalizedFormat(newCenter: String, defaultHeader: ListHeader?): String {
        val centerLength = ChatColor.stripColor(newCenter).length

        val sideLength = Math.max(1, (FlexPlugin.getGlobalModule(MasterMessageModule::class)!!.listLineLength - centerLength / 2)).toInt()

        val sb = StringBuilder()

        /* Left side */
        val applicableLeftPrefix = leftPrefix ?: defaultHeader?.leftPrefix
        val applicableLeftText = leftText ?: defaultHeader?.leftText
        val applicableLeftSuffix = leftSuffix ?: defaultHeader?.leftSuffix

        if (!applicableLeftText.isNullOrEmpty()) {
            val leftRepeat: Int = (sideLength / applicableLeftText!!.length) / 2
            if (leftRepeat > 0) {
                sb.append(applicableLeftPrefix)

                for (i in 0 until leftRepeat) {
                    sb.append(applicableLeftText)
                }

                sb.append(applicableLeftSuffix)
            }
        }

        sb.append(newCenter)

        /* Right side */
        val applicableRightPrefix = rightPrefix ?: defaultHeader?.rightPrefix
        val applicableRightText = rightText ?: defaultHeader?.rightText
        val applicableRightSuffix = rightSuffix ?: defaultHeader?.rightSuffix

        if (!applicableRightText.isNullOrEmpty()) {
            val rightRepeat: Int = (sideLength / applicableRightText!!.length) / 2
            if (rightRepeat > 0) {
                sb.append(applicableRightPrefix)

                for (i in 0 until rightRepeat) {
                    sb.append(applicableRightText)
                }

                sb.append(applicableRightSuffix)
            }
        }

        return ChatColor.translateAlternateColorCodes('&', sb.toString())
    }

}
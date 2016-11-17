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
package me.st28.flexseries.flexlib.player.lookup

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.st28.flexseries.flexlib.util.UuidUtils
import org.bukkit.configuration.ConfigurationSection
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

internal abstract class Resolver(val name: String) {

    companion object {

        val json: JsonParser = JsonParser()

        val RESOLVER_Mojang = "Mojang"
        val RESOLVER_MCAPIca = "MCAPI_ca"

    }


    protected var connectTimeout: Int = 0
    protected var readTimeout: Int = 0

    open fun loadConfig(config: ConfigurationSection?) {
        connectTimeout = config?.getInt("connect timeout", 5000) ?: 5000
        readTimeout = config?.getInt("read timeout", 5000) ?: 5000
    }

    abstract fun lookup(uuid: UUID): CacheEntry?

    abstract fun lookup(name: String): CacheEntry?

}

internal class Resolver_Mojang : Resolver(RESOLVER_Mojang) {

    private fun lookup_impl(inUrl: String): JsonObject? {
        try {
            val url = URL(inUrl)
            val connection = url.openConnection()
            val http = connection as HttpURLConnection

            http.connectTimeout = connectTimeout
            http.readTimeout = readTimeout
            http.connect()

            var recv = ""
            http.inputStream.use {
                while (it.available() > 0) {
                    recv += it.read().toChar()
                }
            }

            return json.parse(recv).asJsonArray.get(0).asJsonObject
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    override fun lookup(uuid: UUID): CacheEntry? {
        val obj = lookup_impl("https://sessionserver.mojang.com/session/minecraft/profile/${uuid.toString().replace("-", "")}")
            ?: return null

        return CacheEntry(
                uuid,
                obj["name"].asString
        )
    }

    override fun lookup(name: String): CacheEntry? {
        val obj = lookup_impl("https://api.mojang.com/users/profiles/minecraft/$name")
            ?: return null

        return CacheEntry(
                UuidUtils.fromString(obj["id"].asString),
                obj["name"].asString
        )
    }

}

internal class Resolver_MCAPIca : Resolver(RESOLVER_MCAPIca) {

    private fun lookup_impl(inUrl: String): CacheEntry? {
        try {
            val url = URL(inUrl)
            val connection = url.openConnection()
            val http = connection as HttpURLConnection

            http.connectTimeout = connectTimeout
            http.readTimeout = readTimeout
            http.connect()

            var recv = ""
            http.inputStream.use {
                while (it.available() > 0) {
                    recv += it.read().toChar()
                }
            }

            val obj = json.parse(recv).asJsonArray.get(0).asJsonObject
            return CacheEntry(UUID.fromString(obj.get("uuid_formatted").asString), obj.get("name").asString)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    override fun lookup(uuid: UUID): CacheEntry? {
        return lookup_impl("https://mcapi.ca/profile/${uuid.toString().replace("-", "")}")
    }

    override fun lookup(name: String): CacheEntry? {
        return lookup_impl("https://mcapi.ca/profile/$name")
    }

}

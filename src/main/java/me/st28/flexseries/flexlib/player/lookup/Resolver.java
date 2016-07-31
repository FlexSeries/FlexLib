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
package me.st28.flexseries.flexlib.player.lookup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.ConfigurationSection;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

abstract class Resolver {

    abstract String getName();

    abstract void loadConfig(ConfigurationSection config);

    abstract CacheEntry lookup(UUID uuid);

    abstract CacheEntry lookup(String name);

    // ------------------------------------------------------------------------------------------ //

    final static class Resolver_MCAPInet extends Resolver {

        public final static String NAME = "MC-API_net";

        private final JsonParser json = new JsonParser();

        private int connectTimeout;
        private int readTimeout;

        @Override
        String getName() {
            return NAME;
        }

        @Override
        void loadConfig(ConfigurationSection config) {
            connectTimeout = config.getInt("connect timeout", 5000);
            readTimeout = config.getInt("read timeout", 5000);
        }

        @Override
        CacheEntry lookup(UUID uuid) {
            try {
                URL url = new URL("https://us.mc-api.net/v3/name/" + uuid.toString().replace("-", ""));
                URLConnection connection = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) connection;

                http.setConnectTimeout(connectTimeout);
                http.setReadTimeout(readTimeout);
                http.connect();

                String recv = "";
                try (InputStream is = http.getInputStream()) {
                    while (is.available() > 0) {
                        recv += (char) is.read();
                    }
                }

                return parse(json.parse(recv).getAsJsonObject());
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        CacheEntry lookup(String name) {
            try {
                URL url = new URL("https://us.mc-api.net/v3/uuid/" + name);
                URLConnection connection = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) connection;

                http.setConnectTimeout(connectTimeout);
                http.setReadTimeout(readTimeout);
                http.connect();

                String recv = "";
                try (InputStream is = http.getInputStream()) {
                    while (is.available() > 0) {
                        recv += (char) is.read();
                    }
                }

                return parse(json.parse(recv).getAsJsonObject());
            } catch (Exception ex) {
                return null;
            }
        }

        private CacheEntry parse(JsonObject object) {
            return new CacheEntry(UUID.fromString(object.get("full_uuid").getAsString()), object.get("name").getAsString());
        }

    }

    // ------------------------------------------------------------------------------------------ //

    final static class Resolver_MCAPIca extends Resolver {

        public final static String NAME = "MCAPI_ca";

        private final JsonParser json = new JsonParser();

        private int connectTimeout;
        private int readTimeout;

        @Override
        String getName() {
            return NAME;
        }

        @Override
        void loadConfig(ConfigurationSection config) {
            connectTimeout = config.getInt("connect timeout", 5000);
            readTimeout = config.getInt("read timeout", 5000);
        }

        @Override
        CacheEntry lookup(UUID uuid) {
            try {
                URL url = new URL("https://mcapi.ca/name/uuid/" + uuid.toString().replace("-", ""));
                URLConnection connection = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) connection;

                http.setConnectTimeout(connectTimeout);
                http.setReadTimeout(readTimeout);
                http.connect();

                String recv = "";
                try (InputStream is = http.getInputStream()) {
                    while (is.available() > 0) {
                        recv += (char) is.read();
                    }
                }

                return parse(json.parse(recv).getAsJsonArray().get(0).getAsJsonObject());
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        CacheEntry lookup(String name) {
            try {
                URL url = new URL("https://mcapi.ca/uuid/player/" + name);
                URLConnection connection = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) connection;

                http.setConnectTimeout(connectTimeout);
                http.setReadTimeout(readTimeout);
                http.connect();

                String recv = "";
                try (InputStream is = http.getInputStream()) {
                    while (is.available() > 0) {
                        recv += (char) is.read();
                    }
                }

                return parse(json.parse(recv).getAsJsonArray().get(0).getAsJsonObject());
            } catch (Exception ex) {
                return null;
            }
        }

        private CacheEntry parse(JsonObject object) {
            return new CacheEntry(UUID.fromString(object.get("uuid_formatted").getAsString()), object.get("name").getAsString());
        }

    }

}
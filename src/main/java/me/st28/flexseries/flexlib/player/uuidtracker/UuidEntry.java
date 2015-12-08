/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.player.uuidtracker;

import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

class UuidEntry {

    final UUID uuid;
    final Map<String, Long> names = new HashMap<>();
    String currentName;

    UuidEntry(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");

        this.uuid = uuid;
    }

    UuidEntry(UuidEntry entry) {
        this.uuid = entry.uuid;
        this.names.putAll(entry.names);
        this.currentName = entry.currentName;
    }

    void determineCurrentName() {
        Entry<String, Long> latest = null;

        for (Entry<String, Long> entry : names.entrySet()) {
            if (latest == null) {
                latest = entry;
                continue;
            }

            if (entry.getValue() > latest.getValue()) {
                latest = entry;
            }
        }

        currentName = latest.getKey();
    }

}
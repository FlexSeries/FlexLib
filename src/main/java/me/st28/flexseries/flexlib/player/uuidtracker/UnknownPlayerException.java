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

import me.st28.flexseries.flexlib.player.PlayerReference;

import java.util.UUID;

/**
 * Thrown when a {@link PlayerReference} failed to be created due to missing information from the {@link PlayerUuidTracker}.
 */
public class UnknownPlayerException extends RuntimeException {

    public UnknownPlayerException(UUID uuid, String name) {
        super(getMessage(uuid, name));
    }

    private static String getMessage(UUID uuid, String name) {
        if (uuid == null) {
            return "No UUID entry for name '" + name + "' found.";
        }

        return "No UUID or name was provided.";
    }

}
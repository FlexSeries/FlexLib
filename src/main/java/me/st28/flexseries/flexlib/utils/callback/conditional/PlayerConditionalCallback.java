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
package me.st28.flexseries.flexlib.utils.callback.conditional;

import me.st28.flexseries.flexlib.utils.callback.ConditionalCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * A conditional callback that is executed if the given UUID is an online player.
 */
public class PlayerConditionalCallback extends ConditionalCallback {

    public PlayerConditionalCallback() {
        super(true); // Execute callback synchronously
    }

    @Override
    protected boolean canExecute(Map<String, Object> arguments) {
        UUID uuid = (UUID) arguments.get("uuid");

        if (uuid == null) {
            throw new IllegalArgumentException("No UUID provided.");
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            arguments.put("player", player);
        }

        return player != null;
    }

    @Override
    protected void run(Map<String, Object> arguments) {
        simpleRun((Player) arguments.get("player"));
    }

    protected void simpleRun(Player player) {}

}
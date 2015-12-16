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
package me.st28.flexseries.flexlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A reference to a command sender (player or console).
 */
public class CommandSenderRef {

    private final static String IDENTIFIER_CONSOLE = "c={}";
    private final static String IDENTIFIER_PLAYER = "p=";

    private String identifier;

    public CommandSenderRef(CommandSender sender) {
        if (sender instanceof Player) {
            identifier = IDENTIFIER_PLAYER + ((Player) sender).getUniqueId().toString();
        } else if (sender instanceof ConsoleCommandSender) {
            identifier = IDENTIFIER_CONSOLE;
        } else {
            throw new IllegalArgumentException("Unsupported CommandSender implementation: " + sender.getClass().getCanonicalName());
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public CommandSender getCommandSender() {
        if (identifier.startsWith(IDENTIFIER_PLAYER)) {
            return Bukkit.getPlayer(UUID.fromString(identifier.replace(IDENTIFIER_PLAYER, "")));
        } else if (identifier.startsWith(IDENTIFIER_CONSOLE)) {
            return Bukkit.getConsoleSender();
        }
        return null;
    }

}
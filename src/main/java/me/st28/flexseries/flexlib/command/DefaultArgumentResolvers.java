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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolveException;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver;
import me.st28.flexseries.flexlib.command.argument.ArgumentConfig;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.lookup.UnknownPlayerException;
import me.st28.flexseries.flexlib.utils.ArgumentCallback;
import me.st28.flexseries.flexlib.utils.UuidUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class DefaultArgumentResolvers {

    static class StringResolver extends ArgumentResolver<String> {

        StringResolver() {
            super(false);
        }

        @Override
        public String resolve(CommandContext context, ArgumentConfig config, String input) {
            int minLength = config.getInteger("min", -1);
            int maxLength = config.getInteger("max", -1);

            boolean tooShort = minLength > 0 && input.length() < minLength;
            boolean tooLong = maxLength > 0 && input.length() > maxLength;

            if (tooLong && tooShort) {
                throw new ArgumentResolveException(Message.get(FlexLib.class, "error.string_outside_range", minLength, maxLength));
            } else if (tooLong) {
                throw new ArgumentResolveException(Message.get(FlexLib.class, "error.string_too_long", maxLength));
            } else if (tooShort) {
                throw new ArgumentResolveException(Message.get(FlexLib.class, "error.string_too_short", minLength));
            }

            return input;
        }

        @Override
        public List<String> getTabOptions(CommandContext context, ArgumentConfig config, String input) {
            return null;
        }

    }

    static class PlayerResolver extends ArgumentResolver<PlayerReference> {

        PlayerResolver() {
            super(true);
        }

        @Override
        public PlayerReference resolve(CommandContext context, ArgumentConfig config, String input) {
            // NO ASYNC/BLOCKING CALLS IN THIS METHOD
            Player player = null;

            // 1) Check if input is UUID
            try {
                player = Bukkit.getPlayer(UuidUtils.fromString(input));
            } catch (IllegalArgumentException ex) { }

            // 2) Check if input is name
            if (player == null) {
                player = Bukkit.getPlayerExact(input);
            }

            // 3) Perform checks
            performChecks(context, config, player);

            return player == null ? null : new PlayerReference(player);
        }

        @Override
        public PlayerReference resolveAsync(CommandContext context, ArgumentConfig config, String input) {
            PlayerReference ref = null;

            // 1) Check if input is UUID (this method will potentially perform a lookup)
            try {
                ref = new PlayerReference(UuidUtils.fromString(input));
            } catch (IllegalArgumentException ex) { }

            // 2) Check if input is name (this method will potentially perform a lookup)
            if (ref == null) {
                try {
                    ref = new PlayerReference(input);
                } catch (UnknownPlayerException ex) {
                    throw new ArgumentResolveException("error.unknown_player");
                }
            }

            // 3) Perform checks
            final PlayerReference finalRef = ref;

            try {
                Bukkit.getScheduler().callSyncMethod(context.getCommand().getPlugin(), () -> performChecks(context, config, finalRef.getPlayer())).get();
            } catch (InterruptedException | ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof ArgumentResolveException) {
                    throw (ArgumentResolveException) cause;
                }

                LogHelper.severe(context.getCommand().getPlugin(), "An exception occurred while resolving argument", ex);
                throw new ArgumentResolveException("error.internal_error");
            }

            return ref;
        }

        private Void performChecks(CommandContext context, ArgumentConfig config, Player player) {
            if (player == null && config.isSet("online")) {
                throw new ArgumentResolveException("error.player_not_online");
            }

            if (player != null && config.isSet("notSender") && context.getSender() == player) {
                throw new ArgumentResolveException("error.player_cannot_be_sender");
            }
            return null;
        }

        @Override
        public List<String> getTabOptions(CommandContext context, ArgumentConfig config, String input) {
            return null;
        }

    }

}
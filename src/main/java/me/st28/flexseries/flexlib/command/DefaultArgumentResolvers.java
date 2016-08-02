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

import me.st28.flexseries.flexlib.command.argument.ArgumentConfig;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolveException;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver;
import me.st28.flexseries.flexlib.command.argument.AutoArgumentResolver;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.lookup.UnknownPlayerException;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.BooleanUtils;
import me.st28.flexseries.flexlib.utils.UuidUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Default FlexLib command argument resolvers.
 */
final class DefaultArgumentResolvers {

    static abstract class NumberResolver<T extends Number> extends ArgumentResolver<T> {

        private final String displayName;

        NumberResolver(String displayName) {
            super(false);
            this.displayName = displayName;
        }

        @Override
        public T resolve(CommandContext context, ArgumentConfig config, String input) {
            T resolved;
            try {
                resolved = handleResolve(context, config, input);
            } catch (NumberFormatException ex) {
                throw new ArgumentResolveException(getMessage("error.input_not"));
            }

            final boolean tooSmall = config.isSet("min") && compare(resolved, (T) config.get("min")) < 0;
            final boolean tooLarge = config.isSet("max") && compare(resolved, (T) config.get("max")) > 0;
            final boolean isRange = config.isSet("min") && config.isSet("max");

            if (isRange && (tooSmall || tooLarge)) {
                throw new ArgumentResolveException(getMessage("error.input_outside_range", config.get("min"), config.get("max")));
            } else if (tooLarge) {
                throw new ArgumentResolveException(getMessage("error.input_too_large", config.get("max")));
            } else if (tooSmall) {
                throw new ArgumentResolveException(getMessage("error.input_too_small", config.get("min")));
            }

            return resolved;
        }

        private Message getMessage(String message, Object... replacements) {
            return Message.getGlobal(message + "_" + displayName, replacements);
        }

        protected abstract T handleResolve(CommandContext context, ArgumentConfig config, String input);

        protected abstract int compare(T o1, T o2);

        @Override
        public List<String> getTabOptions(CommandContext context, ArgumentConfig config, String input) {
            return null;
        }

    }

    static class IntegerResolver extends NumberResolver<Integer> {

        IntegerResolver() {
            super("integer");
        }

        @Override
        protected Integer handleResolve(CommandContext context, ArgumentConfig config, String input) {
            return Integer.valueOf(input);
        }

        @Override
        protected int compare(Integer o1, Integer o2) {
            return Integer.compare(o1, o2);
        }

    }

    static class LongResolver extends NumberResolver<Long> {

        LongResolver() {
            super("integer");
        }

        @Override
        protected Long handleResolve(CommandContext context, ArgumentConfig config, String input) {
            return Long.parseLong(input);
        }

        @Override
        protected int compare(Long o1, Long o2) {
            return Long.compare(o1, o2);
        }

    }

    static class FloatResolver extends NumberResolver<Float> {

        FloatResolver() {
            super("decimal");
        }

        @Override
        protected Float handleResolve(CommandContext context, ArgumentConfig config, String input) {
            return Float.parseFloat(input);
        }

        @Override
        protected int compare(Float o1, Float o2) {
            return Float.compare(o1, o2);
        }

    }

    static class DoubleResolver extends NumberResolver<Double> {

        DoubleResolver() {
            super("decimal");
        }

        @Override
        protected Double handleResolve(CommandContext context, ArgumentConfig config, String input) {
            return Double.parseDouble(input);
        }

        @Override
        protected int compare(Double o1, Double o2) {
            return Double.compare(o1, o2);
        }

    }

    static class BooleanResolver extends ArgumentResolver<Boolean> {

        private final List<String> tabOptions;

        BooleanResolver() {
            super(false);
            List<String> temp = new ArrayList<>();
            Collections.addAll(temp, BooleanUtils.getTrueValues());
            Collections.addAll(temp, BooleanUtils.getFalseValues());
            tabOptions = Collections.unmodifiableList(temp);
        }

        @Override
        public Boolean resolve(CommandContext context, ArgumentConfig config, String input) {
            try {
                return BooleanUtils.fromString(input);
            } catch (IllegalArgumentException ex) {
                throw new ArgumentResolveException("error.input_not_boolean");
            }
        }

        @Override
        public List<String> getTabOptions(CommandContext context, ArgumentConfig config, String input) {
            return new ArrayList<>(tabOptions);
        }

    }

    static class StringResolver extends ArgumentResolver<String> {

        StringResolver() {
            super(false);
        }

        @Override
        public String resolve(CommandContext context, ArgumentConfig config, String input) {
            final int minLength = config.getInteger("min", -1);
            final int maxLength = config.getInteger("max", -1);
            final boolean tooShort = minLength > 0 && input.length() < minLength;
            final boolean tooLong = maxLength > 0 && input.length() > maxLength;
            final boolean isRange = minLength < 0 && maxLength < 0;

            if (isRange && (tooShort || tooLong)) {
                throw new ArgumentResolveException("error.string_outside_range", minLength, maxLength);
            } else if (tooLong) {
                throw new ArgumentResolveException("error.string_too_long", maxLength);
            } else if (tooShort) {
                throw new ArgumentResolveException("error.string_too_short", minLength);
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
                    throw new ArgumentResolveException("error.player_not_found", input);
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

    static class SessionResolver extends AutoArgumentResolver<CommandSession> {

        SessionResolver() {
            super(false);
        }

        @Override
        public CommandSession getDefault(CommandContext context, ArgumentConfig config) {
            final CommandModule module = FlexPlugin.getGlobalModule(CommandModule.class);

            final String id = config.getString("id");
            if (id == null) {
                throw new ArgumentResolveException("error.session_id_not_set");
            }

            boolean create = config.isSet("create");

            CommandSession session = module.getSession(context.getCommand().getPlugin().getClass(), context.getSender(), id, create);
            if (session == null && !config.isSet("optional")) {
                throw new ArgumentResolveException("error.session_does_not_exist");
            }
            return session;
        }

    }

}
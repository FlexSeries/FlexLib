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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.ArgumentCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ArgumentResolver<T> {

    /**
     * Contains registered argument resolvers.
     * < Identifier, Resolver >
     */
    static final Map<String, ArgumentResolver> resolvers = new HashMap<>();

    /**
     * Registers an argument resolver.
     *
     * @param plugin The plugin that the resolver interacts with.
     *               Null to register a generic argument resolver (not recommended).
     * @param identifier The identifier of the argument type.
     * @param resolver The resolver to register.
     * @return True if the argument resolver was successfully registered.
     *         False if the identifier is already registered to another argument resolver.
     */
    public static boolean register(FlexPlugin plugin, String identifier, ArgumentResolver resolver) {
        final String fullIdentifier = (plugin == null ? "" : (plugin.getName().toLowerCase() + "::")) + identifier;

        if (resolvers.containsKey(fullIdentifier)) {
            LogHelper.warning(FlexLib.class, "Argument resolver '" + fullIdentifier + "' is already registered.");
            return false;
        }

        resolvers.put(fullIdentifier, resolver);
        return true;
    }

    public static ArgumentResolver getResolver(String identifier) {
        return resolvers.get(identifier);
    }

    // ------------------------------------------------------------------------------------------ //

    private final boolean isAsync;

    public ArgumentResolver(boolean isAsync) {
        this.isAsync = isAsync;
    }

    /**
     * @return True if this argument should be resolved asynchronously.
     */
    public final boolean isAsync() {
        return isAsync;
    }

    /**
     * Resolves the input value into the full argument type.
     * {@link ArgumentResolveException} should be preferred in most cases over returning null for failed argument resolution.
     *
     * @param context The current command's context.
     * @param config The current command's config for the argument.
     * @param input The user input value.
     * @return The resolved argument.
     *         Can be null.
     * @throws ArgumentResolveException Thrown when the input value cannot be resolved into the full argument type.
     */
    public abstract T resolve(CommandContext context, ArgumentConfig config, String input);

    /**
     * This method is only called if {@link #isAsync} is true and {@link #resolve(CommandContext, ArgumentConfig, String)} returns null.
     *
     * @param context
     * @param config
     * @param input
     * @return
     */
    public T resolveAsync(CommandContext context, ArgumentConfig config, String input) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns tab complete options based on the current input value.
     *
     * @param context The current command's context.
     * @param config The current command's config for the argument.
     * @param input The current user input.
     * @return A list of possible options for the given input value.
     *         Null or an empty list indicates no options are available.
     */
    public abstract List<String> getTabOptions(CommandContext context, ArgumentConfig config, String input);

    /**
     * This method should be overridden if an argument type can have a default value. By default, this
     * method will throw an UnsupportedOperationException indicating that default values are not supported.
     *
     * @param context The current command's context.
     * @param config The current command's config for the argument.
     * @return The default argument value for a given {@link CommandContext}.
     * @throws UnsupportedOperationException Thrown if the argument type does not support default values.
     */
    public T getDefault(CommandContext context, ArgumentConfig config) {
        throw new UnsupportedOperationException("Argument does not have a default value.");
    }

}
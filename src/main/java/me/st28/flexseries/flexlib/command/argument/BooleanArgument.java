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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.utils.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

public class BooleanArgument extends Argument {

    private Boolean defaultValue;

    public BooleanArgument(String name, boolean isRequired) {
        this(name, isRequired, false);
    }

    public BooleanArgument(String name, boolean isRequired, Boolean defaultValue) {
        super(name, isRequired);
        this.defaultValue = defaultValue;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        try {
            return BooleanUtils.parseBoolean(input);
        } catch (IllegalArgumentException ex) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.item_must_be_boolean", new ReplacementMap("{ITEM}", StringUtils.capitalize(getName())).getMap()));
        }
    }

    @Override
    public Object getDefaultValue(CommandContext context) {
        return defaultValue;
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String input) {
        return Arrays.asList("true", "false");
    }

}
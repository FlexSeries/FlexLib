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
import org.apache.commons.lang.StringUtils;

public class IntegerArgument extends Argument {

    private int minValue;
    private int maxValue;
    private Integer defaultValue;

    public IntegerArgument(String name, boolean isRequired) {
        this(name, isRequired, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
    }

    public IntegerArgument(String name, boolean isRequired, int minValue, int maxValue, Integer defaultValue) {
        super(name, isRequired);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
    }

    public int getMinValue() {
        return minValue;
    }

    /**
     * @return This instance, for chaining.
     */
    public IntegerArgument minValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    /**
     * @return This instance, for chaining.
     */
    public IntegerArgument maxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    /**
     * @return This instance, for chaining.
     */
    public IntegerArgument defaultValue(Integer defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        int integer;
        try {
            integer = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.item_must_be_int", new ReplacementMap("{ITEM}", StringUtils.capitalize(getName())).getMap()));
        }

        if (integer < minValue) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.integer_too_low", new ReplacementMap("{ITEM}", StringUtils.capitalize(getName())).put("{MIN}", Integer.toString(minValue)).getMap()));
        } else if (integer > maxValue) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.integer_too_high", new ReplacementMap("{ITEM}", StringUtils.capitalize(getName())).put("{MAX}", Integer.toString(maxValue)).getMap()));
        }

        return integer;
    }

    @Override
    public Object getDefaultValue(CommandContext context) {
        return defaultValue;
    }

}
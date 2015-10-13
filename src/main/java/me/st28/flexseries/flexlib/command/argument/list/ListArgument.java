/**
 * FlexLib - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexlib.command.argument.list;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

public class ListArgument extends Argument {

    private ListArgumentParser parser;

    public ListArgument(String name, boolean isRequired, ListArgumentParser parser) {
        super(name, isRequired);

        Validate.notNull(parser, "Parser cannot be null.");
        this.parser = parser;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        List<Object> returnList = new ArrayList<>();

        for (String arg : context.getArgs()) {
            try {
                returnList.add(parser.parseInput(context, arg));
            } catch (CommandInterruptedException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_ERROR, MessageManager.getMessage(FlexLib.class, "lib_command.errors.invalid_input_list", new ReplacementMap("{LIST}", input).getMap()));
            }
        }

        return returnList;
    }

    // ------------------------------------------------------------------------------------------ //

    public static abstract class ListArgumentParser {

        /**
         * Parses input into its appropriate type.
         * This should throw an exception if it fails.
         *
         * @return The parsed input.
         */
        public abstract Object parseInput(CommandContext context, String input);

    }

}
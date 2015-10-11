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
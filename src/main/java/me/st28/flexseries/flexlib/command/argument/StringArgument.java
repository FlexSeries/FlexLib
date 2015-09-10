package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.utils.StringUtils;

import java.util.List;

public class StringArgument extends Argument {

    private boolean untilEnd;

    public StringArgument(String name, boolean isRequired) {
        this(name, isRequired, false);
    }

    public StringArgument(String name, boolean isRequired, boolean untilEnd) {
        super(name, isRequired);

        this.untilEnd = untilEnd;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        if (!untilEnd) {
            return input;
        }

        final List<String> args = context.getArgs();

        return StringUtils.collectionToString(args.subList(args.indexOf(input), args.size()), " ");
    }

}
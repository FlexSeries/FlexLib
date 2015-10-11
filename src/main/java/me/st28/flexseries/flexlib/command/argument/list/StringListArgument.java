package me.st28.flexseries.flexlib.command.argument.list;

import me.st28.flexseries.flexlib.command.CommandContext;

public class StringListArgument extends ListArgument {

    public StringListArgument(String name, boolean isRequired) {
        super(name, isRequired, new ListArgumentParser() {
            @Override
            public Object parseInput(CommandContext context, String input) {
                return input;
            }
        });
    }

}
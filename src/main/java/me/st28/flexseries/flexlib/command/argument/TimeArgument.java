package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.utils.TimeUtils;

public class TimeArgument extends Argument {

    public TimeArgument(String name, boolean isRequired) {
        super(name, isRequired);
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        return TimeUtils.interpretSeconds(input);
    }

}
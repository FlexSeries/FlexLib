package me.st28.flexseries.flexlib.command.argument.list;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;

import java.util.List;
import java.util.stream.Collectors;

public class FlexModuleListArgument extends ListArgument {

    public FlexModuleListArgument(String name, boolean isRequired) {
        super(name, isRequired, new ListArgumentParser() {
            @Override
            public Object parseInput(CommandContext context, String input) {
                FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

                for (FlexModule module : plugin.getModules()) {
                    if (module.getName().equalsIgnoreCase(input)) {
                        return module;
                    }
                }

                throw new CommandInterruptedException(InterruptReason.ARGUMENT_ERROR,
                        MessageManager.getMessage(FlexLib.class, "lib_plugin.errors.module_not_found",
                                new ReplacementMap("{PLUGIN}", plugin.getName())
                                        .put("{MODULE}", input)
                                        .getMap()
                        )
                );
            }
        });
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String input) {
        FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

        return plugin.getModules().stream().map(FlexModule::getName).collect(Collectors.toList());
    }

}
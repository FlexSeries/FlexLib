package me.st28.flexseries.flexlib.utils.task;

import me.st28.flexseries.flexlib.utils.CommandSenderRef;
import me.st28.flexseries.flexlib.utils.TaskChain;
import org.bukkit.command.CommandSender;

public class SyncCommandSenderTask extends TaskChain {

    public SyncCommandSenderTask(CommandSender sender, LastTask<CommandSender> task) {
        this(new CommandSenderRef(sender), task);
    }

    public SyncCommandSenderTask(CommandSenderRef sender, LastTask<CommandSender> task) {
        add(new FirstTask<CommandSender>() {
            @Override
            protected CommandSender run() {
                CommandSender cs = sender.getCommandSender();
                if (cs != null) {
                    return cs;
                } else {
                    abort();
                    return null;
                }
            }
        }).add(task);
    }

}
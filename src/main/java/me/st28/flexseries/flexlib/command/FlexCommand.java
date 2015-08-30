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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.command.logic.hub.LogicHub;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class FlexCommand<T extends FlexPlugin> implements CommandExecutor, TabCompleter {

    private final T plugin;
    private final LogicHub base;
    private final String name;

    public FlexCommand(T plugin, LogicHub base, String name) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(base, "Base cannot be null.");
        Validate.notNull(name, "Name cannot be null.");

        this.plugin = plugin;
        this.base = base;
        base.setFirstArgumentIndex(0);
        this.name = name;
    }

    public T getPlugin() {
        return plugin;
    }

    public void register() {
        plugin.getCommand(name).setExecutor(this);
        plugin.getCommand(name).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(this, sender, label, args);

        try {
            base.execute(context, 0);
        } catch (CommandInterruptedException ex) {
            ex.getMessageReference().sendTo(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(this, sender, label, args);

        List<String> suggestions = base.getSuggestions(context, args.length - 1);

        if (suggestions == null) {
            return null;
        }

        String argument = context.getArgs().get(args.length - 1).toLowerCase();

        List<String> returnList = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(argument)) {
                returnList.add(suggestion);
            }
        }

        return returnList;
    }

}
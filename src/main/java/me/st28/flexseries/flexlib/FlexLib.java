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
package me.st28.flexseries.flexlib;

import me.st28.flexseries.flexlib.backend.commands.CmdFlexModules;
import me.st28.flexseries.flexlib.backend.commands.CmdFlexReload;
import me.st28.flexseries.flexlib.backend.commands.CmdFlexSave;
import me.st28.flexseries.flexlib.command.FlexCommandWrapper;
import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.MessageMasterManager;
import me.st28.flexseries.flexlib.message.list.ListManager;
import me.st28.flexseries.flexlib.message.variable.MessageVariable;
import me.st28.flexseries.flexlib.permission.PermissionHelper;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.settings.PlayerSettingsManager;
import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.PluginStatus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class FlexLib extends FlexPlugin {

    private static FlexLib instance;

    public static FlexLib getInstance() {
        return instance;
    }

    private String serverName;

    @Override
    public void handleLoad() {
        instance = this;

        registerModule(new MessageMasterManager(this));
        registerModule(new HookManager(this));
        registerModule(new ListManager(this));
        registerModule(new PlayerManager(this));
        registerModule(new PlayerSettingsManager(this));
        registerModule(new PlayerUuidTracker(this));
    }

    @Override
    public void handleEnable() {
        FlexCommandWrapper.registerCommand(new CmdFlexModules(this));
        FlexCommandWrapper.registerCommand(new CmdFlexReload(this));
        FlexCommandWrapper.registerCommand(new CmdFlexSave(this));

        // Register default variables
        MessageVariable.registerVariable(new MessageVariable("server") {
            @Override
            public String getReplacement(Player player) {
                return serverName;
            }
        });

        MessageVariable.registerVariable(new MessageVariable("name") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getName();
            }
        });

        MessageVariable.registerVariable(new MessageVariable("dispname") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getDisplayName();
            }
        });

        MessageVariable.registerVariable(new MessageVariable("world") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getWorld().getName();
            }
        });

        PermissionHelper.reload(getConfig().getConfigurationSection("permission helper"));
    }

    @Override
    public void handleConfigReload(FileConfiguration config) {
        serverName = config.getString("server name", "Minecraft Server");

        if (getStatus() == PluginStatus.ENABLED) {
            PermissionHelper.reload(config.getConfigurationSection("permission helper"));
        }
    }

    public String getServerName() {
        return serverName;
    }

}
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
package me.st28.flexseries.flexlib;

import me.st28.flexseries.flexlib.backend.commands.CmdFlexAdmin;
import me.st28.flexseries.flexlib.backend.commands.plugin.CmdFlexModules;
import me.st28.flexseries.flexlib.backend.commands.plugin.CmdFlexReload;
import me.st28.flexseries.flexlib.backend.commands.plugin.CmdFlexSave;
import me.st28.flexseries.flexlib.command.FlexCommandWrapper;
import me.st28.flexseries.flexlib.gui.GuiManager;
import me.st28.flexseries.flexlib.hook.HookDisabledException;
import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.log.LogHelper;
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
        registerModule(new GuiManager(this));
        registerModule(new HookManager(this));
        registerModule(new ListManager(this));
        registerModule(new PlayerManager(this));
        registerModule(new PlayerSettingsManager(this));
        registerModule(new PlayerUuidTracker(this));
    }

    @Override
    public void handleEnable() {
        FlexCommandWrapper.registerCommand(new CmdFlexAdmin(this));
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

        reloadPermissionHelper();
        FlexCommandWrapper.reload(getConfig());
    }

    @Override
    public void handleConfigReload(FileConfiguration config) {
        serverName = config.getString("server name", "Minecraft Server");

        if (getStatus() == PluginStatus.ENABLED) {
            reloadPermissionHelper();
            FlexCommandWrapper.reload(config);
        }
    }

    private void reloadPermissionHelper() {
        try {
            PermissionHelper.reload(getConfig().getConfigurationSection("permission helper"));
        } catch (HookDisabledException ex) {
            LogHelper.warning(this, "Permission helper disabled - Vault is not installed.");
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while loading the permission helper", ex);
        }
    }

    public String getServerName() {
        return serverName;
    }

}
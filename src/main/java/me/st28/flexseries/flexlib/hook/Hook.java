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
package me.st28.flexseries.flexlib.hook;

import me.st28.flexseries.flexlib.log.LogHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Hook {

    private boolean isEnabled = false;
    private final String pluginName;

    public Hook(String pluginName) {
        Validate.notNull(pluginName, "Plugin name cannot be null.");
        this.pluginName = pluginName;
    }

    public final String getPluginName() {
        return pluginName;
    }

    public final Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }

    public final <T extends Plugin> T getPlugin(Class<T> type) {
        final Plugin plugin = getPlugin();
        return plugin == null ? null : (T) plugin;
    }

    public final boolean isEnabled() {
        return isEnabled;
    }

    final void enable(HookManager manager) {
        final Plugin plugin = getPlugin();
        if (plugin == null) {
            isEnabled = false;
            LogHelper.warning(manager, "Unable to find plugin hook '" + pluginName + "' - some features may be unavailable.");
        } else {
            try {
                handleEnable();
                isEnabled = true;
                LogHelper.info(manager, "Found plugin hook '" + pluginName + "'");
            } catch (Exception ex) {
                LogHelper.severe(manager, "An exception occurred while enabling hook '" + pluginName + "'", ex);
            }
        }
    }

    final void disable(HookManager manager) {
        if (isEnabled) {
            try {
                handleDisable();
            } catch (Exception ex) {
                LogHelper.severe(manager, "An exception occurred while disabling hook '" + pluginName + "'", ex);
            }
        }
    }

    protected void handleEnable() {}

    protected void handleDisable() {}

}
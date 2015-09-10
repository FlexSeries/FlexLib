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
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
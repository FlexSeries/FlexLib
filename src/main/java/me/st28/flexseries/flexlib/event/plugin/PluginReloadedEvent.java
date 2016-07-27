package me.st28.flexseries.flexlib.event.plugin;

import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link FlexPlugin} implementation is reloaded.
 */
public class PluginReloadedEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private final FlexPlugin plugin;

    public PluginReloadedEvent(FlexPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null");
        this.plugin = plugin;
    }

    public FlexPlugin getPlugin() {
        return plugin;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
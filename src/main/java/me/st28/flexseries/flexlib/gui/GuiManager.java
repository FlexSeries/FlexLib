/**
 * FlexCore - Licensed under the MIT License (MIT)
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
package me.st28.flexseries.flexlib.gui;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manges custom inventory GUIs.
 */
public final class GuiManager extends FlexModule<FlexLib> implements Listener {

    private final Map<UUID, GUI> playerGuis = new HashMap<>();

    public GuiManager(FlexLib plugin) {
        super(plugin, "gui", "Manages inventory GUIs", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleDisable() {
        for (GUI gui : playerGuis.values()) {
            gui.destroy();
        }
    }

    /**
     * @return a player's current GUI.
     */
    public GUI getGui(Player player) {
        Validate.notNull(player, "Player cannot be null.");

        return playerGuis.get(player.getUniqueId());
    }

    /**
     * Sets the GUI for a player.
     *
     * @param player The player to set the GUI for.
     * @param gui The GUI to set for the player.<br />
     *            Null to close the player's current GUI.
     */
    public void setGui(Player player, GUI gui) {
        Validate.notNull(player, "Player cannot be null.");

        UUID uuid = player.getUniqueId();

        if (gui != null && playerGuis.containsKey(uuid)) {
            throw new IllegalStateException("Player '" + player.getName() + "' already has a GUI open.");
        }

        if (gui == null) {
            playerGuis.remove(uuid);
            return;
        }

        playerGuis.put(uuid, gui);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        GUI gui = playerGuis.get(uuid);
        if (gui == null) return;

        gui.inventoryClick(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        GUI gui = playerGuis.get(uuid);
        if (gui == null) return;

        if (e.getInventorySlots().size() > 1) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        GUI gui = playerGuis.get(uuid);
        if (gui == null || gui.transitioningPlayers.remove(uuid)) return;

        gui.closeForPlayer((Player) e.getPlayer());
    }

}
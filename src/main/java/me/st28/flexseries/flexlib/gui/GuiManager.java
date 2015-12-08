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
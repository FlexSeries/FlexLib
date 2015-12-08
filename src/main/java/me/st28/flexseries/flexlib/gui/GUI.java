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

import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

/**
 * Represents a custom inventory GUI.
 */
public class GUI {

    private final String title;
    private final int size;

    /**
     * The default page to open for new instances of this GUI.
     */
    private int defaultPage;

    /**
     * Whether or not to remove a player's instance of this GUI if they close it.
     */
    private boolean removeInstanceOnClose;

    /**
     * Whether or not to destroy this GUI if there are no more players with it open after removing a player.
     */
    private boolean autoDestroy;

    /**
     * Allow items to be placed into the inventory.
     */
    private boolean allowPlacing;

    /**
     * Allow items to be taken from the inventory.
     */
    private boolean allowTaking;

    /**
     * Contains all of the different pages of the GUI.
     */
    private final List<GuiPage> pages = new ArrayList<>();

    /**
     * Stores the players that currently have this GUI opened.<br />
     * <b>Format:</b> <code>Player UUID, page number</code>
     */
    private final Map<UUID, Integer> playerInstances = new HashMap<>();

    final Set<UUID> transitioningPlayers = new HashSet<>();

    /**
     * Creates a GUI.
     *
     * @param title The title to display in the inventory.<br />
     *              <code>{PAGE}</code> is a placeholder for the current page.
     *              <code>{MAXPAGES}</code> is a placeholder for the total page count.
     * @param size The number of rows that the inventory should contain.
     */
    public GUI(String title, int size, int defaultPage, boolean removeInstanceOnClose, boolean autoDestroy, boolean allowPlacing, boolean allowTaking) {
        Validate.notNull(title, "Title cannot be null.");
        Validate.isTrue(title.length() <= 32, "Title cannot be longer than 32 characters");
        Validate.isTrue(size >= 1 && size <= 9, "Size must be 1-9");
        Validate.isTrue(defaultPage >= 0, "Default page must be 0 or more.");

        this.title = title;
        this.size = size;
        this.defaultPage = defaultPage;
        this.removeInstanceOnClose = removeInstanceOnClose;
        this.autoDestroy = autoDestroy;
        this.allowPlacing = allowPlacing;
        this.allowTaking = allowTaking;
    }

    /**
     * Adds a page to this GUI.
     *
     * @param page The page to add.
     */
    public void addPage(GuiPage page) {
        Validate.notNull(page, "Page cannot be null.");
        Validate.isTrue(!pages.contains(page), "Page is already added.");

        int pageCount = pages.size() + 1;

        pages.add(page);
        page.items = Arrays.asList(new GuiItem[size * 9]);
        page.inventory = Bukkit.createInventory(null, size * 9, ChatColor.translateAlternateColorCodes('&', title).replace("{PAGE}", Integer.toString(pageCount)).replace("{MAXPAGES}", Integer.toString(pageCount)));
    }

    /**
     * Sets an item in the GUI.
     *
     * @param page The page to put the item on. Page count is zero-indexed<br />
     *             If the page doesn't exist, it will be created.
     * @param item The item to place in the GUI.<br />
     *             If null, will remove the current item.
     */
    public void setItem(int page, int slotX, int slotY, GuiItem item) {
        Validate.isTrue(page >= 0, "Page must be 0 or more.");
        Validate.isTrue(slotY < size, "Slot y (" + slotY + ") must be less than " + size);
        Validate.isTrue(slotX >= 0 && slotX <= 8, "Slot x must be 0-8");

        GuiPage pageObj = pages.get(page);
        int slot = slotY * 9 + slotX;

        pageObj.items.set(slot, item);
        pageObj.inventory.setItem(slot, item.getItemStack());
    }

    /**
     * @return the default page number for this GUI.
     */
    public int getDefaultPage() {
        return defaultPage;
    }

    /**
     * @return the number of pages that the inventory currently has.
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * Opens the GUI for a specified player.
     *
     * @param player The player to open the GUI for.
     */
    public final void openForPlayer(Player player) {
        Validate.notNull(player, "Player cannot be null.");
        GuiManager guiManager = FlexPlugin.getGlobalModule(GuiManager.class);

        UUID uuid = player.getUniqueId();

        int page;
        if (playerInstances.containsKey(uuid)) {
            page = playerInstances.get(uuid);
            if (page >= pages.size()) {
                page = pages.size() - 1;
            }
        } else {
            page = defaultPage;
        }

        setPlayerPage(player, page);

        guiManager.setGui(player, this);
        handleOpen(player);
    }

    /**
     * Handles custom tasks for this GUI when a player opens it.
     *
     * @param player The player who opened the GUI.
     */
    protected void handleOpen(Player player) {}

    public boolean setPlayerPage(Player player, int page) {
        Validate.notNull(player, "Player cannot be null.");
        if (page < 0 || page >= pages.size()) {
            return false;
        }

        Inventory inventory = pages.get(page).inventory;

        if (playerInstances.containsKey(player.getUniqueId())) {
            transitioningPlayers.add(player.getUniqueId());
        }

        playerInstances.put(player.getUniqueId(), page);
        player.openInventory(inventory);
        return true;
    }

    public boolean nextPlayerPage(Player player) {
        return setPlayerPage(player, playerInstances.get(player.getUniqueId()) + 1);
    }

    public boolean previousPlayerPage(Player player) {
        return setPlayerPage(player, playerInstances.get(player.getUniqueId()) - 1);
    }

    public final void inventoryClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        boolean isClickInGui = e.getRawSlot() < size * 9;
        int slot = e.getSlot();
        if (slot < 0) return;

        GuiPage page = pages.get(playerInstances.get(uuid));
        GuiItem item = !isClickInGui ? null : page.items.get(slot);

        if (!isClickInGui) {
            // Click was in the player's inventory
            switch (e.getAction()) {
                case MOVE_TO_OTHER_INVENTORY:
                case COLLECT_TO_CURSOR:
                case UNKNOWN:
                    e.setCancelled(true);
                    break;

                default:
            }
        } else if (item != null) {
            e.setCancelled(true);
            item.handleClick((Player) e.getWhoClicked(), e.getClick());
        } else {
            switch (e.getAction()) {
                /*case COLLECT_TO_CURSOR:
                case UNKNOWN:
                    e.setCancelled(true);
                    break;*/

                // Placing
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                    if (!allowPlacing) {
                        e.setCancelled(true);
                    }
                    break;

                // Taking
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                case MOVE_TO_OTHER_INVENTORY:
                    if (!allowTaking) {
                        e.setCancelled(true);
                    }
                    break;

                default:
                    e.setCancelled(true);
            }
        }
    }

    public final void closeForPlayer(Player player) {
        Validate.notNull(player, "Player cannot be null.");
        GuiManager guiManager = FlexPlugin.getGlobalModule(GuiManager.class);

        UUID uuid = player.getUniqueId();

        if (!playerInstances.containsKey(uuid)) {
            throw new IllegalStateException("Player '" + player.getName() + "' does not have this inventory open.");
        }

        handleClose(player);

        if (removeInstanceOnClose) {
            playerInstances.remove(uuid);
        }

        guiManager.setGui(player, null);

        if (autoDestroy && playerInstances.isEmpty()) {
            destroy();
        }
        player.closeInventory();
    }

    /**
     * Handles custom tasks for this GUI when a player closes it.
     *
     * @param player The player who closed the GUI.
     */
    protected void handleClose(Player player) {}

    public final void destroy() {
        for (UUID uuid : playerInstances.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                closeForPlayer(p);
            }
        }

        handleDestroy();
    }

    /**
     * Handles custom tasks for this GUI when it is destroyed.
     */
    protected void handleDestroy() {}

}
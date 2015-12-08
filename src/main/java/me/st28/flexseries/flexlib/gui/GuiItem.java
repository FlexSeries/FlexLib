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

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in a GUI.
 */
public abstract class GuiItem {

    /**
     * @return the ItemStack that this GuiItem exists as in a GUI inventory.
     */
    public abstract ItemStack getItemStack();

    /**
     * Handles a player clicking the item in the GUI.
     *
     * @param player The player who clicked the item.
     * @param clickType The type of click.
     */
    public abstract void handleClick(Player player, ClickType clickType);

}
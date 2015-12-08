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

import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Represents a page of a GUI.
 */
public class GuiPage {

    List<GuiItem> items;
    Inventory inventory;

    private boolean keepIfEmpty;

    /**
     * @param keepIfEmpty True to preserve the page if the GUI page is changed and this page is empty.
     */
    public GuiPage(boolean keepIfEmpty) {
        this.keepIfEmpty = keepIfEmpty;
    }

}
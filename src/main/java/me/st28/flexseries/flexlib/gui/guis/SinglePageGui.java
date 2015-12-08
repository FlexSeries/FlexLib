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
package me.st28.flexseries.flexlib.gui.guis;

import me.st28.flexseries.flexlib.gui.GUI;
import me.st28.flexseries.flexlib.gui.GuiItem;
import me.st28.flexseries.flexlib.gui.GuiPage;

/**
 * A GUI that only contains a single page.
 */
public class SinglePageGui extends GUI {

    public SinglePageGui(String title, int size, boolean removeInstanceOnClose, boolean autoDestroy, boolean allowPlacing, boolean allowTaking) {
        super(title, size, 0, removeInstanceOnClose, autoDestroy, allowPlacing, allowTaking);
        super.addPage(new GuiPage(true));
    }

    @Override
    public final void addPage(GuiPage page) {
        throw new UnsupportedOperationException("SinglePageGui can have only one page.");
    }

    @Override
    public void setItem(int page, int slotX, int slotY, GuiItem item) {
        super.setItem(0, slotX, slotY, item);
    }

}
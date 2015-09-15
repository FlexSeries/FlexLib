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
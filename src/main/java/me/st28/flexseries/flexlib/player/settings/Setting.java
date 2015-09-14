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
package me.st28.flexseries.flexlib.player.settings;

import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.data.PlayerData;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.UUID;

public abstract class Setting {

    private String group;
    private String name;
    private boolean isTransient;

    public Setting(String group, String name, boolean isTransient) {
        Validate.notNull(group, "Group cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        this.group = group;
        this.name = name;
        this.isTransient = isTransient;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setValue(UUID player, String input) {
        Validate.notNull(player, "Player cannot be null.");

        setValueDirect(player, parseInput(player, input));
    }

    public void setValueDirect(UUID player, Object value) {
        Validate.notNull(player, "Player cannot be null.");
        PlayerData data = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(player, true);

        data.setCustomData("setting." + group + "." + name, value, isTransient);
    }

    public <T> T getValue(UUID player, Class<T> type) {
        return getValue(player, type, (T) getDefaultValue(player));
    }

    public <T> T getValue(UUID player, Class<T> type, T defaultValue) {
        Validate.notNull(player, "Player cannot be null.");
        Validate.notNull(type, "Type cannot be null.");

        PlayerData data = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(player, true);

        return data.getCustomData("setting." + group + "." + name, type, defaultValue);
    }

    public abstract Object parseInput(UUID player, String input);

    public Object getDefaultValue(UUID player) {
        return null;
    }

    public List<String> getSuggestions(UUID player) {
        return null;
    }

}
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
package me.st28.flexseries.flexlib.player.settings;

import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerData;
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
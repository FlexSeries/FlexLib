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

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PlayerSettingsManager extends FlexModule<FlexLib> {

    private final Map<String, Map<String, Setting>> settings = new HashMap<>();

    public PlayerSettingsManager(FlexLib plugin) {
        super(plugin, "player_settings", "API for per-player settings", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    public Map<String, Setting> getSettings(String group) {
        Validate.notNull(group, "Group cannot be null.");
        group = group.toLowerCase();
        return !settings.containsKey(group) ? null : Collections.unmodifiableMap(settings.get(group));
    }

    public Setting getSetting(String group, String name) {
        Validate.notNull(name, "Name cannot be null.");
        Map<String, Setting> groupMap = getSettings(group);
        return groupMap == null ? null : groupMap.get(name.toLowerCase());
    }

    public boolean registerSetting(Setting setting) {
        Validate.notNull(setting, "Setting cannot be null.");

        String group = setting.getGroup().toLowerCase();

        if (!settings.containsKey(group)) {
            settings.put(group, new HashMap<>());
        }

        Map<String, Setting> groupMap = settings.get(group);

        String name = setting.getName().toLowerCase();

        if (groupMap.containsKey(name)) {
            return false;
        }

        groupMap.put(name, setting);
        LogHelper.info(this, "Registered player setting '" + setting.getGroup() + "/" + setting.getName() + "'.");
        return true;
    }

}
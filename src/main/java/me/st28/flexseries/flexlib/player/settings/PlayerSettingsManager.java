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
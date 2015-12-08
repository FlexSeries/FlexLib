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
package me.st28.flexseries.flexlib.player.settings.defaults;

import me.st28.flexseries.flexlib.utils.BooleanUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BooleanSetting extends ToggleableSetting {

    private boolean defaultValue;

    public BooleanSetting(String group, String name, boolean isTransient) {
        this(group, name, isTransient, false);
    }

    public BooleanSetting(String group, String name, boolean isTransient, boolean defaultValue) {
        super(group, name, isTransient);

        this.defaultValue = defaultValue;
    }

    @Override
    public Object parseInput(UUID player, String input) {
        return BooleanUtils.parseBoolean(input);
    }

    @Override
    public void toggle(UUID player) {
        setValueDirect(player, !getValue(player, Boolean.class, (boolean) getDefaultValue(player)));
    }

    @Override
    public boolean isPositiveValue(Object value) {
        return (boolean) value;
    }

    @Override
    public Object getDefaultValue(UUID player) {
        return defaultValue;
    }

    @Override
    public List<String> getSuggestions(UUID player) {
        return Arrays.asList("true", "false");
    }

}
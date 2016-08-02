/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Base for storing different types of key, value data and easily retrieving it.
 */
public class GenericDataContainer {

    protected final Map<String, Object> data = new HashMap<>();

    public boolean isSet(String name) {
        return data.containsKey(name);
    }

    public Object get(String name) {
        return get(name, Object.class);
    }

    public Object get(String name, Object defaultValue) {
        return get(name, Object.class, defaultValue);
    }

    public <T> T get(String name, Class<T> type) {
        return get(name, type, null);
    }

    public <T> T get(String name, Class<T> type, T defaultValue) {
        return !isSet(name) ? defaultValue : (T) data.get(name);
    }

    public boolean getBoolean(String name) {
        return (boolean) data.get(name);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return get(name, boolean.class, defaultValue);
    }

    public String getString(String name) {
        return (String) data.get(name);
    }

    public String getString(String name, String defaultValue) {
        return get(name, String.class, defaultValue);
    }

    public int getInteger(String name) {
        return (int) data.get(name);
    }

    public int getInteger(String name, int defaultValue) {
        return get(name, int.class, defaultValue);
    }

    public double getDouble(String name) {
        return (double) data.get(name);
    }

    public double getDouble(String name, double defaultValue) {
        return get(name, double.class, defaultValue);
    }

}
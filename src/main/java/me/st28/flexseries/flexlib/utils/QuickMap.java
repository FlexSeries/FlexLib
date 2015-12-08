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
package me.st28.flexseries.flexlib.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows for one line map creation.
 */
public class QuickMap<K, V> {

    protected final Map<K, V> map;

    /**
     * Creates a QuickMap instance that uses a HashMap internally.
     */
    public QuickMap() {
        map = new HashMap<>();
    }

    /**
     * Creates a QuickMap instance that uses a HashMap internally and has an initial key and value.
     */
    public QuickMap(K initialKey, V initialValue) {
        map = new HashMap<>();
        map.put(initialKey, initialValue);
    }

    /**
     * Creates a QuickMap instance that uses a custom map type internally.
     */
    public QuickMap(Map<K, V> map) {
        this.map = map;
    }

    /**
     * Utility method for putting a key value pair into the map.
     *
     * @return The map instance, for chaining.
     */
    public QuickMap<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Utility method for putting multiple key value pairs into the map.
     *
     * @return The map instance, for chaining.
     */
    public QuickMap<K, V> putAll(Map<K, V> values) {
        map.putAll(values);
        return this;
    }

    /**
     * @return the underlying map instance.
     */
    public Map<K, V> getMap() {
        return map;
    }

}
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
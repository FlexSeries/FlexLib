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
package me.st28.flexseries.flexlib.plugin.module;

/**
 * Represents the status of a {@link FlexModule}.
 */
public enum ModuleStatus {

    /**
     * The module is waiting to be enabled.
     */
    PENDING,

    /**
     * The module is loading.
     */
    LOADING,

    /**
     * The module is disabled.
     */
    DISABLED,

    /**
     * The module is disabled due to an error while it was enabling.
     */
    DISABLED_ERROR,

    /**
     * The module is disabled due to a missing dependency.
     */
    DISABLED_DEPENDENCY,

    /**
     * The module is enabled.
     */
    ENABLED,

    /**
     * The module is enabled, but encountered errors while reloading.
     */
    ENABLED_ERROR,

    /**
     * The module is reloading.
     */
    RELOADING,

    /**
     * The module is unloading.
     */
    UNLOADING;

    public boolean isEnabled() {
        return this == ENABLED || this == ENABLED_ERROR;
    }

}
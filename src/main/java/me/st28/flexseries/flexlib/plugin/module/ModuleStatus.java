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
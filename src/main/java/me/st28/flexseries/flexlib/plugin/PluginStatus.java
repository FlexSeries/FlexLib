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
package me.st28.flexseries.flexlib.plugin;

public enum PluginStatus {

    /**
     * The plugin is waiting to be loaded.
     */
    PENDING,

    /**
     * The plugin is loading.
     */
    LOADING,

    /**
     * The plugin is enabling.
     */
    ENABLING,

    /**
     * The plugin is enabled.
     */
    ENABLED,

    /**
     * The plugin is enabled, but encountered errors while enabling.
     */
    ENABLED_ERROR,

    /**
     * The plugin is reloading.
     */
    RELOADING,

    /**
     * The plugin is disabling.
     */
    DISABLING;

}
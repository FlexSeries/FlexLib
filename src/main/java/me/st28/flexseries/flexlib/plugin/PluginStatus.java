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
package me.st28.flexseries.flexlib.plugin;

/**
 * Represents the status of a {@link FlexPlugin}.
 */
public enum PluginStatus {

    /**
     * The plugin has begun the loading process.
     */
    LOADING,

    /**
     * The plugin encountered errors while starting.
     */
    LOADING_ERROR,

    /**
     * The plugin has begun the enabling process.
     */
    ENABLING,

    /**
     * The plugin encountered errors while enabling.
     */
    ENABLING_ERROR,

    /**
     * The plugin has loaded
     */
    LOADED_ERROR,

    /**
     * The plugin is enabled.
     */
    ENABLED,

    /**
     * The plugin encountered errors while loading.
     */
    ENABLED_ERROR,

    /**
     * The plugin has begun the disabling process.
     */
    DISABLING;

}
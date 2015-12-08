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

import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Represents a reference to a module based on a plugin and module name.
 */
public final class ModuleReference {

    private final String plugin;
    private final String module;

    /**
     * @param plugin The plugin that owns the module. Null if the module is global.
     * @param module The module name. Cannot be null.
     */
    public ModuleReference(String plugin, String module) {
        Validate.notNull(module, "Module cannot be null.");

        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public String toString() {
        return plugin + "." + module;
    }

    /**
     * @return The referenced module.<br />
     *         Null if no module matching this reference was found.
     */
    public FlexModule getModule() {
        if (plugin == null) {
            // Try global
            for (FlexModule glModule : FlexPlugin.getGlobalModules()) {
                if (glModule.name.equals(module)) {
                    return glModule;
                }
            }
            return null;
        }

        // Try plugin-specific
        for (Plugin pluginInst : Bukkit.getPluginManager().getPlugins()) {
            if (pluginInst.getName().equals(plugin)) {
                if (pluginInst instanceof FlexPlugin) {
                    FlexPlugin flPlugin = (FlexPlugin) pluginInst;

                    for (FlexModule flModule : flPlugin.getModules()) {
                        if (flModule.name.equals(module)) {
                            return flModule;
                        }
                    }
                }
            }
        }
        return null;
    }

}
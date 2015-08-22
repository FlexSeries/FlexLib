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
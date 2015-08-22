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

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

/**
 * Handles a feature of a {@link FlexPlugin}.
 *
 * @param <T> The {@link FlexPlugin} that owns this module.
 */
public abstract class FlexModule<T extends FlexPlugin> {

    private ModuleStatus status = ModuleStatus.PENDING;

    protected final T plugin;
    protected final String name;
    protected final String description;
    protected final ModuleDescriptor descriptor;

    public FlexModule(T plugin, String name, String description, ModuleDescriptor descriptor) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        Validate.notNull(descriptor, "Descriptor cannot be null.");

        this.plugin = plugin;
        this.name = name;

        if (description == null) {
            this.description = "(no description set)";
        } else {
            this.description = description;
        }

        this.descriptor = descriptor;
        descriptor.lock();
    }

    /**
     * @return the {@link ModuleStatus} of this module.
     */
    public final ModuleStatus getStatus() {
        return status;
    }

    /**
     * @return the {@link FlexPlugin} that owns this module.
     */
    public final T getPlugin() {
        return plugin;
    }

    /**
     * @return the name of this module.
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the description of this module.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @return the {@link ModuleDescriptor} for this module.
     */
    public final ModuleDescriptor getDescriptor() {
        return descriptor;
    }

    public final void onEnable() {
        status = ModuleStatus.LOADING;

        try {
            handleEnable();
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while enabling module '" + name + "'", ex);
        }

        try {
            handleReload();
        } catch (Exception ex) {
            status = ModuleStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading module '" + name + "'", ex);
        }

        status = ModuleStatus.ENABLED;
    }

    public final void onReload() {
        status = ModuleStatus.RELOADING;

        try {
            handleReload();
        } catch (Exception ex) {
            status = ModuleStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading module '" + name + "'", ex);
        }

        status = ModuleStatus.ENABLED;
    }

    public final void onSave(boolean async) {
        try {
            handleSave(async);
        } catch (Exception ex) {
            throw new RuntimeException("An exception occurred while saving module '" + name + "'", ex);
        }
    }

    public final void onDisable() {
        status = ModuleStatus.UNLOADING;

        try {
            handleSave(false);
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while saving module '" + name + "'", ex);
        }

        try {
            handleDisable();
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while disabling module '" + name + "'", ex);
        }

        status = ModuleStatus.DISABLED;
    }

    /**
     * Handles custom enable tasks. This will only be called once.
     */
    protected void handleEnable() {}

    /**
     * Handles custom reload tasks.
     */
    protected void handleReload() {}

    /**
     * Handles custom module save tasks.
     *
     * @param async If true, should save asynchronously (where applicable).
     */
    protected void handleSave(boolean async) {}

    /**
     * Handles custom module disable tasks. This will only be called once.<br />
     * {@link #handleSave(boolean)} will be called automatically, so this should not call it again.
     */
    protected void handleDisable() {}

}
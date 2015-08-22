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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains information about a module.
 */
public final class ModuleDescriptor {

    private boolean isLocked = false;

    /**
     * If true, will not load this module unless it's required as a dependency by any other module.<br />
     * Should only be set to false if this module must be loaded.
     */
    private boolean smartLoad = true;

    /**
     * If true, only a single instance of the module can be created across all plugins.
     */
    private boolean global = true;

    /**
     * Optional dependencies for this module.
     */
    private final Set<ModuleReference> softDependencies = new HashSet<>();

    /**
     * Required dependencies for this module.
     */
    private final Set<ModuleReference> hardDependencies = new HashSet<>();

    public final void lock() {
        isLocked = true;
    }

    private void checkLock() {
        if (isLocked) {
            throw new IllegalStateException("No longer accepting changes.");
        }
    }

    /**
     * @see #smartLoad
     */
    public ModuleDescriptor setSmartLoad(boolean smartLoad) {
        checkLock();
        this.smartLoad = smartLoad;
        return this;
    }

    /**
     * @see #smartLoad
     */
    public boolean smartLoad() {
        return smartLoad;
    }

    /**
     * @see #global
     */
    public ModuleDescriptor setGlobal(boolean global) {
        checkLock();
        this.global = global;
        return this;
    }

    /**
     * @see #global
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * @see #softDependencies
     */
    public ModuleDescriptor addSoftDependency(ModuleReference module) {
        checkLock();
        softDependencies.add(module);
        return this;
    }

    /**
     * @see #softDependencies
     */
    public Set<ModuleReference> getSoftDependencies() {
        return Collections.unmodifiableSet(softDependencies);
    }

    /**
     * @see #hardDependencies
     */
    public ModuleDescriptor addHardDependency(ModuleReference module) {
        checkLock();
        hardDependencies.add(module);
        return this;
    }

    /**
     * @see #hardDependencies
     */
    public Set<ModuleReference> getHardDependencies() {
        return Collections.unmodifiableSet(hardDependencies);
    }

    /**
     * @see #softDependencies
     * @see #hardDependencies
     */
    public Set<ModuleReference> getAllDependencies() {
        Set<ModuleReference> deps = new HashSet<>();

        deps.addAll(softDependencies);
        deps.addAll(hardDependencies);

        return deps;
    }

}
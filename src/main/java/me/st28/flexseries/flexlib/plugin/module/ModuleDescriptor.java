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
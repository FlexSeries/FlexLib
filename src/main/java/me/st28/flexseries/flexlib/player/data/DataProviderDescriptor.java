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
package me.st28.flexseries.flexlib.player.data;

import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DataProviderDescriptor {

    private boolean isLocked = false;

    /**
     * If true, this data provider only provides data for online players.
     */
    private boolean onlineOnly = true;

    /**
     * If true and {@link #onlineOnly} is false, this provider will never be unloaded unless forced.
     */
    private boolean persistent = false;

    /**
     * If true, the player will be denied access if this provider fails to load their data.
     * Otherwise, the player will still be able to join.
     */
    private boolean mustLoad = true;

    /**
     * Other data providers that are required for this provider to load.
     */
    private final Set<ModuleReference> hardDependencies = new HashSet<>();

    /**
     * Other data providers that are optional for this provider to load.
     */
    private final Set<ModuleReference> softDependencies = new HashSet<>();

    public void lock() {
        checkLock();
        this.isLocked = true;
    }

    private void checkLock() {
        if (isLocked) {
            throw new IllegalStateException("No longer accepting changes.");
        }
    }

    /**
     * @see #onlineOnly
     */
    public boolean onlineOnly() {
        return onlineOnly;
    }

    /**
     * @see #onlineOnly
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor onlineOnly(boolean state) {
        checkLock();

        this.onlineOnly = state;
        return this;
    }

    /**
     * @see #persistent
     */
    public boolean persistent() {
        return persistent;
    }

    /**
     * @see #persistent
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor persistent(boolean state) {
        checkLock();

        this.persistent = state;
        return this;
    }

    /**
     * @see #mustLoad
     */
    public boolean mustLoad() {
        return mustLoad;
    }

    /**
     * @see #mustLoad
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor mustLoad(boolean mustLoad) {
        checkLock();

        this.mustLoad = mustLoad;
        return this;
    }

    /**
     * @see #hardDependencies
     */
    public Set<ModuleReference> getHardDependencies() {
        return Collections.unmodifiableSet(hardDependencies);
    }

    /**
     * @see #hardDependencies
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor addHardDependency(ModuleReference... modules) {
        checkLock();

        for (ModuleReference ref : modules) {
            final FlexModule module = ref.getModule();
            if (module != null && !(module instanceof PlayerDataProvider)) {
                throw new IllegalArgumentException("Module is not a PlayerDataProvider.");
            }

            hardDependencies.add(ref);
        }

        return this;
    }

    /**
     * @see #softDependencies
     */
    public Set<ModuleReference> getSoftDependencies() {
        return Collections.unmodifiableSet(softDependencies);
    }

    /**
     * @see #softDependencies
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor addSoftDependency(ModuleReference... modules) {
        checkLock();

        for (ModuleReference ref : modules) {
            final FlexModule module = ref.getModule();
            if (module != null && !(module instanceof PlayerDataProvider)) {
                throw new IllegalArgumentException("Module is not a PlayerDataProvider.");
            }

            softDependencies.add(ref);
        }

        return this;
    }

}
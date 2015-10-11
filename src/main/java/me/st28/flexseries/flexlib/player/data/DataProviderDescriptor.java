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
package me.st28.flexseries.flexlib.player.data;

import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DataProviderDescriptor {

    private boolean isLocked = false;

    private UnloadAlertPolicy unloadAlert = UnloadAlertPolicy.QUIT;

    private final Set<ModuleReference> hardDependencies = new HashSet<>();
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
     * @see {@link UnloadAlertPolicy}.
     */
    public UnloadAlertPolicy unloadAlertPolicy() {
        return unloadAlert;
    }

    /**
     * @see {@link UnloadAlertPolicy}
     * @return This instance, for chaining.
     */
    public DataProviderDescriptor unloadAlertPolicy(UnloadAlertPolicy policy) {
        this.unloadAlert = policy;
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

    // ------------------------------------------------------------------------------------------ //

    /**
     * Represents when a data provider should be alerted when to unload a player.
     */
    public enum UnloadAlertPolicy {

        /**
         * {@link PlayerDataProvider#unloadPlayer(PlayerLoader, PlayerData, UUID, String)} will be
         * called for both {@link #AUTO} and {@link #QUIT}.
         */
        BOTH,

        /**
         * {@link PlayerDataProvider#unloadPlayer(PlayerLoader, PlayerData, UUID, String)} will be
         * called when the auto unloader executes.
         */
        AUTO,

        /**
         * {@link PlayerDataProvider#unloadPlayer(PlayerLoader, PlayerData, UUID, String)} will be
         * called when a player quits the server.
         */
        QUIT;

        public boolean fuzzyEquals(UnloadAlertPolicy other) {
            return this == BOTH || other == BOTH || this == other;

        }

    }

}
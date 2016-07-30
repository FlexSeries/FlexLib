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
package me.st28.flexseries.flexlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ArgumentCallback<V> {

    private final boolean isSync;

    /**
     * Constructs an argument callback that is scheduled to be called on the same thread as the calling thread.
     */
    public ArgumentCallback() {
        this(Bukkit.isPrimaryThread());
    }

    /**
     * @param isSync True to call the callback on the main Bukkit thread.
     */
    public ArgumentCallback(boolean isSync) {
        this.isSync = isSync;
    }

    public final void call(Class<? extends JavaPlugin> plugin, V arg) {
        if (isSync && !Bukkit.isPrimaryThread()) {
            // Callback is synchronous, but we're not on the main thread.
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(plugin), () -> handle(arg));
        } else if (!isSync && Bukkit.isPrimaryThread()) {
            // Callback is asynchronous, but we're on the main thread.
            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(plugin), () -> handle(arg));
        } else {
            // Call directly.
            handle(arg);
        }
    }

    public abstract void handle(V arg);

}
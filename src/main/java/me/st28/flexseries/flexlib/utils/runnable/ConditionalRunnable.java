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
package me.st28.flexseries.flexlib.utils.runnable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ConditionalRunnable implements Runnable {

    public void scheduleSync(JavaPlugin plugin) {
        Bukkit.getScheduler().runTask(plugin, this);
    }

    public void scheduleAsync(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this);
    }

    @Override
    public void run() {
        if (areConditionsMet()) {
            handleRun();
        }
    }

    public abstract boolean areConditionsMet();

    public abstract void handleRun();

}
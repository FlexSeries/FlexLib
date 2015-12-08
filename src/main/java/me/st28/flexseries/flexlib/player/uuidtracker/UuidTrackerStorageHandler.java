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
package me.st28.flexseries.flexlib.player.uuidtracker;

import me.st28.flexseries.flexlib.utils.ArgumentCallback;
import me.st28.flexseries.flexlib.utils.TaskChain;
import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;

import java.util.Set;
import java.util.UUID;

abstract class UuidTrackerStorageHandler {

    final PlayerUuidTracker manager;
    final String name;

    UuidTrackerStorageHandler(PlayerUuidTracker manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    void enable() {}

    /**
     * Loads all entries.
     */
    abstract Set<UuidEntry> loadAll();

    /**
     * Loads a single entry.
     */
    abstract UuidEntry loadSingle(UUID uuid);

    /**
     * Loads a single entry asynchronously.
     */
    void loadSingleAsync(UUID uuid, ArgumentCallback<UuidEntry> callback) {
        new TaskChain().add(new AsyncGenericTask() {
            @Override
            protected void run() {
                callback.call(loadSingle(uuid));
            }
        }).execute();
    }

    /**
     * Queues an update for an entry.
     * @param entry Should not be a complete UuidEntry but should rather contain only the new info.
     */
    abstract void queueUpdate(UuidEntry entry);

    /**
     * Saves the given entries.
     */
    abstract void save(boolean async, Set<UuidEntry> entries);

}
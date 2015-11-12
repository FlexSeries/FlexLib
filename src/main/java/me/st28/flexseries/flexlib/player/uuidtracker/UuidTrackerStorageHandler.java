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
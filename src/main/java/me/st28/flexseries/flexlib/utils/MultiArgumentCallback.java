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
package me.st28.flexseries.flexlib.utils;

import org.bukkit.Bukkit;

import java.util.Map;

/**
 * A callback that accepts a map of arguments.
 */
public abstract class MultiArgumentCallback {

    /**
     * Runs the callback.
     */
    public final void call(final Map<String, Object> arguments) {
        if (isSynchronous() && !Bukkit.isPrimaryThread()) {
            // Callback is synchronous but we're not on the main thread.
            new TaskChain().add(new TaskChain.GenericTask() {
                @Override
                protected void run() {
                    MultiArgumentCallback.this.run(arguments);
                }
            }).execute();
        } else {
            // Call from same thread
            run(arguments);
        }
    }

    private boolean isSynchronous;

    public MultiArgumentCallback() {
        this(false);
    }

    public MultiArgumentCallback(boolean isSynchronous) {
        this.isSynchronous = isSynchronous;
    }

    /**
     * Specifies whether or not the callback should be run synchronously.
     *
     * @return True to run the callback synchronously.<br />
     *         False to run the callback in whatever thread the executor is in.
     */
    public boolean isSynchronous() {
        return isSynchronous;
    }

    /**
     * Runs the callback.
     *
     * @param arguments The argument(s) from the executor that the callback requires.
     */
    protected abstract void run(Map<String, Object> arguments);

}
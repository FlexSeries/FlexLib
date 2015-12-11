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

import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.GenericTask;
import org.bukkit.Bukkit;

/**
 * A future task to be run once something finishes executing.
 *
 * @param <T> The type of argument the callback requires for the {@link #run(Object)} method.
 */
public abstract class ArgumentCallback<T> {

    /**
     * Runs the callback.
     */
    public final void call(final T argument) {
        if (isSynchronous() && !Bukkit.isPrimaryThread()) {
            // Callback is synchronous but we're not on the main thread.
            new TaskChain().add(new GenericTask() {
                @Override
                protected void run() {
                    ArgumentCallback.this.run(argument);
                }
            }).execute();
        } else if (Bukkit.isPrimaryThread()) {
            // Callback is asynchronous but we're on the main thread
            new TaskChain().add(new AsyncGenericTask() {
                @Override
                protected void run() {
                    ArgumentCallback.this.run(argument);
                }
            }).execute();
        } else {
            // Call directly, already async
            run(argument);
        }
    }

    private boolean isSynchronous;

    public ArgumentCallback() {
        this(false);
    }

    public ArgumentCallback(boolean isSynchronous) {
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
     * @param argument An argument from the executor that the callback requires.
     */
    protected abstract void run(T argument);

}
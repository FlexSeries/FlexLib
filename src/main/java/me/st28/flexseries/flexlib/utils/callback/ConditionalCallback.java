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
package me.st28.flexseries.flexlib.utils.callback;

import me.st28.flexseries.flexlib.utils.TaskChain;
import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.GenericTask;
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * A callback that is executed only if the proper conditions are met.
 */
public abstract class ConditionalCallback {

    /**
     * Runs the callback.
     */
    public final void call(final Map<String, Object> arguments) {
        if (isSynchronous() && !Bukkit.isPrimaryThread()) {
            // Callback is synchronous but we're not on the main thread.
            new TaskChain().add(new GenericTask() {
                @Override
                protected void run() {
                    ConditionalCallback.this.attemptExecution(arguments);
                }
            }).execute();
        } else if (Bukkit.isPrimaryThread()) {
            // Callback is asynchronous but we're on the main thread
            new TaskChain().add(new AsyncGenericTask() {
                @Override
                protected void run() {
                    ConditionalCallback.this.attemptExecution(arguments);
                }
            }).execute();
        } else {
            // Call directly, already async
            attemptExecution(arguments);
        }
    }

    private void attemptExecution(final Map<String, Object> arguments) {
        if (canExecute(arguments)) {
            run(arguments);
        }
    }

    // ------------------------------------------------------------------------------------------ //

    private boolean isSynchronous;

    public ConditionalCallback() {
        this(false);
    }

    public ConditionalCallback(boolean isSynchronous) {
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
     * Performs a check to see if this callback can be executed.
     *
     * @param arguments The argument(s) from the executor that the callback requires.
     * @return True if the callback can be executed.<br />
     *         False if otherwise.
     */
    protected abstract boolean canExecute(final Map<String, Object> arguments);

    /**
     * Runs the callback.
     *
     * @param arguments The argument(s) from the executor that the callback requires.
     */
    protected abstract void run(final Map<String, Object> arguments);

}
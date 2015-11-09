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
package me.st28.flexseries.flexlib.utils;

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
            new TaskChain().add(new TaskChain.GenericTask() {
                @Override
                protected void run() {
                    ArgumentCallback.this.run(argument);
                }
            }).execute();
        } else {
            // Call from same thread
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
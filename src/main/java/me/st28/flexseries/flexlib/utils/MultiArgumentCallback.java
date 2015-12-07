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
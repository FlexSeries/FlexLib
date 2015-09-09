package me.st28.flexseries.flexlib.hook;

public final class HookDisabledException extends RuntimeException {

    public HookDisabledException(Hook hook) {
        super("Hook '" + hook.getPluginName() + "' is disabled.");
    }

}
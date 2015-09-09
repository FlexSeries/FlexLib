package me.st28.flexseries.flexlib.hook;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HookManager extends FlexModule<FlexLib> {

    private final Map<String, Hook> hooks = new HashMap<>();
    private final Map<Class<? extends Hook>, Hook> hookClasses = new HashMap<>();

    public HookManager(FlexLib plugin) {
        super(plugin, "hooks", "Manages external plugin hooks", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleEnable() {
        registerHook(new VaultHook());
    }

    public boolean registerHook(Hook hook) {
        Validate.notNull(hook, "Hook cannot be null.");

        final String plugin = hook.getPluginName().toLowerCase();

        if (hooks.containsKey(plugin)) {
            return false;
        }

        hooks.put(plugin, hook);
        hookClasses.put(hook.getClass(), hook);
        return true;
    }

    /**
     * @return An unmodifiable collection of all registered hooks.
     */
    public Collection<Hook> getHooks() {
        return Collections.unmodifiableCollection(hooks.values());
    }

    private void checkHookStatus(Hook hook) {
        if (!hook.isEnabled()) {
            throw new HookDisabledException(hook);
        }
    }

    public Hook getHook(String plugin) {
        Validate.notNull(plugin, "Plugin cannot be null.");

        Hook hook = hooks.get(plugin.toLowerCase());
        if (hook == null) {
            throw new IllegalArgumentException("No Hook for plugin '" + plugin + "' is registered.");
        }

        return hook;
    }

    public <T extends Hook> T getHook(Class<T> clazz) {
        Validate.notNull(clazz, "Class cannot be null.");

        T hook = (T) hookClasses.get(clazz);
        if (hook == null) {
            throw new IllegalArgumentException("Class '" + clazz.getCanonicalName() + "' is not a registered Hook.");
        }

        checkHookStatus(hook);
        return hook;
    }

}
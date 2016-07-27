package me.st28.flexseries.flexlib.plugin;

/**
 * Represents the status of a {@link FlexModule}.
 */
public enum ModuleStatus {

    /**
     * The module is waiting to be enabled.
     */
    PENDING,

    /**
     * The module is currently loading.
     */
    LOADING,

    /**
     * The module is disabled via the plugin's configuration.
     */
    DISABLED,

    /**
     * The module is disabled due to errors encountered while enabling.
     */
    DISABLED_ERROR,

    /**
     * The module is disabled due to a missing dependency.
     */
    DISABLED_DEPENDENCY,

    /**
     * The module is enabled.
     */
    ENABLED,

    /**
     * the module is enabled, but encountered errors while reloading.
     */
    ENABLED_ERROR,

    /**
     * The module is reloading.
     */
    RELOADING,

    /**
     * The module is unloading.
     */
    UNLOADING;

    public boolean isEnabled() {
        return this == ENABLED || this == ENABLED_ERROR;
    }

}
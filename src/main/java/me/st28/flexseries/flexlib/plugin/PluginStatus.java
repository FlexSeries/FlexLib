package me.st28.flexseries.flexlib.plugin;

public enum PluginStatus {

    /**
     * The plugin is waiting to be loaded.
     */
    PENDING,

    /**
     * The plugin is loading.
     */
    LOADING,

    /**
     * The plugin is enabling.
     */
    ENABLING,

    /**
     * The plugin is enabled.
     */
    ENABLED,

    /**
     * The plugin is enabled, but encountered errors while enabling.
     */
    ENABLED_ERROR,

    /**
     * The plugin is reloading.
     */
    RELOADING,

    /**
     * The plugin is disabling.
     */
    DISABLING;

}
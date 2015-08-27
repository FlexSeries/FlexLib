package me.st28.flexseries.flexlib.message.list;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class ListManager extends FlexModule<FlexLib> {

    static ListManager getInstance() {
        return FlexPlugin.getGlobalModule(ListManager.class);
    }

    private static final String UNKNOWN_ELEMENT_FORMAT = "&cUnknown element format: &6{NAME}&c.";

    int pageItems;
    int lineLength;

    String msgNextPage;
    String msgNoElements;

    private final Map<String, String> formatsElement = new HashMap<>();
    private final Map<String, ListHeader> formatsHeader = new HashMap<>();

    public ListManager(FlexLib plugin) {
        super(
                plugin,
                "lists",
                "Handles the creation and formatting of lists",
                new ModuleDescriptor()
                        .setGlobal(true)
                        .setSmartLoad(false)
                        .addHardDependency(new ModuleReference(null, "messages-master"))
        );
    }

    @Override
    protected void handleReload() {
        ConfigurationSection config = getConfig();

        pageItems = config.getInt("general config.page items", 8);
        lineLength = config.getInt("general config.line length", 52);

        msgNextPage = config.getString("messages.next page", "&c&oType &6&o/{COMMAND} {NEXTPAGE} &c&oto view the next page.");
        msgNoElements = config.getString("messages.no elements", "&c&oNothing here.");

        formatsElement.clear();
        ConfigurationSection elementSec = config.getConfigurationSection("formats.elements");
        if (elementSec != null) {
            for (String key : elementSec.getKeys(false)) {
                if (formatsElement.containsKey(key.toLowerCase())) {
                    LogHelper.warning(this, "A element format named '" + key.toLowerCase() + "' is already loaded.");
                    continue;
                }

                formatsElement.put(key.toLowerCase(), elementSec.getString(key));
            }
        }

        formatsHeader.clear();
        ConfigurationSection headerSec = config.getConfigurationSection("formats.headers");
        if (headerSec != null) {
            for (String key : headerSec.getKeys(false)) {
                if (formatsHeader.containsKey(key.toLowerCase())) {
                    LogHelper.warning(this, "A header format named '" + key.toLowerCase() + "' is already loaded.");
                    continue;
                }

                ListHeader header;
                try {
                    header = new ListHeader(headerSec.getConfigurationSection(key));
                } catch (Exception ex) {
                    LogHelper.warning(this, "An exception occurred while loading header '" + key.toLowerCase() + "'");
                    ex.printStackTrace();
                    continue;
                }
                formatsHeader.put(key.toLowerCase(), header);
            }
        }
    }

    @Override
    protected void handleSave(boolean async) {
        ConfigurationSection config = getConfig().getConfigurationSection("formats.elements");

        for (Entry<String, String> entry : formatsElement.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return a header format matching a given name.
     */
    public ListHeader getHeaderFormat(String name) {
        Validate.notNull(name, "Name cannot be null.");
        name = name.toLowerCase();
        return !formatsHeader.containsKey(name) ? new ListHeader(name) : formatsHeader.get(name);
    }

    /**
     * @return an element format matching a given name.
     */
    public String getElementFormat(String name) {
        Validate.notNull(name, "Name cannot be null.");
        name = name.toLowerCase();
        return !formatsElement.containsKey(name) ? UNKNOWN_ELEMENT_FORMAT.replace("{NAME}", name) : formatsElement.get(name);
    }

    /**
     * Creates an element format if it doesn't already exist in the configuration file.
     *
     * @param name The name of the format.
     * @param defaultFormat The default format.  This will be saved to the configuration file if nothing is already set.
     */
    public void createElementFormat(String name, String defaultFormat) {
        Validate.notNull(name, "Name cannot be null.");
        Validate.notNull(defaultFormat, "Default format cannot be null.");

        name = name.toLowerCase();
        if (!formatsElement.containsKey(name)) {
            formatsElement.put(name, defaultFormat);
        }
    }

}
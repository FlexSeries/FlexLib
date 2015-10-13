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
package me.st28.flexseries.flexlib.player;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.message.reference.PlainMessageReference;
import me.st28.flexseries.flexlib.player.data.DataProviderDescriptor;
import me.st28.flexseries.flexlib.player.data.PlayerDataProvider;
import me.st28.flexseries.flexlib.player.data.PlayerLoader;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexlib.utils.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerManager extends FlexModule<FlexLib> implements Listener {

    /* Configuration Options */
    private final List<String> loginMessageOrder = new ArrayList<>();

    private int saveBatchSize;
    private int saveBatchInterval;

    private boolean enableJoinMessageChange;
    private boolean enableQuitMessageChange;

    private boolean dataSaveIps;

    private int dataUnloadAge;
    /* End Configuration Options */

    private BukkitRunnable autoUnloadTask;

    private final Map<PlayerDataProvider, DataProviderDescriptor> dataProviders = new HashMap<>();
    private final Map<UUID, PlayerData> loadedData = new HashMap<>();

    public PlayerManager(FlexLib plugin) {
        super(plugin, "players", "Provides a unified player data storage system", new ModuleDescriptor().setGlobal(true).setSmartLoad(false));
    }

    @Override
    protected void handleReload() {
        final ConfigurationSection config = getConfig();

        // TODO: Abstract save batches for use in other modules
        saveBatchSize = config.getInt("auto save.batch size", 20);
        saveBatchInterval = config.getInt("auto save.batch interval", 2);

        enableJoinMessageChange = config.getBoolean("modify join message", true);
        enableQuitMessageChange = config.getBoolean("modify quit message", true);

        dataSaveIps = config.getBoolean("player data.save ips", true);

        int autoUnloadInterval = config.getInt("auto unload.interval", 5);
        if (autoUnloadTask != null) {
            autoUnloadTask.cancel();
        }

        dataUnloadAge = config.getInt("auto unload.age", 300);

        if (autoUnloadInterval > 0) {
            autoUnloadTask = new BukkitRunnable() {
                @Override
                public void run() {
                    long curTime = System.currentTimeMillis();

                    List<UUID> toRemove = loadedData.entrySet().stream()
                            .filter(entry -> Bukkit.getPlayer(entry.getKey()) == null
                                    && (curTime - entry.getValue().lastAccessed) / 1000 >= dataUnloadAge)
                            .map(Entry::getKey)
                            .collect(Collectors.toList());

                    for (UUID uuid : toRemove) {
                        unloadPlayer(uuid, false);
                    }
                }
            };

            autoUnloadTask.runTaskTimer(plugin, autoUnloadInterval * 1200L, autoUnloadInterval * 1200L);
            LogHelper.info(this, "Running player auto unloader every " + TimeUtils.translateSeconds(autoUnloadInterval * 60) + ".");
        }

        loginMessageOrder.clear();
        loginMessageOrder.addAll(config.getStringList("player join.message order"));
    }

    @Override
    protected void handleSave(boolean async) {
        saveAllPlayers(true);
    }

    @Override
    protected void handleFinalSave() {
        saveAllPlayers(false);
    }

    private void saveAllPlayers(boolean batch) {
        if (!batch) {
            for (UUID uuid : loadedData.keySet()) {
                savePlayer(uuid);
            }
            return;
        }

        Queue<UUID> toSave = new PriorityQueue<>(loadedData.keySet());

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < saveBatchSize; i++) {
                    UUID uuid = toSave.poll();
                    if (uuid == null) {
                        cancel();
                        return;
                    }

                    savePlayer(uuid);
                }
            }
        };

        runnable.runTaskTimer(plugin, saveBatchInterval, saveBatchInterval);
    }

    @Override
    protected void handleDisable() {
        for (PlayerData data : loadedData.values()) {
            data.getLoader().unload(true);
        }
    }

    public Map<PlayerDataProvider, DataProviderDescriptor> getDataProviders() {
        return Collections.unmodifiableMap(dataProviders);
    }

    /**
     * @return True if the provider was registered successfully.<br />
     *         False if the provider is already registered.
     */
    public boolean registerDataProvider(PlayerDataProvider provider, DataProviderDescriptor descriptor) {
        Validate.notNull(provider, "Provider cannot be null.");
        Validate.isTrue(provider instanceof FlexModule, "Provider must be a FlexModule.");
        Validate.notNull(descriptor, "Descriptor cannot be null.");

        if (dataProviders.containsKey(provider)) {
            return false;
        }

        descriptor.lock();
        dataProviders.put(provider, descriptor);
        return true;
    }

    /**
     * @return True if the specified player is loaded.<br />
     *         False if the player is not loaded.
     */
    public boolean isPlayerLoaded(UUID uuid) {
        return loadedData.containsKey(uuid);
    }

    /**
     * Retrieves a player's data (and loads it if it's not already loaded).
     * @see #getPlayerData(UUID, boolean)
     */
    public PlayerData getPlayerData(UUID uuid) {
        return getPlayerData(uuid, true);
    }

    /**
     * @param forceLoad If true, will load the player's data if it is not already loaded.
     * @return A specified player's {@link PlayerData}.<br />
     *         Null if the player is not loaded and <code>forceLoad</code> is <code>false</code>.
     */
    public PlayerData getPlayerData(UUID uuid, boolean forceLoad) {
        Validate.notNull(uuid, "UUID cannot be null.");
        if (!isPlayerLoaded(uuid) && forceLoad) {
            // Load player
            loadPlayer(uuid);
        }

        return loadedData.get(uuid);
    }

    private void loadPlayer(UUID uuid) {
        if (isPlayerLoaded(uuid)) {
            loadedData.get(uuid).getLoader().load();

            return;
        }

        PlayerData data = new PlayerData(uuid, new YamlFileManager(getDataFolder() + File.separator + uuid.toString() + ".yml"));

        PlayerLoader loader = new PlayerLoader(new PlayerReference(uuid), data);

        data.setLoader(loader);

        loader.load();

        loadedData.put(uuid, data);
    }

    private void savePlayer(UUID uuid) {
        final PlayerData data = loadedData.get(uuid);

        if (data != null) {
            data.getLoader().save();
        }
    }

    private void unloadPlayer(UUID uuid, boolean force) {
        final PlayerData data = loadedData.get(uuid);

        if (data == null) {
            return;
        }

        try {
            data.getLoader().save();
            data.getLoader().unload(force);
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while unloading player '" + data.getUuid().toString() + "'", ex);
        }

        if (data.getLoader().getLoadedProviderCount() == 0) {
            loadedData.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final String joinMessage = e.getJoinMessage();

        loadPlayer(player.getUniqueId());
        final PlayerData data = getPlayerData(player.getUniqueId());

        if (data.firstJoin == null) {
            data.firstJoin = System.currentTimeMillis();

            data.setCustomData("firstJoin", true, true);
        }

        data.lastLogin = System.currentTimeMillis();

        if (dataSaveIps) {
            String curIp = player.getAddress().getAddress().getHostAddress();
            data.lastIp = curIp;
            if (!data.ips.contains(curIp)) {
                data.ips.add(curIp);
            }
        }

        PlayerExtendedJoinEvent newEvent = new PlayerExtendedJoinEvent(player, data);
        if (enableJoinMessageChange && joinMessage != null) {
            newEvent.setJoinMessage(new PlainMessageReference(joinMessage));
        }

        Bukkit.getPluginManager().callEvent(newEvent);

        data.setCustomData("firstJoin", false, true);

        if (enableJoinMessageChange) {
            e.setJoinMessage(null);

            for (Player op : Bukkit.getOnlinePlayers()) {
                MessageReference message = newEvent.getJoinMessage(op.getUniqueId());

                if (message != null) {
                    message.sendTo(op);
                }
            }
        }

        Map<String, MessageReference> loginMessages = newEvent.getLoginMessages();
        List<String> sent = new ArrayList<>();

        for (String identifier : loginMessageOrder) {
            MessageReference message = loginMessages.get(identifier);
            if (message != null) {
                message.sendTo(player);
                sent.add(identifier);
            }
        }

        if (sent.size() != loginMessages.size()) {
            for (Entry<String, MessageReference> entry : loginMessages.entrySet()) {
                if (!sent.contains(entry.getKey())) {
                    entry.getValue().sendTo(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        handlePlayerLeave(e.getPlayer(), e.getQuitMessage());

        if (enableQuitMessageChange) {
            e.setQuitMessage(null);
        }
    }

    private void handlePlayerLeave(Player player, String message) {
        PlayerExtendedLeaveEvent newLeaveEvent = new PlayerExtendedLeaveEvent(player, getPlayerData(player.getUniqueId()));
        if (enableQuitMessageChange && message != null) {
            newLeaveEvent.setLeaveMessage(new PlainMessageReference(message));
        }

        Bukkit.getPluginManager().callEvent(newLeaveEvent);

        if (enableQuitMessageChange) {
            for (Player op : Bukkit.getOnlinePlayers()) {
                MessageReference plMessage = newLeaveEvent.getLeaveMessage(op.getUniqueId());

                if (plMessage != null) {
                    plMessage.sendTo(op);
                }
            }
        }

        getPlayerData(player.getUniqueId()).lastLogout = System.currentTimeMillis();

        savePlayer(player.getUniqueId());

        unloadPlayer(player.getUniqueId(), false);
    }

}
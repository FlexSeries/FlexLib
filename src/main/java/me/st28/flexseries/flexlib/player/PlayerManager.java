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
import me.st28.flexseries.flexlib.utils.ArgumentCallback;
import me.st28.flexseries.flexlib.utils.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

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

                    List<UUID> toRemove = playerData.entrySet().stream()
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
            for (UUID uuid : playerData.keySet()) {
                savePlayer(uuid);
            }
            return;
        }

        Queue<UUID> toSave = new PriorityQueue<>(playerData.keySet());

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
        for (PlayerData data : playerData.values()) {
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
        return playerData.containsKey(uuid);
    }

    /**
     * @return The {@link PlayerData} instance for the specified player UUID.
     *         Null if the player's data isn't loaded.
     */
    public PlayerData getPlayerData(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return playerData.get(uuid);
    }

    /**
     * Retrieves a player's data. If their data isn't loaded, it will be loaded.
     * Due to player data loading being asynchronous, this method uses a callback to return the
     * player's data.
     */
    public void getOrLoadPlayerData(UUID uuid, ArgumentCallback<PlayerData> callback) {
        Validate.notNull(uuid, "UUID cannot be null.");
        Validate.notNull(callback, "Callback cannot be null.");

        if (!isPlayerLoaded(uuid)) {
            loadPlayer(uuid, new PlayerReference(uuid).getName());
        }
        callback.call(playerData.get(uuid));
    }

    private void savePlayer(UUID uuid) {
        final PlayerData data = playerData.get(uuid);

        if (data != null) {
            data.getLoader().save();
        }
    }

    private void loadPlayer(UUID uuid, String name) {
        LogHelper.debug(this, "Starting loading process for player '" + name + "' (" + uuid.toString() + ")");

        if (isPlayerLoaded(uuid)) {
            LogHelper.debug(this, "Player '" + name + "' (" + uuid.toString() + ") is already loaded. Stopping load.");
            return;
        }

        PlayerData data = new PlayerData(uuid, new YamlFileManager(getDataFolder() + File.separator + uuid.toString() + ".yml"));

        PlayerLoader loader = new PlayerLoader(new PlayerReference(uuid), data);

        data.setLoader(loader);
        playerData.put(uuid, data);

        loader.load();
    }

    private void unloadPlayer(UUID uuid, boolean force) {
        final PlayerData data = playerData.get(uuid);

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
            playerData.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerPreLogin_low(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (uuid == null || isPlayerLoaded(uuid)) {
            return;
        }

        loadPlayer(uuid, e.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin_highest(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (uuid == null) {
            return;
        }

        PlayerData data = this.playerData.get(uuid);
        if (!data.getLoader().loadedSuccessfully()) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your data failed to load. Please try again or contact an admin.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        PlayerLoader loader;
        if (!playerData.containsKey(uuid) || !(loader = playerData.get(uuid).getLoader()).loadedSuccessfully()) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your data failed to load. Please try again or contact an admin.");
            return;
        }

        loader.notifyJoin();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final String joinMessage = e.getJoinMessage();

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
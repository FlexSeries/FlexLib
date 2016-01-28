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
package me.st28.flexseries.flexlib.player.data;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleReference;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public final class PlayerLoader {

    private static PlayerManager getPlayerManager() {
        return FlexPlugin.getGlobalModule(PlayerManager.class);
    }

    private PlayerReference player;
    private final PlayerData data;

    private final Map<PlayerDataProvider, ProviderLoadStatus> providers = new HashMap<>();

    public PlayerLoader(PlayerReference player, PlayerData data) {
        Validate.notNull(player, "Player cannot be null.");
        this.player = player;

        this.data = data;
        Validate.notNull(data, "Data cannot be null.");
    }

    public UUID getUuid() {
        return player.getUuid();
    }

    public String getName() {
        return player.getName();
    }

    public PlayerData getData() {
        return data;
    }

    public void load() {
        LogHelper.debug(getPlayerManager(), "Initializing PlayerLoader for '" + player.getName() + "' (" + player.getUuid().toString() + ")");

        // First, add providers that haven't already been added.

        final Map<PlayerDataProvider, DataProviderDescriptor> dataProviders = FlexPlugin.getGlobalModule(PlayerManager.class).getDataProviders();

        for (Entry<PlayerDataProvider, DataProviderDescriptor> entry : dataProviders.entrySet()) {
            if (!providers.containsKey(entry.getKey())) {
                LogHelper.debug(getPlayerManager(), "Queuing data provider '" + entry.getKey().getClass().getCanonicalName() + "'");
                providers.put(entry.getKey(), ProviderLoadStatus.PENDING);
            }
        }

        // Second, start loading providers
        LogHelper.debug(getPlayerManager(), "Loading player '" + player.getName() + "' (" + player.getUuid().toString() + ")");

        Set<PlayerDataProvider> checked = new HashSet<>();

        for (Entry<PlayerDataProvider, DataProviderDescriptor> entry : dataProviders.entrySet()) {
            loadProvider(checked, entry.getKey(), dataProviders);
        }

        data.load();

        if (loadedSuccessfully()) {
            LogHelper.debug(getPlayerManager(), "Player '" + player.getName() + "' (" + player.getUuid().toString() + ") loaded successfully.");
        } else {
            LogHelper.debug(getPlayerManager(), "Player '" + player.getName() + "' (" + player.getUuid().toString() + ") encountered problems while loading.");
        }
    }

    private void loadProvider(Set<PlayerDataProvider> checked, PlayerDataProvider provider, Map<PlayerDataProvider, DataProviderDescriptor> descriptors) {
        if (checked.contains(provider)) {
            return;
        }

        checked.add(provider);

        if (providers.get(provider) == ProviderLoadStatus.SUCCESS) {
            LogHelper.debug(getPlayerManager(), "Provider '" + provider.getClass().getCanonicalName() + "' already loaded, skipping");
            return;
        }

        LogHelper.debug(getPlayerManager(), "Loading provider '" + provider.getClass().getCanonicalName() + "'");
        providers.put(provider, ProviderLoadStatus.LOADING);

        final DataProviderDescriptor descriptor = descriptors.get(provider);

        for (ModuleReference ref : descriptor.getHardDependencies()) {
            final FlexModule module = ref.getModule();
            if (module == null) {
                indicateFailure(provider, "Required dependency '" + ref.toString() + "' not found.");
                return;
            } else {
                loadProvider(checked, (PlayerDataProvider) module, descriptors);
            }
        }

        for (ModuleReference ref : descriptor.getSoftDependencies()) {
            final FlexModule module = ref.getModule();
            if (module != null) {
                loadProvider(checked, (PlayerDataProvider) module, descriptors);
            }
        }

        for (ModuleReference ref : descriptor.getHardDependencies()) {
            if (providers.get(ref.getModule()) != ProviderLoadStatus.SUCCESS) {
                indicateFailure(provider, "Required dependency '" + ref.toString() + "' didn't load successfully.");
                return;
            }
        }

        if (descriptor.onlineOnly() && player.getPlayer() == null) {
            providers.remove(provider);
            return;
        }

        try {
            provider.loadPlayer(this, data, player);
            indicateSuccess(provider);
        } catch (Exception ex) {
            ex.printStackTrace();
            indicateFailure(provider, ex.getMessage());
        }
    }

    public void notifyJoin() {
        for (Entry<PlayerDataProvider, ProviderLoadStatus> entry : providers.entrySet()) {
            if (entry.getValue() == ProviderLoadStatus.SUCCESS) {
                try {
                    entry.getKey().handlePlayerJoin(this, data, player);
                } catch (Exception ex) {
                    LogHelper.severe(getPlayerManager(),
                            "An exception occurred while data provider '"
                                    + entry.getKey().getClass().getCanonicalName()
                                    + "' was handling player join", ex);
                }
            }
        }
    }

    public int getLoadedProviderCount() {
        int count = 0;
        for (ProviderLoadStatus status : providers.values()) {
            if (status == ProviderLoadStatus.SUCCESS) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return True if the loader loaded the player's data successfully.
     */
    public boolean loadedSuccessfully() {
        return getLoadedProviderCount() == providers.size();
    }

    public void indicateSuccess(PlayerDataProvider provider) {
        Validate.notNull(provider, "Provider cannot be null.");
        providers.put(provider, ProviderLoadStatus.SUCCESS);
        LogHelper.debug(getPlayerManager(), "Provider '" + provider.getClass().getCanonicalName() + "' successfully loaded data for player '" + getName() + "' (" + getUuid().toString() + ")");
    }

    public void indicateFailure(PlayerDataProvider provider, String reason) {
        Validate.notNull(provider, "Provider cannot be null.");
        providers.put(provider, ProviderLoadStatus.FAILURE);
        LogHelper.severe(getPlayerManager(), "Provider '" + provider.getClass().getCanonicalName() + "' failed to load data for player '" + getName() + "' (" + getUuid().toString() + "): " + reason);
    }

    public void save() {
        for (Entry<PlayerDataProvider, ProviderLoadStatus> entry : providers.entrySet()) {
            if (entry.getValue() == ProviderLoadStatus.SUCCESS) {
                try {
                    entry.getKey().savePlayer(this, data, player);
                } catch (Exception ex) {
                    LogHelper.severe(getPlayerManager(), "Provider '" + entry.getKey().getClass().getCanonicalName() + "' encountered an exception while saving data for player '" + getName() + "' (" + getUuid().toString() + ")", ex);
                }
            }
        }

        data.save();
    }

    public void unload(boolean force) {
        Map<PlayerDataProvider, DataProviderDescriptor> regProviders = FlexPlugin.getGlobalModule(PlayerManager.class).getDataProviders();

        Iterator<Entry<PlayerDataProvider, ProviderLoadStatus>> iterator = providers.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<PlayerDataProvider, ProviderLoadStatus> entry = iterator.next();

            if (!force && regProviders.get(entry.getKey()).persistent()) {
                continue;
            }

            if (entry.getValue() == ProviderLoadStatus.SUCCESS) {
                try {
                    if (!entry.getKey().unloadPlayer(this, data, player, force)) {
                        if (force) {
                            LogHelper.warning(getPlayerManager(), "Provider '" + entry.getKey().getClass().getCanonicalName() + "' may not have unloaded data completely for player '" + getName() + "' (" + getUuid().toString() + ")");
                        }
                    }
                } catch (Exception ex) {
                    LogHelper.severe(getPlayerManager(), "Provider '" + entry.getKey().getClass().getCanonicalName() + "' encountered an exception while unloading data for player '" + getName() + "' (" + getUuid().toString() + ")", ex);
                }
                iterator.remove();
            }
        }
    }

}
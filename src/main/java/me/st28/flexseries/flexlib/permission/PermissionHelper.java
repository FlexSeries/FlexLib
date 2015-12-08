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
package me.st28.flexseries.flexlib.permission;

import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides additional utilities for permissions.<br />
 * <br />
 * Since Vault does not provide a way of checking group inheritance, this provides an alternate
 * method of checking group inheritance without having to hook into each individual permission plugin.
 */
// TODO: Make this work without Vault installed
public final class PermissionHelper {

    private static VaultHook vault;

    private static Map<String, GroupEntry> groupEntries = new LinkedHashMap<>();

    // Group, groups that inherit from it
    private static Map<String, List<String>> inheritanceIndex = new HashMap<>();

    public static void reload(ConfigurationSection config) {
        vault = FlexPlugin.getGlobalModule(HookManager.class).getHook(VaultHook.class);

        if (config == null) {
            return;
        }

        PermissionGroupEntry.clear();

        groupEntries.clear();
        inheritanceIndex.clear();

        Object rawObj = config.get("inheritance");

        // Inferred permissions
        if (rawObj instanceof List) {
            for (String group : (List<String>) rawObj) {
                groupEntries.put(group, new PermissionGroupEntry("flexlib.group." + group));
            }
            return;
        }

        if (!(rawObj instanceof ConfigurationSection)) {
            throw new IllegalArgumentException("Invalid configuration for inheritance.");
        }

        ConfigurationSection sec = (ConfigurationSection) rawObj;

        for (String group : sec.getKeys(false)) {
            String permission = sec.getString(group + ".permission");
            if (permission != null) {
                groupEntries.put(group, new PermissionGroupEntry(permission));
                continue;
            }

            if (!sec.isSet("groups")) {
                continue;
            }

            List<String> inherit = sec.getStringList("groups");
            if (inherit != null) {
                groupEntries.put(group, new GroupsGroupEntry(group, inherit));
            }
        }
    }

    private static List<String> getPlayerGroups(Player player) {
        return Arrays.asList(vault.getPermission().getPlayerGroups(null, player)).stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    public static boolean isPlayerInGroup(Player player, String group, boolean checkInheritance) {
        Validate.notNull(player, "Player cannot be null.");
        Validate.notNull(group, "Group cannot be null.");

        GroupEntry entry = groupEntries.get(group.toLowerCase());

        if (entry == null) {
            return getPlayerGroups(player).contains(group.toLowerCase());
        }

        return entry.containsPlayer(player) || (checkInheritance && entry.playerInherits(player));
    }

    public static String getTopGroup(Player player, List<String> groups, String defaultGroup) {
        List<String> reversed = new ArrayList<>(groupEntries.keySet());
        Collections.reverse(reversed);

        for (String group : reversed) {
            GroupEntry entry = groupEntries.get(group);

            if (groups.contains(group) && (entry.containsPlayer(player) || entry.playerInherits(player))) {
                return group;
            }
        }
        return defaultGroup;
    }

    // ------------------------------------------------------------------------------------------ //

    static abstract class GroupEntry {

        /**
         * @return True if the player is in this group.
         */
        abstract boolean containsPlayer(Player player);

        /**
         * @return True if the player is in this group via inheritance.
         */
        abstract boolean playerInherits(Player player);

    }

    static class PermissionGroupEntry extends GroupEntry {

        static final Map<String, Permission> bukkitPermissions = new HashMap<>();

        static void clear() {
            PluginManager pm = Bukkit.getPluginManager();
            for (Permission permission : bukkitPermissions.values()) {
                pm.removePermission(permission);
            }
        }

        // -------------------------------------------------------------------------------------- //

        String permission;

        PermissionGroupEntry(String permission) {
            this.permission = permission;
        }

        @Override
        boolean containsPlayer(Player player) {
            return player.hasPermission(permission);
        }

        @Override
        boolean playerInherits(Player player) {
            // Inheriting groups will have the permission as well, no need to do anything different.
            return containsPlayer(player);
        }

        Permission getBukkitPermission() {
            if (!bukkitPermissions.containsKey(permission)) {
                Permission bPerm = new Permission(permission, PermissionDefault.FALSE);
                Bukkit.getPluginManager().addPermission(bPerm);
                bukkitPermissions.put(permission, bPerm);
            }

            return bukkitPermissions.get(permission);
        }

    }

    static class GroupsGroupEntry extends GroupEntry {

        String group;

        GroupsGroupEntry(String group, List<String> inherits) {
            this.group = group.toLowerCase();

            for (String inherit : inherits) {
                inherit = inherit.toLowerCase();
                if (!inheritanceIndex.containsKey(inherit)) {
                    inheritanceIndex.put(inherit, new ArrayList<>());
                }
                inheritanceIndex.get(inherit).add(group);
            }

        }

        @Override
        boolean containsPlayer(Player player) {
            return getPlayerGroups(player).contains(group);
        }

        @Override
        boolean playerInherits(Player player) {
            List<String> pGroups = getPlayerGroups(player);

            List<String> inheritance = inheritanceIndex.get(group);

            for (String inherit : inheritance) {
                if (pGroups.contains(inherit)) {
                    return true;
                }
            }
            return false;
        }

    }

}
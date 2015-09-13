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
package me.st28.flexseries.flexlib.permission;

import me.st28.flexseries.flexlib.hook.HookManager;
import me.st28.flexseries.flexlib.hook.defaults.VaultHook;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides additional utilities for permissions.<br />
 * <br />
 * Since Vault does not provide a way of checking group inheritance, this provides an alternate
 * method of checking group inheritance without having to hook into each individual permission plugin.
 */
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
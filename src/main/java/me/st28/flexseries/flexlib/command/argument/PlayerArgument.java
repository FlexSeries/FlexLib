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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.permission.PermissionNodes;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An {@link Argument} that creates a {@link PlayerReference} based on user input.
 */
public class PlayerArgument extends Argument {

    /**
     * If true, only targets online players.
     */
    private boolean onlineOnly = false;

    /**
     * If true, will hide the target if the sender does not have permission to view them.
     */
    private boolean hideVanished = false;

    /**
     * The permission nodes that allow the sender to bypass {@link #hideVanished}.
     */
    private final Set<PermissionNode> vanishBypassPerms = new HashSet<>(Collections.singletonList(PermissionNodes.BYPASS_VANISH));

    /**
     * If true, will add online player names to the tab completion list.
     */
    private boolean matchOnlineNames = true;

    /**
     * If true, will add offline player names to the tab completion list.
     */
    private boolean matchOfflineNames = true;

    /**
     * If true, will prevent the sender from being the target.
     */
    private boolean notSender = false;

    /**
     * If true, {@link #getDefaultValue(CommandContext)} will return the sender if they are a player.
     */
    private boolean inferSender = true;

    public PlayerArgument(String name, boolean isRequired) {
        super(name, isRequired);
    }

    /**
     * @see #onlineOnly
     * @return This instance, for chaining.
     */
    public PlayerArgument onlineOnly(boolean isOnline) {
        this.onlineOnly = isOnline;
        return this;
    }

    /**
     * @see #hideVanished
     * @return This instance, for chaining.
     */
    public PlayerArgument hideVanished(boolean hideVanished) {
        this.hideVanished = hideVanished;
        return this;
    }

    /**
     * @see #vanishBypassPerms
     * @return This instance, for chaining.
     */
    public PlayerArgument clearVanishBypassPerms() {
        vanishBypassPerms.clear();
        return this;
    }

    /**
     * @see #vanishBypassPerms
     * @return This instance, for chaining.
     */
    public PlayerArgument addVanishBypassPerm(PermissionNode permission) {
        vanishBypassPerms.add(permission);
        return this;
    }

    /**
     * @see #matchOnlineNames
     * @return This instance, for chaining.
     */
    public PlayerArgument matchOnlineNames(boolean matchOnlineNames) {
        this.matchOnlineNames = matchOnlineNames;
        return this;
    }

    /**
     * @see #matchOfflineNames
     * @return This instance, for chaining.
     */
    public PlayerArgument matchOfflineNames(boolean matchOfflineNames) {
        this.matchOfflineNames = matchOfflineNames;
        return this;
    }

    /**
     * @see #notSender
     * @return This instance, for chaining.
     */
    public PlayerArgument notSender(boolean notSender) {
        this.notSender = notSender;
        return this;
    }

    /**
     * @see #inferSender
     * @return This instance, for chaining.
     */
    public PlayerArgument inferSender(boolean inferSender) {
        this.inferSender = inferSender;
        return this;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);

        Map<String, UUID> lookup;
        if (onlineOnly) {
            // Only lookup online users
            lookup = Bukkit.getOnlinePlayers().stream().collect(Collectors.toMap(p -> p.getName().toLowerCase(), Player::getUniqueId));
        } else {
            // Lookup both online and offline users
            lookup = uuidTracker.getAllLatestNameUuids().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Entry::getValue));
        }

        // 1) Try exact name
        UUID found = lookup.get(input.toLowerCase());

        if (found == null) {
            // 2) Try matching name

            List<String> matchedNames = new ArrayList<>();
            for (String curName : lookup.keySet()) {
                if (input.equalsIgnoreCase(curName)) {
                    // Exact match
                    matchedNames.add(input);
                    break;
                }

                if (curName.toLowerCase().startsWith(input.toLowerCase())) {
                    // Partial match
                    matchedNames.add(curName);
                }
            }

            if (matchedNames.size() == 1) {
                // Only one name was matched
                found = uuidTracker.getLatestUuid(matchedNames.get(0));
            } else if (matchedNames.size() > 1) {
                // Multiple names were matched, show error
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_multiple", new ReplacementMap("{NAME}", input).getMap()));
            }
        }

        CommandSender sender = context.getSender();
        if (found != null && notSender && sender instanceof Player && ((Player) sender).getUniqueId().equals(found)) {
            // UUID was found, but sender is the target
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.player_cannot_be_self"));
        } else if (found != null && ((onlineOnly && Bukkit.getPlayer(found) == null) || (hideVanished && !canSenderView(sender, Bukkit.getPlayer(found))))) {
            // UUID was found, but target is offline (or appears to be offline to sender)
            String cleanName = uuidTracker.getLatestName(found);
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_offline", new ReplacementMap("{NAME}", cleanName).getMap()));
        } else if (found == null) {
            // UUID wasn't found
            if (onlineOnly) {
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_none", new ReplacementMap("{NAME}", input).getMap()));
            } else {
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_none_offline", new ReplacementMap("{NAME}", input).getMap()));
            }
        }

        return new PlayerReference(found);
    }

    @Override
    public Object getDefaultValue(CommandContext context) {
        if (inferSender && !notSender) {
            if (context.getSender() instanceof Player) {
                return new PlayerReference((Player) context.getSender());
            } else {
                throw new CommandInterruptedException(InterruptReason.MUST_BE_PLAYER);
            }
        }
        return null;
    }

    private boolean canSenderView(CommandSender sender, Player other) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (((Player) sender).canSee(other)) {
            return true;
        }

        boolean canView = false;
        for (PermissionNode permission : vanishBypassPerms) {
            if (permission.isAllowed(sender)) {
                canView = true;
            }
        }

        return canView;
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String argument) {
        if (!matchOnlineNames && !matchOfflineNames) {
            return null;
        }

        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);
        CommandSender sender = context.getSender();

        Set<String> returnList = new HashSet<>();

        if (matchOfflineNames && !onlineOnly) {
            returnList.addAll(uuidTracker.getAllNames());
        } else if (matchOnlineNames) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!canSenderView(sender, player)) {
                    continue;
                }
                returnList.add(player.getName());
            }
        }

        return new ArrayList<>(returnList);
    }

}
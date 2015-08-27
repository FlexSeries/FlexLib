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
package me.st28.flexseries.flexlib.command.logic.input;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
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

import java.util.*;

/**
 * An {@link InputPart} that creates a {@link PlayerReference} based on user input.
 */
public class PlayerInputPart extends InputPart {

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
    private Set<PermissionNode> vanishBypassPerms = new HashSet<>(Collections.singletonList(PermissionNodes.BYPASS_VANISH));

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

    public PlayerInputPart(String name, boolean isRequired) {
        super(name, isRequired);
    }

    /**
     * @see #onlineOnly
     * @return This instance, for chaining.
     */
    public PlayerInputPart onlineOnly(boolean isOnline) {
        this.onlineOnly = isOnline;
        return this;
    }

    /**
     * @see #hideVanished
     * @return This instance, for chaining.
     */
    public PlayerInputPart hideVanished(boolean hideVanished) {
        this.hideVanished = hideVanished;
        return this;
    }

    /**
     * @see #vanishBypassPerms
     * @return This instance, for chaining.
     */
    public PlayerInputPart clearVanishBypassPerms() {
        vanishBypassPerms.clear();
        return this;
    }

    /**
     * @see #vanishBypassPerms
     * @return This instance, for chaining.
     */
    public PlayerInputPart addVanishBypassPerm(PermissionNode permission) {
        vanishBypassPerms.add(permission);
        return this;
    }

    /**
     * @see #matchOnlineNames
     * @return This instance, for chaining.
     */
    public PlayerInputPart matchOnlineNames(boolean matchOnlineNames) {
        this.matchOnlineNames = matchOnlineNames;
        return this;
    }

    /**
     * @see #matchOfflineNames
     * @return This instance, for chaining.
     */
    public PlayerInputPart matchOfflineNames(boolean matchOfflineNames) {
        this.matchOfflineNames = matchOfflineNames;
        return this;
    }

    /**
     * @see #notSender
     * @return This instance, for chaining.
     */
    public PlayerInputPart notSender(boolean notSender) {
        this.notSender = notSender;
        return this;
    }

    @Override
    public Object parseInput(CommandContext context, String input) {
        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);

        UUID found = null;

        // 1) Try exact name
        found = uuidTracker.getLatestUuid(input);

        if (found == null) {
            // 2) Try matching name

            List<String> matchedNames = new ArrayList<>();

            for (String curName : uuidTracker.getAllNames()) {
                if (name.equalsIgnoreCase(curName)) {
                    // Exact match
                    matchedNames.add(name);
                    break;
                }

                if (curName.toLowerCase().contains(name.toLowerCase())) {
                    // Partial match
                    matchedNames.add(curName);
                }
            }

            if (matchedNames.size() == 1) {
                found = uuidTracker.getLatestUuid(matchedNames.get(0));
            } else if (matchedNames.size() > 1) {
                throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class,"general.errors.player_matched_multiple", new ReplacementMap("{NAME}", input).getMap()));
            }
        }

        CommandSender sender = context.getSender();
        if (found != null && notSender && sender instanceof Player && ((Player) sender).getUniqueId().equals(found)) {
            throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "general.errors.player_cannot_be_self"));
        } else if (found != null && (onlineOnly && Bukkit.getPlayer(found) == null) || (hideVanished && !canSenderView(sender, Bukkit.getPlayer(found)))) {
            String cleanName = uuidTracker.getLatestName(found);
            throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_offline", new ReplacementMap("{NAME}", cleanName).getMap()));
        } else if (found == null) {
            throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "general.errors.player_matched_none_offline", new ReplacementMap("{NAME}", input).getMap()));
        }

        return found;
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
    public List<String> getSuggestions(CommandContext context, int curIndex) {
        if (!matchOnlineNames && !matchOfflineNames) {
            return null;
        }

        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);

        Set<String> returnList = new HashSet<>();

        if (matchOfflineNames && !onlineOnly) {
            returnList.addAll(uuidTracker.getAllNames());
        } else if (matchOnlineNames) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                returnList.addAll(uuidTracker.getAllNames(player.getUniqueId()));
            }
        }

        return new ArrayList<>(returnList);
    }

}
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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.player.settings.PlayerSettingsManager;
import me.st28.flexseries.flexlib.player.settings.Setting;
import me.st28.flexseries.flexlib.player.settings.defaults.ToggleableSetting;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.Pair;
import me.st28.flexseries.flexlib.utils.StringConverter;
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ToggleSubcommand<T extends FlexPlugin> extends Subcommand<T> {

    private String singleGroup;
    private final Set<Pair<String, String>> settings = new HashSet<>();

    public ToggleSubcommand(AbstractCommand<T> parent, CommandDescriptor descriptor, Pair<String, String>... settings) {
        super(parent, descriptor.defaultCommand("list").description("Base toggle command").playerOnly(true));

        registerSubcommand(new SCmdList(this));

        Collections.addAll(this.settings, settings);

        Validate.notEmpty(this.settings, "Must be one or more settings.");

        List<String> groups = new ArrayList<>();
        for (Pair<String, String> setting : settings) {
            if (!groups.contains(setting.getKey())) {
                groups.add(setting.getKey());
            }
        }

        if (groups.size() > 1) {
            addArgument(new GroupArgument("group", true));
        } else {
            singleGroup = groups.get(0);
        }

        addArgument(new Argument("setting", true) {
            @Override
            public Object parseInput(CommandContext context, String input) {
                Map<String, Setting> groupArg = context.getGlobalObject("group", Map.class);

                Setting setting;
                if (groupArg != null) {
                    setting = groupArg.get(input.toLowerCase());
                } else {
                    setting = FlexPlugin.getGlobalModule(PlayerSettingsManager.class).getSetting(singleGroup, input);
                }

                if (setting == null
                        || (!ToggleSubcommand.this.settings.contains(new Pair<>(setting.getGroup().toLowerCase(), setting.getName().toLowerCase()))
                        && !ToggleSubcommand.this.settings.contains(new Pair<>(setting.getGroup().toLowerCase(), (String) null))))
                {
                    MessageReference message;
                    ReplacementMap replacements = new ReplacementMap();
                    replacements.put("{SETTING}", input);
                    if (groupArg != null) {
                        replacements.put("{GROUP}", context.getGlobalObject("group", String.class));
                        message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.errors.setting_not_found_group", replacements.getMap());
                    } else {
                        message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.errors.setting_not_found", replacements.getMap());
                    }
                    throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, message);
                }

                if (!(setting instanceof ToggleableSetting)) {
                    MessageReference message;
                    ReplacementMap replacements = new ReplacementMap();
                    replacements.put("{SETTING}", input);
                    if (groupArg != null) {
                        replacements.put("{GROUP}", context.getGlobalObject("group", String.class));
                        message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.errors.setting_not_toggleable_group", replacements.getMap());
                    } else {
                        message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.errors.setting_not_toggleable", replacements.getMap());
                    }
                    throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, message);
                }
                return setting;
            }
        });
    }

    @Override
    public void handleExecute(CommandContext context) {
        ToggleableSetting setting = context.getGlobalObject("setting", ToggleableSetting.class);
        UUID uuid = ((Player) context.getSender()).getUniqueId();

        setting.toggle(uuid);

        MessageReference message;
        ReplacementMap map = new ReplacementMap("{TOGGLE}", setting.getName());
        if (setting.getValue(uuid, Boolean.class)) {
            message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.notices.toggle_enabled", map.getMap());
        } else {
            message = MessageManager.getMessage(FlexLib.class, "lib_player_settings.notices.toggle_disabled", map.getMap());
        }
        throw new CommandInterruptedException(InterruptReason.COMMAND_END, message);
    }

    // ------------------------------------------------------------------------------------------ //

    private class GroupArgument extends Argument {

        public GroupArgument(String name, boolean isRequired) {
            super(name, isRequired);
        }

        @Override
        public Object parseInput(CommandContext context, String input) {
            Map<String, Setting> map = FlexPlugin.getGlobalModule(PlayerSettingsManager.class).getSettings(input);
            if (map == null) {
                throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "lib_player_settings.errors.group_not_found", new ReplacementMap("{GROUP}", input).getMap()));
            }
            context.addGlobalObject("groupName", input);
            return map;
        }

        @Override
        public List<String> getSuggestions(CommandContext context, String input) {
            List<String> returnList = new ArrayList<>();

            for (Pair<String, String> setting : settings) {
                String group = setting.getKey().toLowerCase();

                if (!returnList.contains(group)) {
                    returnList.add(group);
                }
            }

            return returnList;
        }

    }

    private class SCmdList extends Subcommand<T> {

        public SCmdList(AbstractCommand<T> parent) {
            super(parent, new CommandDescriptor("list").description("List available toggles"));

            if (singleGroup != null) {
                addArgument(new GroupArgument("group", false));
            }
        }

        @Override
        public void handleExecute(CommandContext context) {
            Map<String, Setting> groupArg = context.getGlobalObject("group", Map.class);

            if (groupArg == null) {
                PlayerSettingsManager manager = FlexPlugin.getGlobalModule(PlayerSettingsManager.class);

                if (singleGroup != null) {
                    groupArg = manager.getSettings(singleGroup);
                    context.addGlobalObject("groupName", singleGroup);
                } else {
                    // List groups
                    ListBuilder builder = new ListBuilder("subtitle", "Toggle Groups", "NYI", context.getLabel());

                    List<String> groups = new ArrayList<>();
                    for (Pair<String, String> setting : settings) {
                        String group = setting.getKey().toLowerCase();
                        if (!groups.contains(group)) {
                            groups.add(group);
                        }
                    }

                    if (!groups.isEmpty()) {
                        builder.addMessage(ChatColor.GOLD + StringUtils.collectionToSortedString(groups, ChatColor.DARK_GRAY + ", " + ChatColor.GOLD));
                    }

                    builder.sendTo(context.getSender());
                    return;
                }
            }

            List<ToggleableSetting> applicable = new ArrayList<>();

            for (Pair<String, String> entry : settings) {
                String setting = entry.getValue();

                if (setting == null) {
                    for (Setting groupSetting : groupArg.values()) {
                        if (groupSetting instanceof ToggleableSetting) {
                            applicable.add((ToggleableSetting) groupSetting);
                        }
                    }
                } else {
                    Setting directSetting = groupArg.get(setting.toLowerCase());

                    if (directSetting != null && directSetting instanceof ToggleableSetting) {
                        applicable.add((ToggleableSetting) directSetting);
                    }
                }
            }

            Collections.sort(applicable, new Comparator<ToggleableSetting>() {
                @Override
                public int compare(ToggleableSetting o1, ToggleableSetting o2) {
                    int initial = o1.getGroup().compareTo(o2.getGroup());
                    if (initial == 0) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return initial;
                }
            });

            ListBuilder builder = new ListBuilder("subtitle", "Toggles", context.getGlobalObject("groupName", String.class), context.getLabel());

            if (!applicable.isEmpty()) {
                builder.addMessage(StringUtils.collectionToString(applicable, ChatColor.DARK_GRAY + ", ", new StringConverter<ToggleableSetting>() {
                    @Override
                    public String toString(ToggleableSetting object) {
                        return (object.isPositiveValue(object.getValue(((Player) context.getSender()).getUniqueId(), Boolean.class)) ? ChatColor.GREEN : ChatColor.RED) + object.getName();
                    }
                }));
            }

            builder.sendTo(context.getSender());
        }

    }

}
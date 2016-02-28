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
package me.st28.flexseries.flexlib.backend.commands.player;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.AbstractCommand;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.Subcommand;
import me.st28.flexseries.flexlib.command.argument.PlayerArgument;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.PlayerReference;
import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.StringUtils;

public final class SCmdPlayer extends Subcommand<FlexLib> {

    public SCmdPlayer(AbstractCommand<FlexLib> parent) {
        super(parent, new CommandDescriptor("player").description("Base player admin command"));

        addArgument(new PlayerArgument("player", true).inferSender(false).onlineOnly(false).matchOfflineNames(true));
    }

    @Override
    public void handleExecute(CommandContext context) {
        PlayerReference player = context.getGlobalObject("player", PlayerReference.class);
        PlayerUuidTracker uuidTracker = FlexPlugin.getGlobalModule(PlayerUuidTracker.class);
        PlayerData data = FlexPlugin.getGlobalModule(PlayerManager.class).getPlayerData(player.getUuid());

        ListBuilder builder = new ListBuilder("subtitle", "Player Info", player.getName(), context.getLabel());

        builder.addMessage("title", "UUID", player.getUuid().toString());
        builder.addMessage("title", "Current name", player.getName());
        builder.addMessage("title", "Past names", StringUtils.collectionToString(uuidTracker.getAllNames(player.getUuid()), ", "));
        builder.addMessage("title", "Data loaded", data == null ? "False" : "True");

        builder.sendTo(context.getSender());
    }

}
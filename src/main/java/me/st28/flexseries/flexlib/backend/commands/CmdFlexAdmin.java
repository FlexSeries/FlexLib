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
package me.st28.flexseries.flexlib.backend.commands;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.backend.commands.player.SCmdPlayer;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.DummyCommand;
import me.st28.flexseries.flexlib.permission.PermissionNodes;

/**
 * Base utility command for the FlexLib library.
 */
public final class CmdFlexAdmin extends DummyCommand<FlexLib> {

    public CmdFlexAdmin(FlexLib plugin) {
        super(plugin, new CommandDescriptor("flexadmin").permission(PermissionNodes.ADMIN));

        registerSubcommand(new SCmdPlayer(this));
    }

}
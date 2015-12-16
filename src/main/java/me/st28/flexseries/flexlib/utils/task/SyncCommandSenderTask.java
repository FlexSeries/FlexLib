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
package me.st28.flexseries.flexlib.utils.task;

import me.st28.flexseries.flexlib.utils.CommandSenderRef;
import me.st28.flexseries.flexlib.utils.TaskChain;
import org.bukkit.command.CommandSender;

public class SyncCommandSenderTask extends TaskChain {

    public SyncCommandSenderTask(CommandSender sender, LastTask<CommandSender> task) {
        this(new CommandSenderRef(sender), task);
    }

    public SyncCommandSenderTask(CommandSenderRef sender, LastTask<CommandSender> task) {
        add(new FirstTask<CommandSender>() {
            @Override
            protected CommandSender run() {
                CommandSender cs = sender.getCommandSender();
                if (cs != null) {
                    return cs;
                } else {
                    abort();
                    return null;
                }
            }
        }).add(task);
    }

}
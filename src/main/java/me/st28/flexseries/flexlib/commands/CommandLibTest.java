/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.commands;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandHandler;
import me.st28.flexseries.flexlib.player.PlayerReference;

public class CommandLibTest {

    @CommandHandler(value = "hello", description = "Basic test command")
    public void hello(CommandContext context) {
        context.getSender().sendMessage("Hello World!");
    }

    @CommandHandler(
            value = "hello2",
            description = "Basic test command with arguments",
            args = {
                "target player always"
            }
    )
    public void hello2(CommandContext context) {
        context.getSender().sendMessage("Hello from " + context.getArgument("target", PlayerReference.class).getName());
    }

    @CommandHandler(
            value = "test",
            parent = "hello2",
            args = {
                "str string always -min=3 -max=20"
            }
    )
    public void hello2_test(CommandContext context) {
        context.getSender().sendMessage(context.getArgument("str", String.class));
    }

}
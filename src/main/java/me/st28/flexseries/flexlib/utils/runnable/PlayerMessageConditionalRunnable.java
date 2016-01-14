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
package me.st28.flexseries.flexlib.utils.runnable;

import me.st28.flexseries.flexlib.message.reference.MessageReference;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerMessageConditionalRunnable extends PlayerConditionalRunnable {

    private final List<MessageReference> messages = new ArrayList<>();

    public PlayerMessageConditionalRunnable(UUID player, MessageReference... message) {
        super(player);
        Collections.addAll(this.messages, message);
    }

    @Override
    public void handleRun(Player player) {
        for (MessageReference message : messages) {
            message.sendTo(player);
        }
    }

}
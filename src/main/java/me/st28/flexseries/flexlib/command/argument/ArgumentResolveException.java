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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;

/**
 * Thrown when an argument fails to be resolved.
 */
public class ArgumentResolveException extends RuntimeException {

    private Message errorMessage;

    public ArgumentResolveException(String message, Object... replacements) {
        errorMessage = Message.getGlobal(message, replacements);
    }

    public ArgumentResolveException(Class<? extends FlexPlugin> plugin, String message, Object... replacements) {
        errorMessage = Message.get(plugin, message, replacements);
    }

    public ArgumentResolveException(Message message) {
        this.errorMessage = message;
    }

    public ArgumentResolveException(Throwable cause) {
        super(cause);
    }

    public ArgumentResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public Message getErrorMessage() {
        return errorMessage;
    }

}
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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import org.apache.commons.lang.Validate;

public class CommandInterruptedException extends RuntimeException {

    private final InterruptReason reason;
    private final MessageReference exitMessage;

    public CommandInterruptedException(InterruptReason reason) {
        this(reason, null, null);
    }

    public CommandInterruptedException(InterruptReason reason, Exception ex) {
        this(reason, null, ex);
    }

    public CommandInterruptedException(InterruptReason reason, MessageReference exitMessage) {
        this(reason, exitMessage, null);
    }

    public CommandInterruptedException(InterruptReason reason, MessageReference exitMessage, Exception ex) {
        super(ex);

        Validate.notNull(reason, "Reason cannot be null.");

        this.reason = reason;
        this.exitMessage = exitMessage != null ? exitMessage : getDefaultMessage(reason);
    }

    public InterruptReason getReason() {
        return reason;
    }

    public MessageReference getExitMessage() {
        return exitMessage;
    }

    private MessageReference getDefaultMessage(InterruptReason reason) {
        switch (reason) {
            case ARGUMENT_ERROR:
            case COMMAND_ERROR:
                return MessageManager.getMessage(FlexLib.class, "lib_command.errors.uncaught_exception", new ReplacementMap("{MESSAGE}", getCause().getMessage()).getMap());

            case MUST_BE_PLAYER:
                return MessageManager.getMessage(FlexLib.class, "general.errors.must_be_player");

            case NO_PERMISSION:
                return MessageManager.getMessage(FlexLib.class, "general.errors.no_permission");

            default:
                return null;
        }
    }

    // ------------------------------------------------------------------------------------------ //

    public enum InterruptReason {

        /**
         * An exception occurred while parsing an argument.
         */
        ARGUMENT_ERROR(true),

        /**
         * There was an issue while parsing an argument that prevents the command from continuing.
         */
        ARGUMENT_SOFT_ERROR(false),

        /**
         * The command ended without any errors.
         */
        COMMAND_END(false),

        /**
         * There was an issue while running the command that prevents it from continuing.
         */
        COMMAND_SOFT_ERROR(false),

        /**
         * An error occurred while executing the command.
         */
        COMMAND_ERROR(true),

        /**
         * The provided arguments are invalid.<br />
         * Automatically sends the usage message to the sender.
         */
        INVALID_USAGE(false),

        /**
         * The command is strictly for players only and the sender is not a player.
         */
        MUST_BE_PLAYER(false),

        /**
         * The sender doesn't have permission for the command.<br />
         * Automatically sends the no permission message to the sender.
         */
        NO_PERMISSION(false);

        private boolean isError;

        InterruptReason(boolean isError) {
            this.isError = isError;
        }

        public boolean isError() {
            return isError;
        }

    }

}
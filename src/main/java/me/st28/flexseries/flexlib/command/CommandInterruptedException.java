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
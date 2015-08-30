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
import me.st28.flexseries.flexlib.permission.PermissionNode;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

public final class CommandUtils {

    private CommandUtils() {}

    public static void performPermissionTest(CommandSender sender, PermissionNode permission) {
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(permission, "Permission cannot be null.");

        if (!permission.isAllowed(sender)) {
            throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "general.errors.no_permission"));
        }
    }

}
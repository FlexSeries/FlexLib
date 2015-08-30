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
package me.st28.flexseries.flexlib.command.logic.hub;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.logic.LogicPath;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * A {@link LogicHub} that uses a default {@link LogicPath} to handle its logic.
 */
public class PathLogicHub extends LogicHub {

    private LogicPath defaultPath;

    public PathLogicHub(LogicPath defaultPath) {
        Validate.notNull(defaultPath, "Default path cannot be null.");
        this.defaultPath = defaultPath;
    }

    @Override
    public void handleExecute(CommandContext context, int curIndex) {
        if (context.getArgs().size() < defaultPath.getRequiredArgs()) {
            throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "lib_command.errors.usage", new ReplacementMap("{USAGE}", defaultPath.buildUsage()).getMap()));
        }

        defaultPath.execute(context, curIndex);
    }

    @Override
    public List<String> getSuggestions(CommandContext context, int curIndex) {
        List<String> superList = super.getSuggestions(context, curIndex);

        if (getFirstArgumentIndex() == curIndex) {
            List<String> defSuggestions = defaultPath.getSuggestions(context, curIndex);
            if (defSuggestions != null) {
                superList.addAll(defSuggestions);
            }
        }

        return superList;
    }

}
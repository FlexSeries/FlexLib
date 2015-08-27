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
package me.st28.flexseries.flexlib.message;

import me.st28.flexseries.flexlib.message.reference.McmlMessageReference;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.Map;

/**
 * A message builder to use colors defined in the config-messages.yml file from FlexCore easily.
 */
public class MessageBuilder {

    private StringBuilder builder;

    public MessageBuilder() {
        this(null);
    }

    public MessageBuilder(String initial) {
        if (initial == null) {
            initial = "";
        }
        builder = new StringBuilder(initial);
    }

    private MessageMasterManager getMessageManager() {
        return FlexPlugin.getGlobalModule(MessageMasterManager.class);
    }

    @Override
    public String toString() {
        return getMessageManager().processMessage(builder.toString());
    }

    /**
     * @see #toMessageReference(Map)
     */
    public MessageReference toMessageReference() {
        return toMessageReference(null);
    }

    /**
     * @return builds the message into a {@link McmlMessageReference} in the default implementation.
     */
    public MessageReference toMessageReference(Map<String, Object> replacements) {
        return new McmlMessageReference(toString(), replacements);
    }

    /**
     * Appends text to the builder.
     *
     * @return This {@link MessageBuilder} instance, for chaining.
     */
    public MessageBuilder text(String text) {
        Validate.notNull(text, "Text cannot be null.");
        builder.append(text);
        return this;
    }

    /**
     * Appends an object to the builder with a given object format.
     *
     * @return This {@link MessageBuilder} instance, for chaining.
     */
    public MessageBuilder object(String object, String format) {
        Validate.notNull(object, "Object cannot be null.");
        Validate.notNull(format, "Format cannot cannot be null.");

        builder.append("{o=").append(format).append("}").append(object).append("{/}");

        return this;
    }

    /**
     * Appends a mood color to the builder.
     *
     * @return This {@link MessageBuilder} instance, for chaining.
     */
    public MessageBuilder mood(String mood) {
        Validate.notNull(mood, "Mood cannot be null.");
        builder.append("{mood=").append(mood).append("}");
        return this;
    }

}
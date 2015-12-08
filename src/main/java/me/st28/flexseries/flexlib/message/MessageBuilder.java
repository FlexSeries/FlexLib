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
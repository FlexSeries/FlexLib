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

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: API for adding mood and object formats
public final class MessageMasterManager extends FlexModule<FlexLib> {

    private static final Pattern MOOD_PATTERN = Pattern.compile("\\{mood=(\\S+?)\\}");
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{o=(\\S+?)\\}");
    private static final Pattern OBJECT_VALUE_PATTERN = Pattern.compile("\\{o=(\\S+?)\\}(.+?)\\{/\\}");
    private static final Pattern OBJECT_SPLIT_PATTERN = Pattern.compile("\\{,\\}");

    private final Map<String, String> moodFormats = new HashMap<>();

    private final Map<String, String> objectFormats = new HashMap<>();

    public MessageMasterManager(FlexLib plugin) {
        super(plugin, "messages-master", "The master messages handler", new ModuleDescriptor().setSmartLoad(false).setGlobal(true));
    }

    @Override
    protected void handleReload() {
        final ConfigurationSection config = getConfig();

        // Load mood colors
        moodFormats.clear();

        final ConfigurationSection moodSec = config.getConfigurationSection("formats.moods");

        for (String mood : moodSec.getKeys(false)) {
            moodFormats.put(mood, moodSec.getString(mood));
        }

        // Load object colors
        objectFormats.clear();

        final ConfigurationSection objectSec = config.getConfigurationSection("formats.objects");

        for (String object : objectSec.getKeys(false)) {
            objectFormats.put(object, objectSec.getString(object));
        }
    }

    /**
     * To check if a mood format doesn't exist, {@link #getMoodFormatRaw(String)} can be used instead.
     *
     * @return the format for a given mood.<br />
     *         Returns an empty string if the mood was not found.
     */
    public String getMoodFormat(String mood) {
        Validate.notNull(mood, "Mood cannot be null.");
        return !moodFormats.containsKey(mood) ? "" : moodFormats.get(mood);
    }

    /**
     * To avoid having to use a null check, {@link #getMoodFormat(String)} can be used instead.
     *
     * @return the format for a given mood.<br />
     *         Returns null if the mood was not found.
     */
    public String getMoodFormatRaw(String mood) {
        Validate.notNull(mood, "Mood cannot be null.");
        return moodFormats.get(mood);
    }

    /**
     * To check if an object format doesn't exist, {@link #getObjectFormatRaw(String)} can be used instead.
     *
     * @return the format for a given object.<br />
     *         Returns an empty string if the object was not found.
     */
    public String getObjectFormat(String object) {
        Validate.notNull(object, "Object cannot be null.");
        return !objectFormats.containsKey(object) ? "" : objectFormats.get(object);
    }

    /**
     * To avoid having to use a null check, {@link #getObjectFormat(String)} can be used instead.
     *
     * @return the format for a given object.<br />
     *         Returns an empty string if the object was not found.
     */
    public String getObjectFormatRaw(String object) {
        Validate.notNull(object, "Object cannot be null.");
        return objectFormats.get(object);
    }

    public String processMessage(String message) {
        // Process objects with values
        StringBuilder stage1 = new StringBuilder(message);

        Matcher matcherStage1 = OBJECT_VALUE_PATTERN.matcher(message);
        int offsetStage1 = 0;
        while (matcherStage1.find()) {
            String format = getObjectFormat(matcherStage1.group(1));

            String replacement = matcherStage1.group(2);

            String[] split = OBJECT_SPLIT_PATTERN.split(replacement);
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    format = format.replace("{" + (i + 1) + "}", split[i]);
                }
            }

            format = format.replace("{?}", matcherStage1.group(2));

            stage1.replace(matcherStage1.start() + offsetStage1, matcherStage1.end() + offsetStage1, format);
            offsetStage1 -= matcherStage1.end() - matcherStage1.start() - format.length();
        }

        // Process objects with no values
        StringBuilder stage2 = new StringBuilder(stage1.toString());

        Matcher matcherStage2 = OBJECT_PATTERN.matcher(stage1.toString());
        while (matcherStage2.find()) {
            stage2.replace(matcherStage2.start(), matcherStage2.end(), getObjectFormat(matcherStage2.group(1)));
        }

        // Process moods
        StringBuilder stage3 = new StringBuilder(stage2.toString());

        Matcher matcherStage3 = MOOD_PATTERN.matcher(stage2.toString());

        String lastMood = "";
        int offsetStage3 = 0;
        while (matcherStage3.find()) {
            String curMood = matcherStage3.group(1);

            if (!curMood.equals(".")) {
                lastMood = curMood;
            }

            String replacement = getMoodFormat(curMood.equals(".") ? lastMood : curMood);

            stage3.replace(matcherStage3.start() + offsetStage3, matcherStage3.end() + offsetStage3, replacement);
            offsetStage3 -= matcherStage3.end() - matcherStage3.start() - replacement.length();
        }

        return stage3.toString();
    }

}
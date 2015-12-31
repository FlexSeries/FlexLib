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
package me.st28.flexseries.flexlib.utils;

import org.apache.commons.lang.Validate;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {

    private TimeUtils() {}

    @Deprecated
    public static String translateSeconds(int seconds) {
        return translateSeconds(seconds, TimeUtils.TimeFormat.LONG);
    }

    @Deprecated
    public static String translateSeconds(int seconds, TimeUtils.TimeFormat format) {
        Validate.notNull(format);
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds cannot be negative! (" + seconds + ")");
        } else {
            StringBuilder returnString = new StringBuilder();
            int minutes = seconds / 60;
            seconds -= minutes * 60;
            int hours = minutes / 60;
            minutes -= hours * 60;
            int days = hours / 24;
            hours -= days * 24;

            if (format == TimeUtils.TimeFormat.LONG) {
                if (days > 0) {
                    returnString.append(days).append(days == 1 ? " day" : " days");
                }

                if (hours > 0) {
                    if (days > 0) {
                        returnString.append(" ");
                    }

                    returnString.append(hours).append(hours == 1 ? " hour" : " hours");
                }

                if (minutes > 0) {
                    if (hours > 0) {
                        returnString.append(" ");
                    }

                    returnString.append(minutes).append(minutes == 1 ? " minute" : " minutes");
                }

                if (seconds > 0 || seconds == 0 && minutes == 0 && hours == 0 && days == 0) {
                    if (minutes > 0) {
                        returnString.append(" ");
                    }

                    returnString.append(seconds).append(seconds == 1 ? " second" : " seconds");
                }
            } else if (format == TimeUtils.TimeFormat.SHORT) {
                if (days > 0) {
                    returnString.append(Integer.toString(days).length() == 1 ? "0" : "" + days);
                }

                if (hours > 0) {
                    if (days > 0) {
                        returnString.append(":");
                    }

                    returnString.append(Integer.toString(hours).length() == 1 ? "0" : "" + hours);
                }

                if (hours > 0) {
                    returnString.append(":");
                }

                returnString.append(Integer.toString(minutes).length() == 1 ? "0" : "" + minutes);
                if (minutes > 0) {
                    returnString.append(":");
                }

                returnString.append(Integer.toString(seconds).length() == 1 ? "0" : "" + seconds);
            } else {
                if (days > 0) {
                    returnString.append(days).append("d");
                }

                if (hours > 0) {
                    returnString.append(hours).append("h");
                }

                if (minutes > 0) {
                    returnString.append(minutes).append("m");
                }

                if (seconds > 0 || seconds == 0 && minutes == 0 && hours == 0 && days == 0) {
                    returnString.append(seconds).append("s");
                }
            }

            return returnString.toString();
        }
    }

    private final static Pattern PATTERN_TIME_GROUP = Pattern.compile("([0-9.]+)([smhd])");

    /**
     * Converts raw input into seconds (ex. 1.5h -> 5400 ; 5d2h -> 439200)
     * @return The converted seconds.<br />
     *         -1 if the input string is invalid.
     */
    public static int interpretSeconds(String input) {
        input = input.toLowerCase();
        Matcher matcher = PATTERN_TIME_GROUP.matcher(input);
        int total = 0;

        while (matcher.find()) {
            double time = Double.parseDouble(matcher.group(1));

            switch (matcher.group(2)) {
                case "d":
                    time *= 24D;
                    // fall through
                case "h":
                    time *= 60D;
                    // fall through
                case "m":
                    time *= 60D;
                    // fall through
                case "s":
                    break;
            }

            total += time;
        }

        return total;
    }

    private final static Pattern PATTERN_REPEATING_DELIM = Pattern.compile("(?:\\{-}){2,}");

    public static String formatSeconds(int seconds) {
        return formatSeconds(seconds, " ", DefaultTimeFormat.LONG, null, false);
    }

    public static String formatSeconds(int seconds, String delim, String format, TimeUnit maxUnit, boolean hideZero) {
        Validate.isTrue(seconds >= 0, "Seconds must be >= 0");

        int minutes = -1;
        int hours = -1;
        int days = -1;

        if (maxUnit != TimeUnit.SECONDS) {
            minutes = seconds / 60;
            seconds -= minutes * 60;

            if (maxUnit != TimeUnit.MINUTES) {
                hours = minutes / 60;
                minutes -= hours * 60;

                if (maxUnit != TimeUnit.HOURS) {
                    days = hours / 24;
                    hours -= days * 24;
                }
            }
        }

        if (days == -1 || (hideZero && days == 0)) {
            format = format.replace("{d}", "");
            format = format.replace("{dn}", "");
            format = format.replace("{ds}", "");
        } else {
            format = format.replace("{d}", days + " day" + (days == 1 ? "" : "s"));
            format = format.replace("{dn}", days < 10 ? ("0" + days) : Integer.toString(days));
            format = format.replace("{ds}", days + "d");
        }

        if (hours == -1 || (hideZero && hours == 0)) {
            format = format.replace("{h}", "");
            format = format.replace("{hn}", "");
            format = format.replace("{hs}", "");
        } else {
            format = format.replace("{h}", hours + " hour" + (hours == 1 ? "" : "s"));
            format = format.replace("{hn}", hours < 10 ? ("0" + hours) : Integer.toString(hours));
            format = format.replace("{hs}", hours + "h");
        }

        if (minutes == -1 || (hideZero && minutes == 0)) {
            format = format.replace("{m}", "");
            format = format.replace("{mn}", "");
            format = format.replace("{ms}", "");
        } else {
            format = format.replace("{m}", minutes + " minute" + (minutes == 1 ? "" : "s"));
            format = format.replace("{mn}", minutes < 10 ? ("0" + minutes) : Integer.toString(minutes));
            format = format.replace("{ms}", minutes + "m");
        }

        if (hideZero && seconds == 0) {
            format = format.replace("{s}", "");
            format = format.replace("{sn}", "");
            format = format.replace("{ss}", "");
        } else {
            format = format.replace("{s}", seconds + " second" + (seconds == 1 ? "" : "s"));
            format = format.replace("{sn}", seconds < 10 ? ("0" + seconds) : Integer.toString(seconds));
            format = format.replace("{ss}", seconds + "s");
        }

        format = PATTERN_REPEATING_DELIM.matcher(format).replaceAll("");
        format = format.replace("{-}", delim);

        return format;
    }

    public static class DefaultTimeFormat {

        private DefaultTimeFormat() {}

        /**
         * 5 days 20 hours 35 minutes 55 seconds
         */
        public final static String LONG = "{d}{-}{h}{-}{m}{-}{s}";

        /**
          * 05:20:35:55
          */
        public final static String SHORT = "{dn}{-}{hn}{-}{mn}{-}{sn}";

        /**
         * 5d 20h 35m 55s
         */
        public final static String SHORT_ABBR = "{ds}{-}{hs}{-}{ms}{-}{ss}";

    }

    @Deprecated
    public enum TimeFormat {

        LONG, // 5 days 20 hours 35 minutes 55 seconds
        SHORT, // 05:20:35:55
        SHORT_ABBR // 5d20h35m55s

    }

}
package me.st28.flexseries.flexlib.utils;

import org.apache.commons.lang.Validate;

public final class TimeUtils {

    private TimeUtils() {}

    public static String translateSeconds(int seconds) {
        return translateSeconds(seconds, TimeUtils.TimeFormat.LONG);
    }

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

    public enum TimeFormat {

        LONG, // 5 days 20 hours 35 minutes 55 seconds
        SHORT, // 05:20:35:55
        SHORT_ABBR // 5d20h35m55s

    }

}
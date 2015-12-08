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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Utility methods for Bukkit's Location class.
 */
public final class LocationUtils {

    private LocationUtils() {}

    /**
     * Converts a location to a string.
     *
     * @param location The location to convert.
     * @param useYawPitch True to use the yaw and pitch of the location.<br />
     *                    False to use 0F as the value for both yaw and pitch in the converted location.
     * @return A string representing the location.<br />
     *         <b>Format: </b> <code>world;y;x;z;yaw;pitch</code>
     */
    public static String locationToString(Location location, boolean useYawPitch) {
        Validate.notNull(location, "Location cannot be null.");

        String returnString = location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ();

        if (useYawPitch) {
            return returnString + ";" + location.getYaw() + ";" + location.getPitch();
        } else {
            return returnString + ";0;0";
        }
    }

    /**
     * Converts a string to a location.
     *
     * @param rawLocation The raw location.<br />
     *                    <b>Format:</b> <code>world;x;y;z;yaw;pitch</code>
     * @return A new Location object that represents the input string.
     */
    public static Location stringToLocation(String rawLocation) {
        Validate.notNull(rawLocation, "Raw location cannot be null.");

        String[] split = rawLocation.split(";");

        if (split.length != 6) {
            throw new IllegalArgumentException("Invalid location '" + rawLocation + "'");
        }

        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]);
        float pitch = Float.parseFloat(split[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Saves a location to a configuration section.
     *
     * @param location The location to save.
     * @param config The configuration section to save the location in.
     */
    public static void saveLocationToConfiguration(Location location, ConfigurationSection config) {
        config.set("world", location.getWorld().getName());
        config.set("x", location.getX());
        config.set("y", location.getY());
        config.set("z", location.getZ());
        config.set("yaw", location.getYaw());
        config.set("pitch", location.getPitch());
    }

    /**
     * Reads a location from a configuration section.
     *
     * @param config The configuration to read the location from.
     * @return A location representing the values found in the configuration.
     */
    public static Location loadLocationFromConfiguration(ConfigurationSection config) {
        World world = Bukkit.getWorld(config.getString("world"));
        double x = config.getDouble("x");
        double y = config.getDouble("y");
        double z = config.getDouble("z");
        float yaw = (float) config.getDouble("yaw");
        float pitch = (float) config.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

}
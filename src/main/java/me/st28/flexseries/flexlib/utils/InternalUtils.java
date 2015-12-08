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

import org.bukkit.Bukkit;

public final class InternalUtils {

    private InternalUtils() {}

    public static String getBukkitVersion() {
        return Bukkit.getServer().getClass().getCanonicalName().split("\\.")[3];
    }

    public static String getNMSClassName(String name) {
        return "net.minecraft.server." + getBukkitVersion() + "." + name;
    }

    public static Class getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + name);
    }

    public static String getCBClassName(String name) {
        return "org.bukkit.craftbukkit." + getBukkitVersion() + "." + name;
    }

    public static Class getCBClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + "." + name);
    }

}
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
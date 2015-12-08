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

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Represents a reference to a sound.
 */
public final class SoundRef {

    private Sound sound;
    private float volume;
    private float pitch;

    public SoundRef(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundRef(ConfigurationSection config) {
        sound = Sound.valueOf(config.getString("sound").toUpperCase());
        volume = (float) config.getDouble("volume", 1D);
        pitch = (float) config.getDouble("pitch", 1D);
    }

    public void play(Location location) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    public void play(Player player) {
        play(player, true);
    }

    public void play(Player player, boolean onlyPlayer) {
        if (onlyPlayer) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } else {
            play(player.getLocation());
        }
    }

}
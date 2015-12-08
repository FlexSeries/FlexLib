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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Basic representation of a material.
 */
public final class MaterialRef {

    private final Material material;
    private final short damage;

    public MaterialRef(String raw) {
        Validate.notNull(raw, "Raw cannot be null.");

        String[] split = raw.split(":");

        if (split.length > 1) {
            material = Material.valueOf(split[0].toUpperCase());
            damage = Short.parseShort(split[1]);
        } else {
            material = Material.valueOf(raw.toUpperCase());
            damage = 0;
        }
    }

    public MaterialRef(Material material, short damage) {
        Validate.notNull(material, "Material cannot be null.");
        Validate.isTrue(damage >= 0, "Damage value must be 0 or more.");

        this.material = material;
        this.damage = damage;
    }

    public MaterialRef(ItemStack item) {
        Validate.notNull(item, "Item cannot be null.");

        this.material = item.getType();
        this.damage = item.getDurability();
    }

    public final Material getMaterial() {
        return material;
    }

    public final short getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return material.toString() + ":" + damage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialRef that = (MaterialRef) o;

        return damage == that.damage && material == that.material;
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + (int) damage;
        return result;
    }

}
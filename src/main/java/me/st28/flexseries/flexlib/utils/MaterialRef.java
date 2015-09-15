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
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
package me.st28.flexseries.flexlib.utils.packet;

import me.st28.flexseries.flexlib.utils.InternalUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketHelper {

    private static Method methodCbCraftPlayerGetHandle;
    private static Field fieldNmsEntityPlayerPlayerConnection;
    private static Method methodNmsPlayerConnectionSendPacket;

    static {
        try {
            Class classCbCraftPlayer = InternalUtils.getCBClass("entity.CraftPlayer");
            Class classNmsEntityPlayer = InternalUtils.getNMSClass("EntityPlayer");
            Class classNmsPlayerConnection = InternalUtils.getNMSClass("PlayerConnection");
            Class classNmsPacket = InternalUtils.getNMSClass("Packet");

            methodCbCraftPlayerGetHandle = classCbCraftPlayer.getMethod("getHandle");

            fieldNmsEntityPlayerPlayerConnection = classNmsEntityPlayer.getField("playerConnection");

            methodNmsPlayerConnectionSendPacket = classNmsPlayerConnection.getMethod("sendPacket", classNmsPacket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendPacket(WrappedPacket packet, Player... players) {
        Validate.notNull(packet, "Packet cannot be null.");

        try {
            final Object builtPacket = packet.buildPacket();

            for (Player player : players) {
                final Object connection = fieldNmsEntityPlayerPlayerConnection.get(methodCbCraftPlayerGetHandle.invoke(player));

                methodNmsPlayerConnectionSendPacket.invoke(connection, builtPacket);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
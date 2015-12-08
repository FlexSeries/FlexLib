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
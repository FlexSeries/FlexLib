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
import me.st28.flexseries.flexlib.utils.packet.WrappedPacket;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class WrappedPacketPlayOutTitle extends WrappedPacket {

    /**
     * Fade in time, stay time, fade out time
     */
    private static Constructor constructor;

    static {
        try {
            Class classPacket = InternalUtils.getNMSClass("PacketPlayOutTitle");
            Class classTitleAction = InternalUtils.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
            Class classIChatBaseComponent = InternalUtils.getNMSClass("IChatBaseComponent");

            constructor = classPacket.getConstructor(classTitleAction, classIChatBaseComponent, int.class, int.class, int.class);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TitleAction.init();
    }

    // ------------------------------------------------------------------------------------------ //

    private int fadeInTime = -1;
    private int stayTime = -1;
    private int fadeOutTime = -1;

    private TitleAction action;

    private String text;

    public WrappedPacketPlayOutTitle() {}

    public int getFadeInTime() {
        return fadeInTime;
    }

    public void setFadeInTime(int fadeInTime) {
        this.fadeInTime = fadeInTime;
    }

    public int getStayTime() {
        return stayTime;
    }

    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }

    public int getFadeOutTime() {
        return fadeOutTime;
    }

    public void setFadeOutTime(int fadeOutTime) {
        this.fadeOutTime = fadeOutTime;
    }

    public TitleAction getAction() {
        return action;
    }

    public void setAction(TitleAction action) {
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Object buildPacket() {
        try {
            return constructor.newInstance(action.enumValue, new WrappedChatComponentText(text).toNmsObject(), fadeInTime, stayTime, fadeOutTime);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------ //

    public enum TitleAction {

        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET;

        private Object enumValue;

        static void init() {
            try {
                Class classTitleAction = InternalUtils.getNMSClass("PacketPlayOutTitle$EnumTitleAction");

                Map<String, Object> enumValues = new HashMap<>();

                for (Object enumConstant : classTitleAction.getEnumConstants()) {
                    enumValues.put(enumConstant.toString(), enumConstant);
                }

                for (TitleAction action : values()) {
                    action.enumValue = enumValues.get(action.toString());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
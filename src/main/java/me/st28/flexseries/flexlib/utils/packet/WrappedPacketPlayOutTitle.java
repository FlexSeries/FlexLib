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
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
package me.st28.flexseries.flexlib.utils.title;

import me.st28.flexseries.flexlib.utils.packet.PacketHelper;
import me.st28.flexseries.flexlib.utils.packet.WrappedPacketPlayOutTitle;
import me.st28.flexseries.flexlib.utils.packet.WrappedPacketPlayOutTitle.TitleAction;
import org.bukkit.entity.Player;

public class ScreenTitle {

    public static void clear(Player... players) {
        WrappedPacketPlayOutTitle clear = new WrappedPacketPlayOutTitle();
        clear.setAction(TitleAction.CLEAR);

        PacketHelper.sendPacket(clear, players);
    }

    public static void reset(Player... players) {
        WrappedPacketPlayOutTitle reset = new WrappedPacketPlayOutTitle();
        reset.setAction(TitleAction.RESET);

        PacketHelper.sendPacket(reset, players);
    }
    private String title;
    private String subtitle;

    private int fadeIn;
    private int stay;
    private int fadeOut;

    private boolean inTicks = false;

    public ScreenTitle(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public ScreenTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public void setInTicks(boolean inTicks) {
        this.inTicks = inTicks;
    }

    public void sendTo(Player... players) {
        WrappedPacketPlayOutTitle times = new WrappedPacketPlayOutTitle();

        times.setAction(TitleAction.TIMES);
        times.setFadeInTime(inTicks ? fadeIn : fadeIn * 20);
        times.setStayTime(inTicks ? stay : stay * 20);
        times.setFadeOutTime(inTicks ? fadeOut : fadeOut * 20);

        WrappedPacketPlayOutTitle title = new WrappedPacketPlayOutTitle();
        title.setAction(TitleAction.TITLE);
        title.setText(this.title);

        WrappedPacketPlayOutTitle subtitle = this.subtitle == null ? null : new WrappedPacketPlayOutTitle();
        if (subtitle != null) {
            subtitle.setAction(TitleAction.SUBTITLE);
            subtitle.setText(this.subtitle);
        }

        PacketHelper.sendPacket(times, players);
        PacketHelper.sendPacket(title, players);
        if (subtitle != null) {
            PacketHelper.sendPacket(subtitle, players);
        }
    }

}
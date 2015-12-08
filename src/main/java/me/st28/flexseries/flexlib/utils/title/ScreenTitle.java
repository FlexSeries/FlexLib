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
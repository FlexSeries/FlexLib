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
package me.st28.flexseries.flexlib.message.list;

import me.st28.flexseries.flexlib.message.MessageMasterManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.McmlMessageReference;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.MiscUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ListBuilder {

    private static ListManager getListManager() {
        return FlexPlugin.getGlobalModule(ListManager.class);
    }

    private static String processMessage(String message) {
        return FlexPlugin.getGlobalModule(MessageMasterManager.class).processMessage(message);
    }

    private ListHeader header;
    private String headerKey;
    private String headerValue;

    private String nextPageCommand;

    private String label;
    private List<MessageReference> messages = new ArrayList<>();

    private int pageItems = -1; // -1 = Use default from the ListManager

    public ListBuilder(String header, String key, String value, String label) {
        this.header = getListManager().getHeaderFormat(header);
        this.headerKey = key;
        this.headerValue = value;
        this.label = label;
    }

    public void addMessage(MessageReference reference) {
        messages.add(reference);
    }

    public void addMessage(String message) {
        messages.add(new McmlMessageReference(processMessage(message)));
    }

    public void addMessage(String format, String key, String value) {
        String rawFormat = getListManager().getElementFormat(format).replace("{KEY}", key);
        if (value != null) {
            rawFormat = rawFormat.replace("{VALUE}", value);
        }
        messages.add(new McmlMessageReference(processMessage(rawFormat)));
    }

    public void addMessage(String format, Map<String, String> replacements) {
        String rawFormat = getListManager().getElementFormat(format);
        for (Entry<String, String> entry : replacements.entrySet()) {
            rawFormat = rawFormat.replace(entry.getKey(), entry.getValue());
        }
        messages.add(new McmlMessageReference(processMessage(rawFormat)));
    }

    public void enableNextPageNotice(String nextPageCommand) {
        this.nextPageCommand = nextPageCommand;
    }

    public int getPageItems() {
        return pageItems == -1 ? getListManager().pageItems : pageItems;
    }

    public void setPageItems(int pageItems) {
        Validate.isTrue(pageItems > 0, "Page items must be greater than zero.");
        this.pageItems = pageItems;
    }

    /**
     * @return All of the messages contained within the builder.<br />
     *         To send a single page of messages, use {@link #getMessages(int)} instead.
     */
    public List<MessageReference> getMessages() {
        ListManager listManager = getListManager();

        List<MessageReference> returnList = new ArrayList<>();
        returnList.add(new McmlMessageReference(header.getFormattedHeader(-1, -1, headerKey, headerValue)));
        for (int i = 0; i < messages.size(); i++) {
            try {
                returnList.add(messages.get(i).duplicate(new ReplacementMap("{LABEL}", label).put("{INDEX}", Integer.toString(i + 1)).getMap()));
            } catch (Exception ex) {
                break;
            }
        }

        if (returnList.size() == 1) {
            returnList.add(new McmlMessageReference(listManager.msgNoElements));
        }

        return returnList;
    }

    /**
     * @return A page worth of messages contained within the builder.<br />
     *         To send all of the messages, use {@link #getMessages()} instead.
     */
    public List<MessageReference> getMessages(int page) {
        ListManager listManager = getListManager();

        int pageItems = getPageItems();
        int maxPages = MiscUtils.getPageCount(messages.size(), pageItems);

        List<MessageReference> returnList = new ArrayList<>();
        returnList.add(new McmlMessageReference(header.getFormattedHeader(page, maxPages, headerKey, headerValue)));
        for (int i = 0; i < pageItems; i++) {
            int curIndex = i + ((page - 1) * pageItems);

            try {
                returnList.add(messages.get(curIndex).duplicate(new ReplacementMap("{LABEL}", label).put("{INDEX}", Integer.toString(curIndex + 1)).getMap()));
            } catch (Exception ex) {
                if (i == 0) {
                    returnList.add(new McmlMessageReference(listManager.msgNoElements));
                }
                break;
            }

            if (i == pageItems - 1 && page < maxPages && nextPageCommand != null) {
                returnList.add(new McmlMessageReference(listManager.msgNextPage.replace("{COMMAND}", nextPageCommand.replace("{LABEL}", label)).replace("{NEXTPAGE}", Integer.toString(page + 1))));
            }
        }

        return returnList;
    }

    /**
     * @see #getMessages()
     */
    public void sendTo(CommandSender sender) {
        for (MessageReference message : getMessages()) {
            message.sendTo(sender);
        }
    }

    /**
     * @see #getMessages(int)
     */
    public void sendTo(CommandSender sender, int page) {
        for (MessageReference message : getMessages(page)) {
            message.sendTo(sender);
        }
    }

}
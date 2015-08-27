package me.st28.flexseries.flexlib.message.reference;

import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class FancyMessageReference extends MessageReference {

    private FancyMessage message;

    public FancyMessageReference(FancyMessage message) {
        Validate.notNull(message, "Message cannot be null.");
        this.message = message;
    }

    @Override
    public MessageReference duplicate(Map<String, Object> replacements) {
        return new FancyMessageReference(message);
    }

    @Override
    public void sendTo(CommandSender sender) {
        message.send(sender);
    }

    @Override
    public void sendTo(CommandSender sender, Map<String, Object> replacements) {
        // TODO: Use replacements
        message.send(sender);
    }

}
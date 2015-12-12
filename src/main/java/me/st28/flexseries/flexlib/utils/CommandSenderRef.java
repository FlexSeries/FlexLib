package me.st28.flexseries.flexlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A reference to a command sender (player or console).
 */
public class CommandSenderRef {

    private final static String IDENTIFIER_CONSOLE = "c={}";
    private final static String IDENTIFIER_PLAYER = "p=";

    private String identifier;

    public CommandSenderRef(CommandSender sender) {
        if (sender instanceof Player) {
            identifier = IDENTIFIER_PLAYER + ((Player) sender).getUniqueId().toString();
        } else if (sender instanceof ConsoleCommandSender) {
            identifier = IDENTIFIER_CONSOLE;
        } else {
            throw new IllegalArgumentException("Unsupported CommandSender implementation: " + sender.getClass().getCanonicalName());
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public CommandSender getCommandSender() {
        if (identifier.startsWith(IDENTIFIER_PLAYER)) {
            return Bukkit.getPlayer(UUID.fromString(identifier.replace(IDENTIFIER_PLAYER, "")));
        } else if (identifier.startsWith(IDENTIFIER_CONSOLE)) {
            return Bukkit.getConsoleSender();
        }
        return null;
    }

}
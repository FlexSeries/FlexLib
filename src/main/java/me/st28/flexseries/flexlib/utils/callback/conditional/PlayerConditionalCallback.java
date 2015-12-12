package me.st28.flexseries.flexlib.utils.callback.conditional;

import me.st28.flexseries.flexlib.utils.callback.ConditionalCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * A conditional callback that is executed if the given UUID is an online player.
 */
public class PlayerConditionalCallback extends ConditionalCallback {

    public PlayerConditionalCallback() {
        super(true); // Execute callback synchronously
    }

    @Override
    protected boolean canExecute(Map<String, Object> arguments) {
        UUID uuid = (UUID) arguments.get("uuid");

        if (uuid == null) {
            throw new IllegalArgumentException("No UUID provided.");
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            arguments.put("player", player);
        }

        return player != null;
    }

    @Override
    protected void run(Map<String, Object> arguments) {
        simpleRun((Player) arguments.get("player"));
    }

    protected void simpleRun(Player player) {}

}
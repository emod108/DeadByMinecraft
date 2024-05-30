package me.mod108.deadbyminecraft.managers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;

// A class which can disable player's movement abilities
public class FreezeManager implements Listener, CommandExecutor {
    private static final int NOT_FROZEN = -1;
    private final ArrayList<Player> frozenPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (getFrozenIndex(e.getPlayer()) == NOT_FROZEN)
            return;

        // Getting from and to locations
        final Location to = e.getTo();
        if (to == null)
            return;
        final Location from = e.getFrom().clone();

        from.setPitch(to.getPitch());
        from.setYaw(to.getYaw());
        e.setTo(from);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String label, final String[] args) {
        if (sender instanceof final Player player) {
            if (getFrozenIndex(player) == NOT_FROZEN) {
                freeze(player);
                sender.sendMessage(ChatColor.YELLOW + "You are frozen now!");
            } else {
                unFreeze(player);
                sender.sendMessage(ChatColor.YELLOW + "Now you are free to move!");
            }

        }
        return true;
    }

    // Returns index if player is frozen
    // Returns NOT_FROZEN if player is not frozen
    private int getFrozenIndex(final Player player) {
        for (int i = 0; i < frozenPlayers.size(); ++i) {
            if (player.getUniqueId().equals(frozenPlayers.get(i).getUniqueId()))
                return i;
        }
        return NOT_FROZEN;
    }

    public boolean isFrozen(final Player player) {
        return getFrozenIndex(player) != NOT_FROZEN;
    }

    public void freeze(final Player player) {
        if (!isFrozen(player))
            frozenPlayers.add(player);
    }

    public void unFreeze(final Player player) {
        final int index = getFrozenIndex(player);
        if (index != NOT_FROZEN)
            frozenPlayers.remove(index);
    }
}

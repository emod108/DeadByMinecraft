package me.mod108.deadbyminecraft.managers;

import me.mod108.deadbyminecraft.utility.MyPair;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.UUID;

// A class which can disable player's movement abilities
public class FreezeManager implements Listener, CommandExecutor {
    private static final int NOT_FROZEN = -1;

    // Array, which lists players who are frozen
    // If Boolean is true, it means they also can't move their camera
    private final ArrayList<MyPair<UUID, Boolean>> frozenPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        final int frozenIndex = getFrozenIndex(e.getPlayer().getUniqueId());
        if (frozenIndex == NOT_FROZEN)
            return;

        // If true, players can't even move their camera
        if (frozenPlayers.get(frozenIndex).second) {
            e.setCancelled(true);
            return;
        }

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof final Player player) {
            final UUID playerUUID = player.getUniqueId();
            if (getFrozenIndex(playerUUID) == NOT_FROZEN) {
                freeze(playerUUID, true);
                sender.sendMessage(ChatColor.YELLOW + "You are frozen now!");
            } else {
                unFreeze(playerUUID);
                sender.sendMessage(ChatColor.YELLOW + "Now you are free to move!");
            }

        }
        return true;
    }

    // Returns index if player is frozen
    // Returns NOT_FROZEN if player is not frozen
    private int getFrozenIndex(final UUID player) {
        for (int i = 0; i < frozenPlayers.size(); ++i) {
            if (player.equals(frozenPlayers.get(i).first))
                return i;
        }
        return NOT_FROZEN;
    }

    public boolean isFrozen(final UUID player) {
        return getFrozenIndex(player) != NOT_FROZEN;
    }

    public void freeze(final UUID player, final boolean freezeCamera) {
        if (!isFrozen(player))
            frozenPlayers.add(new MyPair<>(player, freezeCamera));
    }

    public void unFreeze(final UUID player) {
        final int index = getFrozenIndex(player);
        if (index != NOT_FROZEN)
            frozenPlayers.remove(index);
    }
}

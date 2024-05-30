package me.mod108.deadbyminecraft.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.ArrayList;

// A class which allows or disallows health regeneration
public class HealthRegainManager implements Listener, CommandExecutor {
    private static final int CAN_REGAIN_HEALTH = -1;
    private final ArrayList<Player> healthRegainDisabled = new ArrayList<>();

    @EventHandler
    public void onHealthRegain(final EntityRegainHealthEvent e) {
        final Entity entity = e.getEntity();
        if (entity instanceof final Player player) {
            // Allowing custom health regain
            if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.CUSTOM)
                return;

            // Checking if the player is in the list
            if (getHealthRegainDisabledIndex(player) != CAN_REGAIN_HEALTH)
                e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String label, final String[] args) {
        if (sender instanceof final Player player) {
            if (getHealthRegainDisabledIndex(player) == CAN_REGAIN_HEALTH)
                disableHealthRegain(player);
            else
                enableHealthRegain(player);
        }
        return true;
    }

    private int getHealthRegainDisabledIndex(final Player player) {
        for (int i = 0; i < healthRegainDisabled.size(); ++i) {
            if (player.getUniqueId().equals(healthRegainDisabled.get(i).getUniqueId()))
                return i;
        }
        return CAN_REGAIN_HEALTH;
    }

    public boolean canRegainHealth(final Player player) {
        return getHealthRegainDisabledIndex(player) == CAN_REGAIN_HEALTH;
    }

    public void disableHealthRegain(final Player player) {
        if (!canRegainHealth(player))
            healthRegainDisabled.add(player);
    }

    public void enableHealthRegain(final Player player) {
        final int index = getHealthRegainDisabledIndex(player);
        if (index != CAN_REGAIN_HEALTH)
            healthRegainDisabled.remove(index);
    }
}

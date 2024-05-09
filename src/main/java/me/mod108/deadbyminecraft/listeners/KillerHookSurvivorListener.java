package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.KillerHookSurvivorEvent;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KillerHookSurvivorListener implements Listener {
    @EventHandler
    public void onKillerHookSurvivor(final KillerHookSurvivorEvent e) {
        final Killer killer = e.getKiller();
        final Hook hook = e.getHook();

        // Killer must stand in front of the hook
        final Location hookingLocation = hook.getLocation().getBlock().
                getRelative(hook.getDirection(), 2).getLocation();

        final Location killerLocation = killer.getLocation().getBlock().getLocation();

        if (!hookingLocation.equals(killerLocation)) {
            killer.getPlayer().sendMessage(ChatColor.YELLOW + "You must stand in front of the hook to hook survivors!");
            return;
        }

        killer.hookSurvivor(hook);
    }
}

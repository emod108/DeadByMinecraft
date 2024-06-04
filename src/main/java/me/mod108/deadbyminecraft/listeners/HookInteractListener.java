package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.events.HookInteractEvent;
import me.mod108.deadbyminecraft.events.KillerHookSurvivorEvent;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HookInteractListener implements Listener {
    @EventHandler
    public void onHookInteract(final HookInteractEvent e) {
        final Character player = e.getPlayer();
        if (player instanceof final Killer killer) {
            // If killer is carrying a survivor and this hook can be used to hook someone
            final Hook hook = e.getHook();
            if (killer.getCarriedSurvivor() != null && hook.availableForHooking())
                Bukkit.getServer().getPluginManager().callEvent(new KillerHookSurvivorEvent(killer, hook));
        }
    }
}

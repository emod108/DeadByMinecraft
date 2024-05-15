package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.props.vaultable.Vaultable;
import me.mod108.deadbyminecraft.targets.characters.Character;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VaultEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Vaultable vaultable;

    public VaultEvent(final Character player, final Vaultable vaultable) {
        this.player = player;
        this.vaultable = vaultable;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Character getPlayer() {
        return player;
    }

    public Vaultable getVaultable() {
        return vaultable;
    }
}

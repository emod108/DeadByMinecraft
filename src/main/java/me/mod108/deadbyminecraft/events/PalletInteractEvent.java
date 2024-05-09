package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PalletInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Pallet pallet;

    public PalletInteractEvent(final Character player, final Pallet pallet) {
        this.player = player;
        this.pallet = pallet;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Character getPlayer() {
        return player;
    }

    public Pallet getPallet() {
        return pallet;
    }
}

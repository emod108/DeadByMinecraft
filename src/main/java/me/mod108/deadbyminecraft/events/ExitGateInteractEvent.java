package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ExitGateInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final ExitGate exitGate;

    public ExitGateInteractEvent(final Character player, final ExitGate exitGate) {
        this.player = player;
        this.exitGate = exitGate;
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

    public ExitGate getExitGate() {
        return exitGate;
    }
}

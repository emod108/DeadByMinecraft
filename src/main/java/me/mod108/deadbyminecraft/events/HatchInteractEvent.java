package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Hatch;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HatchInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Hatch hatch;

    public HatchInteractEvent(final Character player, final Hatch hatch) {
        this.player = player;
        this.hatch = hatch;
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

    public Hatch getHatch() {
        return hatch;
    }
}

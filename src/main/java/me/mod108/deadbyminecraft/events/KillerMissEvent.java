package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillerMissEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Killer killer;

    public KillerMissEvent(final Killer killer) {
        this.killer = killer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Killer getKiller() {
        return killer;
    }
}

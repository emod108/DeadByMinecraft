package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillerAttackSurvivorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Killer killer;
    private final Survivor survivor;

    public KillerAttackSurvivorEvent(final Killer killer, final Survivor survivor) {
        this.killer = killer;
        this.survivor = survivor;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Killer getKiller() {
        return killer;
    }

    public Survivor getSurvivor() {
        return survivor;
    }
}

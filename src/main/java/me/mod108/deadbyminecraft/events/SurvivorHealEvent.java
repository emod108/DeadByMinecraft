package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SurvivorHealEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Survivor performer;
    private final Survivor healingTarget;

    public SurvivorHealEvent(final Survivor performer, final Survivor healingTarget) {
        this.performer = performer;
        this.healingTarget = healingTarget;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Survivor getPerformer() {
        return performer;
    }

    public Survivor getHealingTarget() {
        return healingTarget;
    }
}

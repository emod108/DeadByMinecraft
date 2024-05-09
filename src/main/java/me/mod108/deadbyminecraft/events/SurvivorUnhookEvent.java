package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SurvivorUnhookEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Survivor performer;
    private final Survivor unhookTarget;

    public SurvivorUnhookEvent(final Survivor performer, final Survivor unhookTarget) {
        this.performer = performer;
        this.unhookTarget = unhookTarget;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Survivor getPerformer() {
        return performer;
    }

    public Survivor getUnhookTarget() {
        return unhookTarget;
    }
}

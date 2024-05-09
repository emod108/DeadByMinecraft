package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Hook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillerHookSurvivorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Killer killer;
    private final Hook hook;

    public KillerHookSurvivorEvent(final Killer killer, final Hook hook) {
        this.killer = killer;
        this.hook = hook;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Killer getKiller() {
        return killer;
    }

    public Hook getHook() {
        return hook;
    }
}

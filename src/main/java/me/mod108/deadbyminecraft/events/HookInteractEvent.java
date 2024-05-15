package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Hook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HookInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Hook hook;

    public HookInteractEvent(final Character player, final Hook hook) {
        this.player = player;
        this.hook = hook;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Character getPlayer() {
        return player;
    }

    public Hook getHook() {
        return hook;
    }
}

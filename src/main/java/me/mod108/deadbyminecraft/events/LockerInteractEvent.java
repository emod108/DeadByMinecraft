package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Locker;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockerInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Locker locker;

    public LockerInteractEvent(final Character player, final Locker locker) {
        this.player = player;
        this.locker = locker;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Character getPlayer() {
        return player;
    }

    public Locker getLocker() {
        return locker;
    }
}

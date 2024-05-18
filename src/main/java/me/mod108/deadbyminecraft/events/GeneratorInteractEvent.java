package me.mod108.deadbyminecraft.events;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Generator;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GeneratorInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Character player;
    private final Generator generator;

    public GeneratorInteractEvent(final Character player, final Generator generator) {
        this.player = player;
        this.generator = generator;
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

    public Generator getGenerator() {
        return generator;
    }
}

package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.events.GeneratorInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class GeneratorInteractListener implements Listener {
    @EventHandler
    public void onGeneratorInteract(final GeneratorInteractEvent e) {
        final Generator generator = e.getGenerator();
        final Character player = e.getPlayer();

        // If generator is fully repaired, we can't interact with it
        if (generator.getGeneratorState() == Generator.GeneratorState.REPAIRED)
            return;

        // Checking if player is able to interact with generator
        if (!player.canInteractWithGenerator())
            return;

        // Finding out on which generator side player is
        final Location playerLocation = player.getLocation().getBlock().getLocation();
        final ArrayList<Generator.GeneratorSide> generatorSides = generator.getGeneratorSides();
        Generator.GeneratorSide generatorSide = null;
        for (final Generator.GeneratorSide side : generatorSides) {
            for (final Location block : side.getBlocks()) {
                if (playerLocation.equals(block)) {
                    generatorSide = side;
                    break;
                }
            }
            if (generatorSide != null)
                break;
        }

        // If not found the side
        if (generatorSide == null)
            return;

        // Killer interaction with generator
        if (player instanceof final Killer killer) {
            // If someone is repairing generator, we can't start breaking it
            if (generator.isBeingInteractedWith()) {
                killer.getPlayer().sendMessage(ChatColor.YELLOW + "A survivor is repairing this generator!");
                return;
            }

            // We can't break regressing generators
            if (generator.getGeneratorState() == Generator.GeneratorState.REGRESSING) {
                killer.getPlayer().sendMessage(ChatColor.YELLOW + "This generator is already regressing!");
                return;
            }

            // Generator can be broken only certain amount of times
            if (generator.getTimesBroken() >= Generator.MAX_BREAK_TIMES) {
                killer.getPlayer().sendMessage(ChatColor.YELLOW + "This generator already received maximum" +
                        " amount of break actions!");
                return;
            }

            // Start breaking generator
            killer.startBreaking(generator);
            return;
        }

        // Survivor interaction with generator
        final Survivor survivor = (Survivor) player;

        // If the killer is breaking generator, we return
        if (generator.getInteractingPlayer() instanceof Killer)
            return;

        // If side is occupied
        if (generatorSide.getPlayer() != null)
            return;

        survivor.startRepairing(generator, generatorSide);
    }
}

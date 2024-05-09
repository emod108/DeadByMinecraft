package me.mod108.deadbyminecraft.listeners;

import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.events.GeneratorInteractEvent;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class GeneratorInteractListener implements Listener {
    @EventHandler
    public void onGeneratorInteract(final GeneratorInteractEvent e) {
        final Generator generator = e.getGenerator();

        if (generator.getGeneratorState() == Generator.GeneratorState.REPAIRED)
            return;

        if (!(e.getPlayer() instanceof final Survivor survivor))
            return;

        // Ability check
        if (!survivor.canInteractWithGenerator())
            return;

        // Finding out on which generator side survivor is
        final Location survivorLocation = survivor.getPlayer().getLocation().getBlock().getLocation();
        final ArrayList<Generator.GeneratorSide> generatorSides = generator.getGeneratorSides();
        Generator.GeneratorSide generatorSide = null;
        for (final Generator.GeneratorSide side : generatorSides) {
            for (final Location block : side.getBlocks()) {
                if (survivorLocation.equals(block)) {
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

        // If side is occupied
        if (generatorSide.getPlayer() != null)
            return;

        survivor.startRepairing(generator, generatorSide);
    }
}

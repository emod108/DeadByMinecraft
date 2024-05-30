package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class RepairAction extends Action {
    // How often does the action sound play
    private static final int timeTillRepairSound = Timings.secondsToTicks(2);

    // Generator side player occupies
    private final Generator.GeneratorSide side;

    // When does the action sound play
    private int currentTicksTillRepairSound = 0;

    // If players moves, generator repairing process stops
    private Location startLocation = null;

    public RepairAction(final Survivor performer, final Generator generator, final Generator.GeneratorSide side) {
        super(performer, generator);
        this.side = side;

        // If generator was regressing, it becomes Idle
        if (generator.getGeneratorState() == Generator.GeneratorState.REGRESSING)
            generator.setGeneratorState(Generator.GeneratorState.IDLE);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // If survivor can't perform repairing anymore, we stop
        if (((Survivor) performer).isIncapacitated()) {
            end();
            return;
        }

        // Getting starting location
        if (startLocation == null)
            startLocation = performer.getLocation();

        // Checking if survivor moved
        final Location location = performer.getLocation();
        if (location.getX() != startLocation.getX() || location.getZ() != startLocation.getZ() ||
                location.getY() != startLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "You have moved! Repairing was canceled.");
            return;
        }

        // Adding repair progress
        ((Generator) target).addRepairProgress(ACTION_SPEED);

        // Repair sounds
        ++currentTicksTillRepairSound;
        if (currentTicksTillRepairSound >= timeTillRepairSound) {
            SoundManager.playForAll(location, Generator.REPAIR_SOUND, 1.0f, 0.5f);
            currentTicksTillRepairSound = 0;
        }
    }

    @Override
    public void end() {
        super.end();
        side.setPlayer(null);
    }

    @Override
    public float getProgress() {
        return ((Generator) target).getProgressPercents();
    }
}

package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.FreezeManager;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.utility.Timings;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class HealAction extends Action {
    // Healing progress achieved per tick
    private static final float HEALING_SPEED = Timings.secondsToTicks(1.0);

    // If healer moves, then action stops
    private Location healerLocation = null;

    public HealAction(final Survivor healer, final Survivor target) {
        super(healer, target);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Getting starting location
        if (healerLocation == null)
            healerLocation = performer.getLocation();

        // Checking if survivor moved
        final Location location = performer.getLocation();
        if (location.getX() != healerLocation.getX() || location.getZ() != healerLocation.getZ() ||
                location.getY() != healerLocation.getY()) {
            end();
            performer.getPlayer().sendMessage(ChatColor.RED + "You have moved! Healing was canceled.");
            return;
        }

        final Survivor healedSurvivor = (Survivor) target;
        healedSurvivor.addHealingProgress(HEALING_SPEED);
    }

    @Override
    public void end() {
        super.end();

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        final Survivor healedSurvivor = (Survivor) target;
        final FreezeManager manager = plugin.freezeManager;
        manager.unFreeze(healedSurvivor.getPlayer());
    }

    @Override
    public float getProgress() {
        return ((Generator) target).getProgressPercents();
    }
}

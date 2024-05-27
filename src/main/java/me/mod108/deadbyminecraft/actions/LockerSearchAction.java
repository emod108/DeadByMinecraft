package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Locker;
import org.bukkit.ChatColor;

public class LockerSearchAction extends LockerAction {
    public LockerSearchAction(final Killer performer, final Locker target, final int actionTimeTicks) {
        super(performer, target, actionTimeTicks, true);

        final Survivor survivor = target.getHidingSurvivor();
        if (survivor == null) {
            performer.getPlayer().sendMessage(ChatColor.YELLOW + "No one is hiding in this locker");
            return;
        }

        survivor.setHealthState(Survivor.HealthState.BEING_CARRIED);
        performer.getPlayer().sendMessage(ChatColor.YELLOW + "You have found " +
                    survivor.getPlayer().getDisplayName() + " hiding in this locker");
        survivor.getPlayer().sendMessage(ChatColor.RED + "You have been found!");
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Progressing the action
        ++currentActionTime;
        if (currentActionTime >= actionTimeTicks) {
            final Locker locker = (Locker) target;
            final Killer killer = (Killer) performer;
            final Survivor survivor = locker.getHidingSurvivor();

            if (survivor != null) {
                // Getting survivor out from the locker
                survivor.teleportFromLocker(locker);
                DeadByMinecraft.getPlugin().freezeManager.unFreeze(survivor.getPlayer());

                // Picking up survivor
                killer.getSurvivorOnShoulder(survivor);
            }

            end();
        }
    }

    @Override
    public void end() {
        super.end();

        // Unfreezing the killer
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer());
    }
}

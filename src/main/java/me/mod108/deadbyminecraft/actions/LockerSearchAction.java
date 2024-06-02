package me.mod108.deadbyminecraft.actions;

import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Locker;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class LockerSearchAction extends LockerAction {
    final Survivor hidingSurvivor;

    public LockerSearchAction(final Killer performer, final Locker target, final int actionTimeTicks) {
        super(performer, target, actionTimeTicks, true);

        hidingSurvivor = target.getHidingSurvivor();
        if (hidingSurvivor == null)
            return;

        // Found survivor
        hidingSurvivor.setHealthState(Survivor.HealthState.BEING_CARRIED);
        SoundManager.playForAll(target.getLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);
    }

    @Override
    public void run() {
        if (cancelled)
            return;
        super.run();

        // Progressing the action
        ++currentActionTime;
        if (currentActionTime >= actionTimeTicks) {
            if (hidingSurvivor != null) {
                final Locker locker = (Locker) target;
                final Killer killer = (Killer) performer;

                // Getting survivor out from the locker
                hidingSurvivor.teleportFromLocker(locker);
                DeadByMinecraft.getPlugin().freezeManager.unFreeze(hidingSurvivor.getPlayer().getUniqueId());

                // Picking up survivor
                killer.getSurvivorOnShoulder(hidingSurvivor);
            }

            end();
        }
    }

    @Override
    public void end() {
        super.end();

        // Unfreezing the killer
        DeadByMinecraft.getPlugin().freezeManager.unFreeze(performer.getPlayer().getUniqueId());
    }

    @Override
    public String getActionBar() {
        if (hidingSurvivor != null)
            return ChatColor.RED + "Found " + hidingSurvivor.getPlayer().getName();
        return ChatColor.YELLOW + "Searching empty locker";
    }
}

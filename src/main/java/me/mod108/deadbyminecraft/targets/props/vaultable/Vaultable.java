package me.mod108.deadbyminecraft.targets.props.vaultable;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Prop;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;

public abstract class Vaultable extends Prop {
    public Vaultable(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    abstract public boolean canVaultEastAndWest();

    // Returns sounds which should be played, when vaulted over this prop
    abstract public Sound getVaultingSound();
}

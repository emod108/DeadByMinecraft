package me.mod108.deadbyminecraft.targets.props.vaultable;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.props.Prop;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public abstract class Vaultable extends Prop {
    // Player, who currently vaults this prop
    private Character vaultingPlayer = null;

    public Vaultable(final Location location, final BlockFace direction) {
        super(location, direction);
    }

    @Override
    public void destroy() {
        if (vaultingPlayer != null)
            vaultingPlayer.cancelAction();

        super.destroy();
    }

    public Character getVaultingPlayer() {
        return vaultingPlayer;
    }

    public void setVaultingPlayer(final Character vaultingPlayer) {
        this.vaultingPlayer = vaultingPlayer;
    }

    abstract public boolean canVaultEastAndWest();
}

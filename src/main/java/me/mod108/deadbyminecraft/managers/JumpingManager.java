package me.mod108.deadbyminecraft.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// A class which disables jumping ability by applying a very high jump boost amplifier
public class JumpingManager implements CommandExecutor {
    // The potion level needed, to remove jumping ability
    private static final int MINIMAL_DISABLE_JUMPING_EFFECT_AMPLIFIER = 128;

    // The value should be precisely 250 to avoid a bug
    // The bug allows you to instantly teleport down when you're jumping from edges
    private static final int DISABLE_JUMPING_EFFECT_AMPLIFIER = 250;

    // This effect removes jumping ability
    private static final PotionEffect JUMPING_DISABLER = new PotionEffect(
            PotionEffectType.JUMP,
            PotionEffect.INFINITE_DURATION,
            DISABLE_JUMPING_EFFECT_AMPLIFIER,
            false,
            false,
            false
    );

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String label,  final String[] args) {
        if (sender instanceof final Player player) {
            if (canJump(player))
                disableJumping(player);
            else
                enableJumping(player);
        }

        return true;
    }

    // Returns true if player doesn't have a very high jump boost modifier
    public boolean canJump(final Player player) {
        final PotionEffect effect = player.getPotionEffect(PotionEffectType.JUMP);
        if (effect == null)
            return true;

        // Returns true if amplifier is less than MINIMAL_DISABLE_JUMPING_EFFECT_AMPLIFIER
        return effect.getAmplifier() < MINIMAL_DISABLE_JUMPING_EFFECT_AMPLIFIER;
    }

    public void disableJumping(final Player player) {
        player.removePotionEffect(PotionEffectType.JUMP);
        player.addPotionEffect(JUMPING_DISABLER);
    }

    public void enableJumping(final Player player) {
        if (!canJump(player))
            player.removePotionEffect(PotionEffectType.JUMP);
    }
}

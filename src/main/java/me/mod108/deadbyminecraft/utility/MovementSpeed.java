package me.mod108.deadbyminecraft.utility;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MovementSpeed {
    static public final float MIN_SPEED = 0.0f;
    static public final float DEFAULT_SPEED = 1.0f;
    static public final float MAX_SPEED = 50.0f;

    // Default value in Spigot's setWalkSpeed()
    static public final float DEFAULT_SPIGOT_SPEED = 0.2f;

    // Each level of the minecraft speed effect gives 20% of additional movement speed
    static public final float SPEED_EFFECT_BONUS_SPEED = 0.2f;

    // Indicates that the speed effect shouldn't be given
    static public final int NO_SPEED_EFFECT_AMPLIFIER = -1;

    // Because each 5 levels of the speed effect gives 100% of the current speed
    // After dividing by 10 we can simply give 45 levels to add needed 9/10 of the desired speed
    // But because spigot gives speed effects starting from level 0, we need to reduce this value by 1
    static final int SPEED_EFFECT_AMPLIFIER_MULTIPLY_BY_TEN = 44;

    // Max speed which can be achieved with only setWalkSpeed()
    static final float MAX_SPEED_WITH_ONLY_SPIGOT = 5.0f;

    // Actual movement speed in comparison to vanilla
    private float speed = DEFAULT_SPEED;

    // Spigot setWalkSpeed which must be set to get desired speed
    private float spigotWalkSpeed = DEFAULT_SPIGOT_SPEED;

    // Minecraft speed effect strength which must be given to get desired speed
    private int speedEffectAmplifier = NO_SPEED_EFFECT_AMPLIFIER;

    public MovementSpeed(final float speed) {
        setSpeed(speed);
    }

    // Returns the actual speed value
    public float getSpeed() {
        return speed;
    }

    // Returns spigot walk speed component
    public float getSpigotWalkSpeed() {
        return spigotWalkSpeed;
    }

    // Set new movement speed with a value between MIN_SPEED and MAX_SPEED
    public void setSpeed(float speed) {
        speed = Math.min(Math.max(speed, MIN_SPEED), MAX_SPEED);

        if (this.speed == speed)
            return;

        this.speed = speed;

        // In this case, setting Spigot Walk speed is enough
        if (speed <= MAX_SPEED_WITH_ONLY_SPIGOT) {
            spigotWalkSpeed = speed * DEFAULT_SPIGOT_SPEED;
            speedEffectAmplifier = NO_SPEED_EFFECT_AMPLIFIER;
            return;
        }

        // If requested speed is greater than 500%
        // Then we divide it by 10 to make it lower
        spigotWalkSpeed = (speed / 10.0f) * DEFAULT_SPIGOT_SPEED;
        speedEffectAmplifier = SPEED_EFFECT_AMPLIFIER_MULTIPLY_BY_TEN;
    }

    // Calculates movement speed in comparison to vanilla movement speed
    // spigotWalkSpeed - a float between 0.0 and 1.0
    // speedEffectLevels - an integer between -1 and 255
    public static float calculateSpeed(final float spigotWalkSpeed, final int speedEffectAmplifier) {
        float speed = spigotWalkSpeed / DEFAULT_SPIGOT_SPEED;

        final float bonusPerSpeedEffectLevel = speed * SPEED_EFFECT_BONUS_SPEED;
        speed += bonusPerSpeedEffectLevel * (speedEffectAmplifier + 1);

        return speed;
    }

    public static String toPercents(final float value) {
        return (value * 100) + "%";
    }

    public void applyToPlayer(final Player player) {
        player.setWalkSpeed(spigotWalkSpeed);
        player.removePotionEffect(PotionEffectType.SPEED);
        if (speedEffectAmplifier != MovementSpeed.NO_SPEED_EFFECT_AMPLIFIER) {
            final PotionEffect speedEffect = new PotionEffect(
                    PotionEffectType.SPEED,
                    PotionEffect.INFINITE_DURATION,
                    speedEffectAmplifier);
            player.addPotionEffect(speedEffect);
        }
    }

    public static void clearPlayerSpeed(final Player player) {
        player.setWalkSpeed(DEFAULT_SPIGOT_SPEED);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
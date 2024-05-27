package me.mod108.deadbyminecraft.targets.props;

import me.mod108.deadbyminecraft.targets.Target;
import org.bukkit.Sound;

public interface Breakable extends Target {
    // Returns the sound this prop should play, when targeted by Break action
    Sound getBreakingSound();

    // Returns how much time in seconds break action takes
    float getBreakingTime();

    // This method modifies object targeted by Break action.
    void getBroken();
}

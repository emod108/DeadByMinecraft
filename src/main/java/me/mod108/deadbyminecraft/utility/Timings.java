package me.mod108.deadbyminecraft.utility;

public class Timings {
    public final static int TICKS_PER_SECOND = 20;

    public static int secondsToTicks(final double seconds) {
        return (int) Math.round(seconds * TICKS_PER_SECOND);
    }

    public static double ticksToSeconds(final int ticks) {
        return ((double) ticks / TICKS_PER_SECOND);
    }
}

package me.mod108.deadbyminecraft.utility;

public class SpeedModifier {
    // Modifier shouldn't be active anymore
    static public final int DURATION_END = 0;

    // Modifier has infinite duration
    static public final int INFINITE_DURATION = -1;

    // Value of this speed modifier
    private float value;

    // For how long this modifier will be working (in ticks)
    // -1 for infinite duration
    private int time;

    // Name of the modifier
    private final String name;

    public SpeedModifier(final float value, final int time, final String name) {
        this.value = value;
        this.time = time;
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final float value) {
        this.value = value;
    }

    public int getTime() {
        return time;
    }

    public void setTime(final int time) {
        this.time = time;
    }

    public void decrementTime() {
        if (time > 0)
            --time;
    }

    public String getName() {
        return name;
    }
}
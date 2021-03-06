package pokecube.core.utils;

/** Represents a time period within the (Minecraftian) day.
 * <p>
 * 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999 last "tick" of the
 * night.
 * <p>
 * 24000 is a valid end time point and means "until the end of the night".
 * <p>
 * It's guaranteed that both values are in the range [0, 24000]. */
public final class TimePeriod
{
    public final static TimePeriod fullDay = new TimePeriod(0, 24000);

    public final static TimePeriod never   = new TimePeriod(0, 0);

    public final int               startTick;
    public final int               endTick;
    public final double            startTime;

    public final double            endTime;

    private final boolean          wrapped;

    /** 0.0/1.0 means sunrise. Noon is at 0.25, dusk at 0.5, midnight at 0.75.
     * The precision is limited to Minecraft's tick precision. */
    public TimePeriod(double start, double end)
    {
        this((int) (start * 24000), (int) (end * 24000));
    }

    public TimePeriod(int sTick, int eTick)
    {
        sTick = Math.min(Math.max(sTick, 0), 24000);
        eTick = Math.min(Math.max(eTick, 0), 24000);
        startTick = sTick;
        endTick = eTick;
        startTime = startTick / 24000.0;
        endTime = endTick / 24000.0;
        wrapped = startTick > endTick;
    }

    public TimePeriod(TimePeriod other)
    {
        if (null != other)
        {
            startTick = other.startTick;
            endTick = other.endTick;
            startTime = other.startTime;
            endTime = other.endTime;
            wrapped = other.wrapped;
        }
        else
        {
            startTick = 0;
            endTick = 24000;
            startTime = 0.0;
            endTime = 1.0;
            wrapped = false;
        }
    }

    public boolean contains(double time)
    {
        return wrapped ? time >= startTime || time <= endTime : (time >= startTime && time <= endTime);
    }

    public boolean contains(long time, long dayLength)
    {
        time = time % dayLength;
        return contains(time / ((double) dayLength));
    }

    public boolean overlaps(TimePeriod other)
    {
        if (null != other) { return (this.startTick < other.endTick && this.endTick > other.startTick); }
        return false;
    }

}

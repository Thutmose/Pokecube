package pokecube.core.utils;

/**
 * Represents a time period within the (Minecraftian) day.
 * <p>
 * 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999 last "tick" of the night.
 * <p>
 * 24000 is a valid end time point and means "until the end of the night".
 * <p>
 * It's guaranteed that both values are in the range [0, 24000] and that startTick is smaller than endTick.
 */
public final class TimePeriod
{
	public final static TimePeriod fullDay = new TimePeriod(0, 24000);
	
	/**
	 * Returns <i>null</i> if not mergable
	 */
	public static TimePeriod merge(TimePeriod one, TimePeriod two)
	{
		if( null != one && null != two && one.overlaps(two) )
		{
			return new TimePeriod(Math.min(one.startTick, two.startTick), Math.max(one.endTick, two.endTick));
		}
		else
		{
			return null;
		}
	}
	public final int startTick;
	public final int endTick;
	public final double startTime;
	
	public final double endTime;
	
	/**
	 * 0.0/1.0 means sunrise. Noon is at 0.25, dusk at 0.5, midnight at 0.75.
	 * 
	 * The precision is limited to Minecraft's tick precision.
	 */
	public TimePeriod(double start, double end)
	{
		this((int)(start * 24000), (int)(end * 24000));
	}
	
	public TimePeriod(int sTick, int eTick)
	{
		sTick = Math.min(Math.max(sTick, 0), 24000);
		eTick = Math.min(Math.max(eTick, 0), 24000);
		if( sTick <= eTick )
		{
			startTick = sTick;
			endTick = eTick;
		}
		else
		{
			startTick = eTick;
			endTick = sTick;
		}
		startTime = startTick / 24000.0;
		endTime = endTick / 24000.0;
	}
	
	public TimePeriod(TimePeriod other)
	{
		if( null != other )
		{
			startTick = other.startTick;
			endTick = other.endTick;
			startTime = other.startTime;
			endTime = other.endTime;
		}
		else
		{
			startTick = 0;
			endTick = 24000;
			startTime = 0.0;
			endTime = 1.0;
		}
	}
	
	public boolean contains(double time)
	{
		return (time >= startTime && time <= endTime);
	}
	
	public boolean contains(int time)
	{
		time = time%24000;
		return (time >= startTick && time <= endTick);
	}
	
	public boolean contains(long time)
	{
		time = time%24000;
		return (time >= startTick && time <= endTick);
	}
	
	public boolean overlaps(TimePeriod other)
	{
		if( null != other )
		{
			return (this.startTick < other.endTick && this.endTick > other.startTick);
		}
		else
		{
			return false;
		}
	}
	
}

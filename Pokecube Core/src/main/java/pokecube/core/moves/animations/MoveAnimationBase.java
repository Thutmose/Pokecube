package pokecube.core.moves.animations;

import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;

public abstract class MoveAnimationBase implements IMoveAnimation
{
    protected String  particle;
    protected int     rgba;
    protected int     duration        = 5;
    protected int     applicationTick = -1;
    protected int     particleLife    = 5;
    protected boolean customColour    = false;
    protected float   density         = 1;
    protected float   width           = 1;
    protected float   angle           = 0;
    protected boolean flat            = false;
    protected boolean reverse         = false;

    protected String  rgbaVal         = null;

    public int getColourFromMove(Move_Base move, int alpha)
    {
        alpha = Math.min(255, alpha);
        int colour = move.getType(null).colour + 0x01000000 * alpha;
        return colour;
    }

    public IMoveAnimation init(String preset)
    {
        return this;
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

    @Override
    public int getApplicationTick()
    {
        return duration;
    }

    @SideOnly(Side.CLIENT)
    public abstract void initColour(long time, float partialTicks, Move_Base move);

    @Override
    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
    }

    @SideOnly(Side.CLIENT)
    public void reallyInitRGBA()
    {
        if (rgbaVal == null) return;
        String val = rgbaVal;
        rgbaVal = null;
        int alpha = 255;
        EnumDyeColor colour = null;
        try
        {
            colour = EnumDyeColor.byDyeDamage(Integer.parseInt(val));
        }
        catch (NumberFormatException e)
        {
            try
            {
                colour = EnumDyeColor.valueOf(val);
            }
            catch (Exception e1)
            {
                for (EnumDyeColor col : EnumDyeColor.values())
                {
                    if (col.getName().equals(val))
                    {
                        colour = col;
                        break;
                    }
                }
            }
        }
        if (colour == null) return;
        rgba = colour.getColorValue() + 0x01000000 * alpha;
        customColour = true;
    }

    protected void initRGBA(String val)
    {
        this.rgbaVal = val;
    }
}

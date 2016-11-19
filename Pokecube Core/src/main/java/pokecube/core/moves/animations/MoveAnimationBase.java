package pokecube.core.moves.animations;

import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;

public abstract class MoveAnimationBase implements IMoveAnimation
{
    String            particle;
    protected int     rgba;
    int               duration     = 5;
    int               particleLife = 5;
    protected boolean customColour = false;

    public int getColourFromMove(Move_Base move, int alpha)
    {
        alpha = Math.min(255, alpha);
        int colour = move.getType(null).colour + 0x01000000 * alpha;
        return colour;
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

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
}

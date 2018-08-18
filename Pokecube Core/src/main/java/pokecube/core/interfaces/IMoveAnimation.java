package pokecube.core.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.maths.Vector3;

public interface IMoveAnimation
{
    public static class MovePacketInfo
    {
        public final Move_Base move;
        public final Entity    attacker;
        public final Entity    attacked;
        public final Vector3   source;
        public final Vector3   target;
        public int             currentTick;

        public MovePacketInfo(Move_Base move, Entity attacker, Entity attacked, Vector3 source, Vector3 target)
        {
            this.move = move;
            this.attacked = attacked;
            this.attacker = attacker;
            this.source = source;
            this.target = target;
        }
    }

    /** Actually plays the animation in the world, this is called every render
     * tick for the number of world ticks specificed in getDuration(); This is
     * used for direct GL call rendering
     * 
     * @param info
     * @param world
     * @param partialTick */
    @SideOnly(Side.CLIENT)
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick);

    /** Used if you need to spawn in something like thunder effects.
     * 
     * @param info */
    @SideOnly(Side.CLIENT)
    public void spawnClientEntities(MovePacketInfo info);

    /** How long this animation plays for in world ticks.
     * 
     * @return */
    public int getDuration();

    /** How far into the duration should the move actually be applied.
     * 
     * @return */
    public int getApplicationTick();

    /** Sets the duration.
     * 
     * @param duration */
    public void setDuration(int duration);

    @SideOnly(Side.CLIENT)
    /** Initialise colours for the move. */
    default void reallyInitRGBA()
    {
    }
}

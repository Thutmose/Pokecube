package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import thut.api.maths.Vector3;

public class AIGatherStuff extends AIBase
{
    final EntityLiving entity;
    final boolean[]    states   = { false, false };
    final int[] cooldowns = {0,0};
    Vector3            seeking  = Vector3.getNewVectorFromPool();

    public AIGatherStuff(EntityLiving entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean shouldRun()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset()
    {
        // TODO Auto-generated method stub

    }

}

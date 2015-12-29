package pokecube.core.database.abilities.misc;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import thut.api.maths.Vector3;

public class HoneyGather extends Ability
{
    int range = 4;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (Math.random() < 0.999) return;
        Vector3 here = Vector3.getNewVectorFromPool().set(mob);
        EntityLiving entity = (EntityLiving) mob;
        Random rand = entity.getRNG();
        for (int i = 0; i < range * range * range; i++)
        {

            here.set(mob).addTo(5 * (rand.nextDouble() - 0.5), 5 * (rand.nextDouble() - 0.5),
                    5 * (rand.nextDouble() - 0.5));

            IBlockState state = here.getBlockState(entity.worldObj);
            Block block = state.getBlock();
            if (block instanceof IGrowable)
            {
                IGrowable growable = (IGrowable) block;
                if (growable.canGrow(entity.worldObj, here.getPos(), here.getBlockState(entity.worldObj),
                        entity.worldObj.isRemote))
                {
                    if (!entity.worldObj.isRemote)
                    {
                        if (growable.canUseBonemeal(entity.worldObj, entity.worldObj.rand, here.getPos(), state))
                        {
                            growable.grow(entity.worldObj, entity.worldObj.rand, here.getPos(), state);
                            return;
                        }
                    }
                }
            }
        }
        here.freeVectorFromPool();
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public Ability init(Object... args)
    {
        for (int i = 1; i < 2; i++)
            if (args != null && args.length > i)
            {
                if (args[i - 1] instanceof Integer)
                {
                    range = (int) args[i - 1];
                    return this;
                }
            }
        return this;
    }
}

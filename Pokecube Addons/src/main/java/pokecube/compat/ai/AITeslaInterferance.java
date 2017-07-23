package pokecube.compat.ai;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.utils.PokeType;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;

public class AITeslaInterferance extends EntityAIBase
{
    @CapabilityInject(ITeslaConsumer.class)
    public static Capability<ITeslaConsumer> TESLA_CONSUMER = null;
    @CapabilityInject(ITeslaProducer.class)
    public static Capability<ITeslaProducer> TESLA_PRODUCER = null;
    @CapabilityInject(ITeslaHolder.class)
    public static Capability<ITeslaHolder>   TESLA_HOLDER   = null;

    final IPokemob                           pokemob;
    final Vector3                            mobLoc         = Vector3.getNewVector();
    final EntityLiving                       entity;

    public AITeslaInterferance(IPokemob pokemob_)
    {
        pokemob = pokemob_;
        entity = pokemob.getEntity();
    }

    @Override
    public boolean shouldExecute()
    {
        return pokemob.isType(PokeType.getType("electric")) && entity.ticksExisted % 5 == 0;
    }

    @Override
    /** Execute a one shot task or start executing a continuous task */
    public void updateTask()
    {
        int range = 4;
        int statFactor = pokemob.getStat(Stats.ATTACK, true) + pokemob.getStat(Stats.SPATTACK, true);
        statFactor /= 2;
        float timescale = 1f;
        int tempFactor = 0;
        float number = 10f;
        for (int i = 0; i < number; i++)
        {
            tempFactor += statFactor * MathHelper.cos((entity.ticksExisted + i / number) / timescale) / number;
        }
        statFactor = tempFactor;

        Vector3 toFill = Vector3.getNewVector();
        range *= 2;
        for (int i = 0; i < range * range * range; i++)
        {
            Cruncher.indexToVals(i, toFill);
            mobLoc.set(pokemob).addTo(toFill);
            TileEntity tile = mobLoc.getTileEntity(entity.getEntityWorld());
            if (tile == null) continue;
            ITeslaConsumer cap = null;
            try
            {
                cap = tile.getCapability(TESLA_CONSUMER, null);
            }
            catch (Exception e)
            {
                for (EnumFacing side : EnumFacing.VALUES)
                {
                    cap = tile.getCapability(TESLA_CONSUMER, side);
                    if (cap != null) break;
                }
            }
            if (cap != null)
            {
                int radSq = (int) toFill.magSq();
                radSq = Math.max(1, radSq);
                int num = statFactor / radSq;
                if (num == 0) return;
                cap.givePower(num, false);
            }
        }
    }

}

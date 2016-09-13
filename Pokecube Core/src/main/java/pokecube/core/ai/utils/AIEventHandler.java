package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.ai.thread.aiRunnables.AIIdle;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.TerrainDamageSource;
import thut.api.maths.Vector3;

public class AIEventHandler
{

    public AIEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    /** The pokemob should path out of harmful materials if hurt by them.
     * 
     * @param evt */
    public void pokemobMaterialHurt(LivingHurtEvent evt)
    {
        if (evt.getSource() instanceof TerrainDamageSource && evt.getEntityLiving() instanceof IPokemob)
        {
            IPokemob pokemob = (IPokemob) evt.getEntityLiving();
            if (!pokemob.getPokemonAIState(IPokemob.ANGRY))
            {
                Vector3 v = AIIdle.getRandomPointNear(evt.getEntity().getEntityWorld(), pokemob,
                        Vector3.getNewVector().set(pokemob), 8);
                if (v != null)
                {
                    ((EntityLiving) evt.getEntityLiving()).getNavigator().tryMoveToXYZ(v.x, v.y, v.z,
                            evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                                    .getAttributeValue());
                }
            }
        }
    }
}

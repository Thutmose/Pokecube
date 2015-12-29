package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Explode;
import thut.api.maths.Vector3;

public class Damp extends Ability
{
    Vector3 location = null;
    int range = 16;
    
    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if(move.getMove() instanceof Move_Explode)
        {
            move.failed = true;
            move.canceled = true;
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }
    
    @Override
    public Ability init(Object... args)
    {
        for (int i = 0; i < 1; i++)
            if (args != null && args.length > i)
            {
                if (args[i] instanceof IPokemob)
                {
                    MinecraftForge.EVENT_BUS.register(this);
                    location = Vector3.getNewVectorFromPool().set(args[i]);
                }
                if (args[i] instanceof Integer)
                {
                    range = (int) args[i];
                }
            }
        return this;
    }

    @Override
    public void destroy()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        if(location!=null) location.freeVectorFromPool();
    }
    
    @SubscribeEvent
    public void denyBoom(ExplosionEvent.Start boom)
    {
        Vector3 boomLoc = Vector3.getNewVectorFromPool().set(boom.explosion.getPosition());
        if(boomLoc.distToSq(location)<range*range)
        {
            boom.setCanceled(true);
        }
    }

}

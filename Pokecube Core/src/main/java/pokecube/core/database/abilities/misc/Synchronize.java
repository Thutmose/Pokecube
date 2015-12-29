package pokecube.core.database.abilities.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class Synchronize extends Ability
{
    Vector3 location = Vector3.getNewVectorFromPool();
    IPokemob pokemob;
    int range = 16;
    
    @Override
    public void onUpdate(IPokemob mob)
    {   
        location.set(mob);
        pokemob = mob;
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.statusChange != IMoveConstants.STATUS_NON
                && mob.getStatus() == IMoveConstants.STATUS_NON)
        {
            if (move.statusChange != IMoveConstants.STATUS_FRZ && move.statusChange != IMoveConstants.STATUS_SLP)
                MovesUtils.setStatus((Entity) move.attacker, move.statusChange);
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
                    pokemob = (IPokemob) args[i];
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
    }
    
    @SubscribeEvent
    public void editNature(SpawnEvent.Post event)
    {
        if(event.location.distToSq(location)<range*range && Math.random() > 0.5)
        {
            event.pokemob.setNature(pokemob.getNature());
        }
    }
}

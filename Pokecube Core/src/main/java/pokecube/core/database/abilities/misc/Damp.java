package pokecube.core.database.abilities.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Explode;
import scala.actors.threadpool.Arrays;
import thut.api.maths.Vector3;

public class Damp extends Ability
{
    IPokemob mob;
    int range = 16;
    
    @Override
    public void onUpdate(IPokemob mob)
    {
        this.mob = mob;
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
        if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT) return this;
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i)
            {
                if (args[i] instanceof IPokemob)
                {
                    MinecraftForge.EVENT_BUS.register(this);
                    System.out.println("Initing "+Arrays.toString(args));
                    mob = (IPokemob) (args[i]);
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
        if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT) return;
        MinecraftForge.EVENT_BUS.unregister(this);
        System.out.println("unIniting "+mob+" "+range);
    }
    
    @SubscribeEvent
    public void tick(ServerTickEvent evt)
    {
//        MinecraftForge.EVENT_BUS.unregister(this);
        if(mob==null || ((Entity)mob).isDead)
        {
            destroy();
        }
    }
    
    @SubscribeEvent
    public void denyBoom(ExplosionEvent.Start boom)
    {
        Vector3 boomLoc = Vector3.getNewVectorFromPool().set(boom.explosion.getPosition());
        System.out.println(boomLoc+" "+mob+" "+((Entity)mob).isDead+" "+boomLoc.distToEntity((Entity) mob)+" "+range);
        if(boomLoc.distToEntity((Entity) mob)<range)
        {
            boom.setCanceled(true);
        }
        boomLoc.freeVectorFromPool();
    }

}

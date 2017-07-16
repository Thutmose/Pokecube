package pokecube.core.database.abilities.eventwatchers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Explode;
import thut.api.maths.Vector3;

public class Damp extends Ability
{
    IPokemob mob;
    int      range = 16;

    @SubscribeEvent
    public void denyBoom(ExplosionEvent.Start boom)
    {
        Vector3 boomLoc = Vector3.getNewVector().set(boom.getExplosion().getPosition());
        if (boomLoc.distToEntity((Entity) mob) < range)
        {
            boom.setCanceled(true);
        }
    }

    @Override
    public void destroy()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public Ability init(Object... args)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return this;
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i)
            {
                if (args[i] instanceof IPokemob)
                {
                    MinecraftForge.EVENT_BUS.register(this);
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
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.getMove() instanceof Move_Explode)
        {
            move.failed = true;
            move.canceled = true;
        }
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        this.mob = mob;
    }

}

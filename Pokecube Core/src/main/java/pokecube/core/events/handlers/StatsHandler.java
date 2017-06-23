package pokecube.core.events.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.EggEvent;
import pokecube.core.events.EvolveEvent;
import pokecube.core.events.KillEvent;
import pokecube.core.events.TradeEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.items.pokecubes.EntityPokecube;

public class StatsHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void canCapture(CaptureEvent.Pre evt)
    {
        int num = evt.caught.getPokedexNb();
        if (evt.caught.getPokemonAIState(IMoveConstants.TAMED)) evt.setResult(Result.DENY);
        if (evt.caught.getPokemonAIState(IMoveConstants.DENYCAPTURE)) evt.setResult(Result.DENY);

        long lastAttempt = ((Entity) evt.caught).getEntityData().getLong("lastCubeTime");
        if (lastAttempt > evt.pokecube.getEntityWorld().getTotalWorldTime())
        {
            Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
            evt.setCanceled(true);
            if (catcher instanceof EntityPlayer)
            {
                ((EntityPlayer) catcher).sendMessage(new TextComponentTranslation("pokecube.denied"));
            }
            evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
            evt.pokecube.setDead();
            return;
        }

        if (ISpecialCaptureCondition.captureMap.containsKey(num))
        {
            Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
            if (!ISpecialCaptureCondition.captureMap.get(num).canCapture(catcher, evt.caught))
            {
                evt.setCanceled(true);
                if (catcher instanceof EntityPlayer)
                {
                    ((EntityPlayer) catcher).sendMessage(new TextComponentTranslation("pokecube.denied"));
                }
                evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
                evt.pokecube.setDead();
                return;
            }
        }
        int id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.map.containsKey(id))
        {
            PokecubeBehavior cube = IPokecube.map.get(id);
            cube.onPreCapture(evt);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordCapture(CaptureEvent.Post evt)
    {

        int id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.map.containsKey(id))
        {
            PokecubeBehavior cube = IPokecube.map.get(id);
            cube.onPostCapture(evt);
        }
        if (evt.caught.isShadow() || evt.isCanceled()) return;
        StatsCollector.addCapture(evt.caught);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordEvolve(EvolveEvent.Post evt)
    {
        if (evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordHatch(EggEvent.Hatch evt)
    {
        StatsCollector.addHatched(evt.egg);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordKill(KillEvent evt)
    {
        if (!evt.killed.isShadow()) StatsCollector.addKill(evt.killed, evt.killer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordTrade(TradeEvent evt)
    {

        if (evt.mob == null || evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
    }

}

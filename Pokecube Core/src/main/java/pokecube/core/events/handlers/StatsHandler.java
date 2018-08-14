package pokecube.core.events.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.EggEvent;
import pokecube.core.events.EvolveEvent;
import pokecube.core.events.KillEvent;
import pokecube.core.events.TradeEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.utils.Permissions;

public class StatsHandler
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void canCapture(CaptureEvent.Pre evt)
    {
        PokedexEntry entry = evt.caught.getPokedexEntry();
        if (evt.caught.getGeneralState(GeneralStates.TAMED)) evt.setResult(Result.DENY);
        if (evt.caught.getGeneralState(GeneralStates.DENYCAPTURE)) evt.setResult(Result.DENY);
        Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
        if (!EntityPokecubeBase.canCaptureBasedOnConfigs(evt.caught))
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
        Config config = PokecubeCore.core.getConfig();
        // Check permissions
        if (catcher instanceof EntityPlayer && (config.permsCapture || config.permsCaptureSpecific))
        {
            EntityPlayer player = (EntityPlayer) catcher;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            boolean denied = false;
            if (config.permsCapture
                    && !handler.hasPermission(player.getGameProfile(), Permissions.CATCHPOKEMOB, context))
                denied = true;
            if (config.permsCaptureSpecific && !denied
                    && !handler.hasPermission(player.getGameProfile(), Permissions.CATCHSPECIFIC.get(entry), context))
                denied = true;
            if (denied)
            {
                evt.setCanceled(true);
                if (catcher instanceof EntityPlayer)
                {
                    ((EntityPlayer) catcher).sendMessage(new TextComponentTranslation("pokecube.denied"));
                }
                evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getItem(), (float) 0.5);
                evt.pokecube.setDead();
            }
        }

        if (ISpecialCaptureCondition.captureMap.containsKey(entry))
        {
            if (!ISpecialCaptureCondition.captureMap.get(entry).canCapture(catcher, evt.caught))
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
        ResourceLocation id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
            cube.onPreCapture(evt);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void recordCapture(CaptureEvent.Post evt)
    {
        ResourceLocation id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.BEHAVIORS.containsKey(id))
        {
            PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(id);
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

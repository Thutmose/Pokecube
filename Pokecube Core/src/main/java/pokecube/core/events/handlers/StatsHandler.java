package pokecube.core.events.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.EggEvent;
import pokecube.core.events.EvolveEvent;
import pokecube.core.events.KillEvent;
import pokecube.core.events.TradeEvent;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;

public class StatsHandler
{
    @SubscribeEvent
    public void canCapture(CaptureEvent.Pre evt)
    {
        int num = evt.caught.getPokedexNb();

        if (ISpecialCaptureCondition.captureMap.containsKey(num))
        {
            Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
            if (!ISpecialCaptureCondition.captureMap.get(num).canCapture(catcher, evt.caught))
            {
                evt.setCanceled(true);
                if (catcher instanceof EntityPlayer)
                {
                    ((EntityPlayer) catcher).addChatMessage(new TextComponentTranslation("pokecube.denied"));
                }
                evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getEntityItem(), (float) 0.5);
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

    @SubscribeEvent
    public void recordCapture(CaptureEvent.Post evt)
    {
        StatsCollector.addCapture(evt.caught);

        int id = PokecubeItems.getCubeId(evt.filledCube);
        if (IPokecube.map.containsKey(id))
        {
            PokecubeBehavior cube = IPokecube.map.get(id);
            cube.onPostCapture(evt);
        }
        if (evt.caught.isShadow()) return;
        Entity owner = evt.caught.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;

        if (player != null && !player.worldObj.isRemote)
        {
            player.addStat(PokecubeMod.get1stPokemob, 0);
            player.addStat(PokecubeMod.pokemobAchievements.get(evt.caught.getPokedexNb()), 1);
        }
    }

    @SubscribeEvent
    public void recordEvolve(EvolveEvent.Post evt)
    {
        StatsCollector.addCapture(evt.mob);

        if (evt.mob.isShadow()) return;
        Entity owner = evt.mob.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;

        if (player != null && !player.worldObj.isRemote)
        {
            player.addStat(PokecubeMod.get1stPokemob, 0);
            player.addStat(PokecubeMod.pokemobAchievements.get(evt.mob.getPokedexNb()), 1);
        }
    }

    @SubscribeEvent
    public void recordHatch(EggEvent.Hatch evt)
    {
        StatsCollector.addHatched(evt.egg);
        Entity owner = evt.placer;
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;

        if (player != null && !player.worldObj.isRemote)
        {
            player.addStat(PokecubeMod.get1stPokemob, 0);
            player.addStat(PokecubeMod.pokemobAchievements.get(evt.egg.getPokemob().getPokedexNb()), 1);
        }
    }

    @SubscribeEvent
    public void recordKill(KillEvent evt)
    {
        if (!evt.killed.isShadow()) StatsCollector.addKill(evt.killed, evt.killer);
    }

    @SubscribeEvent
    public void recordTrade(TradeEvent evt)
    {

        if (evt.mob == null || evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
        Entity owner = evt.mob.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer) player = (EntityPlayer) owner;

        if (player != null && !player.worldObj.isRemote)
        {
            player.addStat(PokecubeMod.get1stPokemob, 0);
            player.addStat(PokecubeMod.pokemobAchievements.get(evt.mob.getPokedexNb()), 1);
        }
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
    }

}

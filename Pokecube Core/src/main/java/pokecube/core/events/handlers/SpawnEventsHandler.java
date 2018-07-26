package pokecube.core.events.handlers;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.SpawnEvent;
import pokecube.core.events.StructureEvent;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class SpawnEventsHandler
{

    public SpawnEventsHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void CapLevel(SpawnEvent.Level event)
    {
        int level = event.getInitialLevel();
        if (SpawnHandler.lvlCap) level = Math.min(level, SpawnHandler.capLevel);
        event.setLevel(level);
    }

    @SubscribeEvent
    public void StructureSpawn(StructureEvent.SpawnEntity event)
    {
        if (!(event.getEntity() instanceof EntityLiving)) return;
        EntityLiving v = (EntityLiving) event.getEntity();
        Vector3 pos = Vector3.getNewVector().set(v);
        IGuardAICapability capability = null;
        for (Object o2 : v.tasks.taskEntries)
        {
            EntityAITaskEntry taskEntry = (EntityAITaskEntry) o2;
            if (taskEntry.action instanceof GuardAI)
            {
                capability = ((GuardAI) taskEntry.action).capability;
                capability.getPrimaryTask().setPos(pos.getPos());
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void PickSpawn(SpawnEvent.Pick.Pre event)
    {
        Vector3 v = event.getLocation();
        World world = event.world;
        List<PokedexEntry> entries = Lists.newArrayList(Database.spawnables);
        Collections.shuffle(entries);
        int index = 0;
        PokedexEntry dbe = entries.get(index);
        SpawnCheck checker = new SpawnCheck(v, world);
        float weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(checker));
        double random = Math.random();
        int max = entries.size();
        Vector3 vbak = v.copy();
        while (weight <= random && index++ < max)
        {
            dbe = entries.get(index % entries.size());
            weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(checker));
            if (weight == 0) continue;
            if (!dbe.flys() && random >= weight)
            {
                if (!(dbe.swims() && v.getBlockMaterial(world) == Material.WATER))
                {
                    v = Vector3.getNextSurfacePoint2(world, vbak, Vector3.secondAxisNeg, 20);
                    if (v != null)
                    {
                        v.offsetBy(EnumFacing.UP);
                        weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(world, v));
                    }
                    else weight = 0;
                }
            }
            if (v == null)
            {
                v = vbak.copy();
            }
        }
        if (random > weight || v == null) return;
        if (dbe.legendary)
        {
            int level = SpawnHandler.getSpawnLevel(world, v, dbe);
            if (level < PokecubeMod.core.getConfig().minLegendLevel) { return; }
        }
        event.setLocation(v);
        event.setPick(dbe);
    }

    /** This is done here for when pokedex is checked, to compare to blacklist.
     * 
     * @param event */
    @SubscribeEvent
    public void onSpawnCheck(SpawnEvent.Check event)
    {
        if (!SpawnHandler.canSpawnInWorld(event.world)) event.setCanceled(true);
    }
}

package pokecube.compat.reccomplex;

import java.util.List;

import ivorius.reccomplex.events.StructureGenerationEventLite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.Database;
import pokecube.core.events.handlers.SpawnHandler;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;

public class ReComplexCompat
{

    public static void register()
    {
        ReComplexCompat compat = new ReComplexCompat();
        MinecraftForge.EVENT_BUS.register(compat);
    }

    @SubscribeEvent
    public void gen(StructureGenerationEventLite evt)
    {
        System.out.println(evt.structureName);
        if (evt instanceof StructureGenerationEventLite.Suggest) return;

        if (evt instanceof StructureGenerationEventLite.Post)
        {
            Vector3 pos = Vector3.getNewVector().set(evt.coordinates);
            AxisAlignedBB box = pos.addTo(evt.size[0] / 2, evt.size[1] / 2, evt.size[2] / 2).getAABB()
                    .expand(evt.size[0] / 2, evt.size[1] / 2, evt.size[2] / 2);
            List<Entity> entities = evt.getWorld().getEntitiesWithinAABB(Entity.class, box);
            if (entities != null && !entities.isEmpty())
            {
                for (Object o : entities)
                {
                    System.out.println(o);
                    if (o instanceof EntityLiving)
                    {
                        EntityLiving v = (EntityLiving) o;
                        pos.set(v);
                        IGuardAICapability capability = null;
                        for (Object o2 : v.tasks.taskEntries)
                        {
                            EntityAITaskEntry taskEntry = (EntityAITaskEntry) o2;
                            if (taskEntry.action instanceof GuardAI)
                            {
                                capability = ((GuardAI) taskEntry.action).capability;
                                capability.setPos(pos.getPos());
                                break;
                            }
                        }
                    }
                    if (o instanceof EntityTrainer)
                    {
                        EntityTrainer trainer = (EntityTrainer) o;
                        if (trainer.getShouldRandomize())
                        {
                            randomizeTrainerTeam(trainer);
                        }
                    }
                }
            }
            return;
        }

        if (!Config.biomeMap.containsKey(evt.structureName.toLowerCase(java.util.Locale.ENGLISH))) { return; }

        int biome = Config.biomeMap.get(evt.structureName.toLowerCase(java.util.Locale.ENGLISH));
        Vector3 pos = Vector3.getNewVector().set(evt.coordinates);
        System.out.println(
                "Setting " + evt.structureName + " as biome type " + BiomeDatabase.getReadableNameFromType(biome));
        for (int i = 0; i < evt.size[0]; i++)
        {
            for (int j = 0; j < evt.size[1]; j++)
            {
                for (int k = 0; k < evt.size[2]; k++)
                {
                    pos.set(evt.coordinates);
                    TerrainManager.getInstance().getTerrian(evt.getWorld(), pos.addTo(i, j, k)).setBiome(pos, biome);
                }
            }
        }
    }

    public static void randomizeTrainerTeam(EntityTrainer trainer)
    {
        // if(trainer instanceof EntityLeader)
        // {
        //
        // }
        // else
        {
            Vector3 loc = Vector3.getNewVector().set(trainer);
            int maxXp = SpawnHandler.getSpawnXp(trainer.getEntityWorld(), loc, Database.getEntry(1));
            trainer.initTrainer(trainer.getType(), maxXp);
            System.out.println("Randomized " + trainer.name);
        }
    }

}

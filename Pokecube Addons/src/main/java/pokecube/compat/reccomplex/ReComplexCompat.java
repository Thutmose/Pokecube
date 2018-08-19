package pokecube.compat.reccomplex;

import java.util.List;

import ivorius.reccomplex.events.StructureGenerationEventLite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class ReComplexCompat
{

    @Optional.Method(modid = "reccomplex")
    @CompatClass(phase = Phase.POST)
    public static void RecComplex_Compat()
    {
        System.out.println("Initialiing Recurrent Complex Compat");
        pokecube.compat.reccomplex.ReComplexCompat.register();
    }

    public static void register()
    {
        ReComplexCompat compat = new ReComplexCompat();
        MinecraftForge.EVENT_BUS.register(compat);
    }

    @SubscribeEvent
    public void gen(StructureGenerationEventLite evt)
    {
        if (evt instanceof StructureGenerationEventLite.Suggest) return;
        if (evt instanceof StructureGenerationEventLite.Post)
        {
            StructureBoundingBox bounds = evt.getBoundingBox();
            AxisAlignedBB box = new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY,
                    bounds.maxZ);
            List<Entity> entities = evt.getWorld().getEntitiesWithinAABB(Entity.class, box);
            if (entities != null && !entities.isEmpty())
            {
                for (Object o : entities)
                {
                    if (o instanceof EntityLiving)
                    {
                        EntityLiving v = (EntityLiving) o;
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
                    if (o instanceof EntityTrainer)
                    {
                        EntityTrainer trainer = (EntityTrainer) o;
                        if (trainer.getShouldRandomize())
                        {
                            PAEventsHandler.randomizeTrainerTeam(trainer, trainer.pokemobsCap);
                        }
                    }
                }
            }
            return;
        }
        if (evt.getStructureName() == null || !Config.biomeMap
                .containsKey(evt.getStructureName().toLowerCase(java.util.Locale.ENGLISH))) { return; }
        int biome = Config.biomeMap.get(evt.getStructureName().toLowerCase(java.util.Locale.ENGLISH));
        Vector3 pos = Vector3.getNewVector();
        StructureBoundingBox bounds = evt.getBoundingBox();
        for (int i = bounds.minX; i <= bounds.maxX; i++)
        {
            for (int j = bounds.minY; j <= bounds.maxY; j++)
            {
                for (int k = bounds.minZ; k < bounds.maxZ; k++)
                {
                    pos.set(i, j, k);
                    TerrainManager.getInstance().getTerrian(evt.getWorld(), pos).setBiome(pos, biome);
                }
            }
        }
    }
}

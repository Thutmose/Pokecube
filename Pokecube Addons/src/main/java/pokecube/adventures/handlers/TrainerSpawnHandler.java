package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import org.nfunk.jep.JEP;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnEvent;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public class TrainerSpawnHandler
{
    public static int                      trainerBox = 64;

    private static TrainerSpawnHandler     instance;
    public static HashSet<ChunkCoordinate> trainers   = new HashSet<ChunkCoordinate>();

    public static boolean addTrainerCoord(Entity e)
    {
        int x = (int) e.posX;
        int y = (int) e.posY;
        int z = (int) e.posZ;
        int dim = e.dimension;
        return addTrainerCoord(x, y, z, dim);
    }

    public static boolean addTrainerCoord(int x, int y, int z, int dim)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        if (trainers.contains(coord)) return false;

        return trainers.add(coord);
    }

    public static int countTrainersInArea(World world, int chunkPosX, int chunkPosY, int chunkPosZ)
    {
        int tolerance = trainerBox;

        int ret = 0;
        for (Object o : trainers)
        {
            ChunkCoordinate coord = (ChunkCoordinate) o;
            if (chunkPosX >= coord.getX() - tolerance && chunkPosZ >= coord.getZ() - tolerance
                    && chunkPosY >= coord.getY() - tolerance && chunkPosY <= coord.getY() + tolerance
                    && chunkPosX <= coord.getX() + tolerance && chunkPosZ <= coord.getZ() + tolerance
                    && world.provider.getDimension() == coord.dim)
            {
                ret++;
            }
        }
        return ret;
    }

    public static TrainerSpawnHandler getInstance()
    {
        return instance;
    }

    public static boolean removeTrainerCoord(int x, int y, int z, int dim)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        return trainers.remove(coord);
    }

    Vector3 v      = Vector3.getNewVector(), v1 = Vector3.getNewVector(), v2 = Vector3.getNewVector();

    JEP     parser = new JEP();

    public TrainerSpawnHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityVillager || event.getObject() instanceof EntityTrainer)
        {
            class Provider extends GuardAICapability implements ICapabilitySerializable<NBTTagCompound>
            {
                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    EventsHandler.storage.readNBT(EventsHandler.GUARDAI_CAP, this, null, nbt);
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (hasCapability(capability, facing)) return EventsHandler.GUARDAI_CAP.cast(this);
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return EventsHandler.GUARDAI_CAP != null && capability == EventsHandler.GUARDAI_CAP;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    return (NBTTagCompound) EventsHandler.storage.writeNBT(EventsHandler.GUARDAI_CAP, this, null);
                }
            }
            event.addCapability(new ResourceLocation("pokecube_adventures:guardai"), new Provider());
        }
    }

    public void tick(World w)
    {
        if (w.isRemote) { return; }
        if (!SpawnHandler.canSpawnInWorld(w)) return;
        ArrayList<Object> players = new ArrayList<Object>();
        players.addAll(w.playerEntities);
        if (players.size() < 1) return;
        EntityPlayer p = (EntityPlayer) players.get(w.rand.nextInt(players.size()));
        Vector3 v = SpawnHandler.getRandomSpawningPointNearEntity(w, p, trainerBox, 32);
        if (v == null) return;
        if (v.y < 0) v.y = v.getMaxY(w);
        Vector3 temp = Vector3.getNextSurfacePoint2(w, v, Vector3.secondAxisNeg, v.y);
        temp = Vector3.getNextSurfacePoint2(w, v, Vector3.secondAxisNeg, v.y);
        v = temp != null ? temp.offset(EnumFacing.UP) : v;

        if (!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ())) return;
        int count = countTrainersInArea(w, v.intX(), v.intY(), v.intZ());

        if (count < 2)
        {
            long time = System.nanoTime();
            EntityTrainer t = getTrainer(v, w);
            if (t == null) return;
            IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(t);
            TrainerSpawnEvent event = new TrainerSpawnEvent(cap.getType(), t, v.getPos(), w);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                t.setDead();
                return;
            }
            double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) System.err.println(FMLCommonHandler.instance().getEffectiveSide() + " Trainer "
                    + cap.getType().name + " " + dt + "ms ");
            v.offsetBy(EnumFacing.UP).moveEntity(t);
            if (t.pokemobsCap.countPokemon() > 0
                    && SpawnHandler.checkNoSpawnerInArea(w, (int) t.posX, (int) t.posY, (int) t.posZ))
            {
                addTrainerCoord(t);
                w.spawnEntity(t);
            }
            else t.setDead();
        }

    }

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        if (Config.instance.trainerSpawn && evt.phase == Phase.END && evt.type != Type.CLIENT && evt.side != Side.CLIENT
                && evt.world.getTotalWorldTime() % 100 == 0)
        {
            long time = System.nanoTime();
            tick(evt.world);
            double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 50) System.err
                    .println(FMLCommonHandler.instance().getEffectiveSide() + "Trainer Spawn Tick took " + dt + "ms");
        }
    }

    public EntityTrainer getTrainer(Vector3 v, World w)
    {
        TypeTrainer ttype = null;
        Material m = v.getBlockMaterial(w);
        if (m == Material.AIR && v.offset(EnumFacing.DOWN).getBlockMaterial(w) == Material.AIR)
        {
            v = v.getTopBlockPos(w).offsetBy(EnumFacing.UP);
        }
        SpawnCheck checker = new SpawnCheck(v, w);
        types:
        for (TypeTrainer type : TypeTrainer.typeMap.values())
        {
            for (Entry<SpawnBiomeMatcher, Float> entry : type.matchers.entrySet())
            {
                SpawnBiomeMatcher matcher = entry.getKey();
                Float value = entry.getValue();
                if (w.rand.nextFloat() < value && matcher.matches(checker))
                {
                    ttype = type;
                    break types;
                }
            }
        }
        if (ttype == null) return null;
        int level = SpawnHandler.getSpawnLevel(w, v, Database.getEntry(1));
        EntityTrainer trainer = new EntityTrainer(w, ttype, level);
        trainer.aiStates.setAIState(IHasNPCAIStates.MATES, true);
        trainer.aiStates.setAIState(IHasNPCAIStates.TRADES, true);
        return trainer;
    }
}

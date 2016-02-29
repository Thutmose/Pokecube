package pokecube.core.events.handlers;

import static pokecube.core.database.PokedexEntry.SpawnData.DAY;
import static pokecube.core.database.PokedexEntry.SpawnData.NIGHT;
import static pokecube.core.database.PokedexEntry.SpawnData.WATER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.nfunk.jep.JEP;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Cruncher;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou Heavily modified by Thutmose */
public final class SpawnHandler
{
    private static final Map<ChunkCoordinate, Integer> forbiddenSpawningCoords = new HashMap<ChunkCoordinate, Integer>();
    public static HashMap<Integer, String>             functions               = new HashMap<Integer, String>();
    public static HashMap<Integer, Integer[]>          subBiomeLevels          = new HashMap<Integer, Integer[]>();
    public static boolean                              doSpawns                = true;
    public static boolean                              onlySubbiomes           = false;

    static
    {
        functions.put(0, "(10^6)*(sin(x*10^-3)^8 + sin(y*10^-3)^8)");
        functions.put(1, "10+r/130;r");
        functions.put(2, "(10^6)*(sin(x*0.5*10^-3)^8 + sin(y*0.5*10^-3)^8)");
    }

    // randomized for spawning
    public static final HashMap<Integer, ArrayList<PokedexEntry>> spawns     = new HashMap<Integer, ArrayList<PokedexEntry>>();
    // not randomized
    public static final HashMap<Integer, ArrayList<PokedexEntry>> spawnLists = new HashMap<Integer, ArrayList<PokedexEntry>>();
    public static int                                             number     = 0;

    private static Vector3                                        vec        = Vector3.getNewVector();
    private static Vector3                                        vec1       = Vector3.getNewVector();
    private static Vector3                                        vec2       = Vector3.getNewVector();
    private static Vector3                                        temp       = Vector3.getNewVector();

    public SpawnHandler()
    {
        if (Mod_Pokecube_Helper.pokemonSpawn) MinecraftForge.EVENT_BUS.register(this);
    }

    public static double MAX_DENSITY = 1;
    public static int    MAXNUM      = 10;

    public static void sortSpawnables()
    {
        spawnLists.clear();

        for (int s : spawns.keySet())
        {
            ArrayList<Double> occurances = new ArrayList<Double>();
            ArrayList<Integer> numbers = new ArrayList<Integer>();
            for (PokedexEntry p : spawns.get(s))
            {
                occurances.add((double) p.getSpawnData().getWeight(s));
                numbers.add(p.getPokedexNb());
            }
            spawnLists.put(s, new ArrayList<PokedexEntry>());
            Double[] oc = occurances.toArray(new Double[0]);
            Integer[] i = numbers.toArray(new Integer[0]);
            new Cruncher().sort22(oc, i);
            for (int j = i.length; j > 0; j--)
                if (!spawnLists.get(s).contains(Database.getEntry(i[j - 1])))
                    spawnLists.get(s).add(Database.getEntry(i[j - 1]));

        }
    }

    public static void addSpawn(PokedexEntry entry)
    {
        SpawnData spawn = entry.getSpawnData();
        if (spawn == null) return;

        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
        {
            if (b == null) continue;
            ArrayList<PokedexEntry> entries = spawns.get(b.biomeID);
            if (entries == null)
            {
                entries = new ArrayList<PokedexEntry>();
                spawns.put(b.biomeID, entries);
            }
            if (spawn.isValid(b.biomeID))
            {
                entries.add(entry);
            }
        }
        for (BiomeType b : BiomeType.values())
        {
            if (b == null) continue;
            ArrayList<PokedexEntry> entries = spawns.get(b.getType());
            if (entries == null)
            {
                entries = new ArrayList<PokedexEntry>();
                spawns.put(b.getType(), entries);
            }
            if (spawn.isValid(b.getType()))
            {
                entries.add(entry);
            }
        }
    }

    public static void addSpawn(PokedexEntry entry, BiomeGenBase b)
    {
        SpawnData spawn = entry.getSpawnData();
        if (spawn == null) return;

        ArrayList<PokedexEntry> entries = spawns.get(b.biomeID);
        if (entries == null)
        {
            entries = new ArrayList<PokedexEntry>();
            spawns.put(b.biomeID, entries);
        }
        if (spawn.isValid(b.biomeID))
        {
            entries.add(entry);
        }
    }

    public static boolean addForbiddenSpawningCoord(int x, int y, int z, int dim, int range)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        if (forbiddenSpawningCoords.containsKey(coord)) return false;
        forbiddenSpawningCoords.put(coord, range);
        return true;
    }

    public static boolean removeForbiddenSpawningCoord(int x, int y, int z, int dim)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        return forbiddenSpawningCoords.remove(coord) != null;
    }

    /** Given a player, find a random position near it. */
    public static Vector3 getRandomSpawningPointNearEntity(World world, Entity player, int maxRange)
    {
        if (player == null) return null;

        Vector3 temp;
        Vector3 temp1 = vec1.set(player);

        Vector3 ret = temp1;
        temp = ret.copy();
        int rand = Math.abs(new Random().nextInt());
        int distance = maxRange;
        if (distance % 2 == 0) distance++;
        int num = distance * distance;
        for (int i = 0; i < num; i++)
        {
            for (int k = 0; k <= 20; k++)
            {
                int j = (i + rand) % num;
                int x = j % (distance) - distance / 2;
                int z = (j / distance) % (distance) - distance / 2;
                int y = 10 - world.rand.nextInt(20);
                temp.set(ret).addTo(x, y, z);
                if (temp.isClearOfBlocks(world)) { return temp; }
            }

        }

        if (temp == null) temp = Vector3.getNewVector().set(player);

        temp1 = Vector3.getNextSurfacePoint2(world, temp, vec2.set(EnumFacing.DOWN), temp.y);

        if (temp1 != null)
        {
            temp1.y++;
            return temp1;
        }
        return temp;
    }

    public static Vector3 getRandomPointNear(IBlockAccess world, Vector3 v, int distance)
    {
        Vector3 ret = v;
        Vector3 temp = ret.copy();
        int rand = Math.abs(new Random().nextInt());
        if (distance % 2 == 0) distance++;
        int num = distance * distance * distance;
        for (int i = 0; i < num; i++)
        {
            int j = (i + rand) % num;
            int x = j % (distance) - distance / 2;
            int y = (j / distance) % (distance) - distance / 2;
            int z = (j / (distance * distance)) % (distance) - distance / 2;
            y = Math.max(1, y);
            temp.set(ret).addTo(x, y, z);
            if (temp.isClearOfBlocks(world)) { return temp; }

        }
        return null;
    }

    public static boolean isPointValidForSpawn(World world, Vector3 point, PokedexEntry dbe)
    {
        int i = point.intX();
        int j = point.intY();
        int k = point.intZ();

        if (!checkNoSpawnerInArea(world, i, j, k)) { return false; }
        SpawnData entry = dbe.getSpawnData();

        if (entry.types[DAY] && !entry.types[NIGHT])
        {
            int lightBlock = world.getLightFor(EnumSkyBlock.BLOCK, point.getPos());
            int lightDay = world.getLightFor(EnumSkyBlock.SKY, point.getPos());
            if (lightBlock == 0 && world.isDaytime()) lightBlock = lightDay;
            if (lightBlock < 8) { return false; }
        }
        if (!entry.types[DAY] && entry.types[NIGHT])
        {
            int lightBlock = world.getLightFor(EnumSkyBlock.BLOCK, point.getPos());
            int lightDay = world.getLightFor(EnumSkyBlock.SKY, point.getPos());
            if (lightBlock == 0 && world.isDaytime()) lightBlock = lightDay;
            if (lightBlock > 5) { return false; }
        }

        boolean validLocation = canPokemonSpawnHere(point, world, dbe);
        return validLocation;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void spawn(World world)
    {
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL || !doSpawns) return;
        List players = new ArrayList(world.playerEntities);
        if (players.isEmpty()) return;
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++)
        {
            Vector3 v = getRandomSpawningPointNearEntity(world, (Entity) players.get(0),
                    Mod_Pokecube_Helper.mobDespawnRadius);
            if (v != null)
            {
                doSpawnForLocation(world, v);
            }
        }
    }

    private int doSpawnForLocation(World world, Vector3 v)
    {
        int ret = 0;
        if (!v.doChunksExist(world, 10)) return ret;
        AxisAlignedBB box = v.getAABB();
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                box.expand(Mod_Pokecube_Helper.mobDespawnRadius, 20, Mod_Pokecube_Helper.mobDespawnRadius));

        int num = 0;
        boolean player = false;
        for (Object o : list)
        {
            if (o instanceof IPokemob) num++;
            if (o instanceof EntityPlayer) player = true;
        }

        if (num > MAX_DENSITY * MAXNUM || !player) return ret;

        if (v.y < 0 || !checkNoSpawnerInArea(world, v.intX(), v.intY(), v.intZ())) return ret;

        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, v);
        int b = t.getBiome(v);
        if (onlySubbiomes && b <= BiomeGenBase.getBiomeGenArray().length) { return ret; }

        if (b == BiomeType.CAVE.getType())
        {
            t.checkIndustrial(world);
            b = t.getBiome(v);
        }

        if (spawns.containsKey(b))
        {
            ArrayList<PokedexEntry> entries = spawns.get(b);
            if (entries.isEmpty())
            {
                spawns.remove(b);
                return ret;
            }
            Collections.shuffle(entries);
            int index = 0;
            if (index >= entries.size()) return ret;
            PokedexEntry dbe = entries.get(index);
            float weight = dbe.getSpawnData().getWeight(b);
            if (b > 256) weight *= 10;
            if (Math.random() > weight) return ret;

            if (!dbe.flys())
            {
                v = Vector3.getNextSurfacePoint2(world, v, v1.set(0, -1, 0), v.y);
            }
            if (v == null) { return ret; }

            Vector3.movePointOutOfBlocks(v, world);

            if (!canSpawn(t, dbe.getSpawnData(), v, world)) return ret;

            if (dbe.getSpawnData().types[SpawnData.LEGENDARY])
            {
                int exp = getSpawnXp(world, v, dbe);
                int level = Tools.xpToLevel(dbe.getEvolutionMode(), exp);
                if (level < Mod_Pokecube_Helper.minLegendLevel) { return ret; }
            }

            if (!isPointValidForSpawn(world, v, dbe)) return ret;

            num = 0;
            if (dbe.getSpawnData() == null)
            {
                System.err.println("Error with Spawn registration for " + dbe.getName());
            }
            else
            {
                long time = System.nanoTime();

                ret += num = doSpawnForType(world, v, dbe, parser, t);

                double dt = (System.nanoTime() - time) / 1000000D;
                if (dt > 50)
                {
                    if (dt > maxtime)
                    {
                        maxtime = dt;
                        functions.put(-2, dbe.toString());
                    }
                    System.err.println(t.getCentre() + " " + dt + " " + dbe + " " + num + " Maximum was "
                            + functions.get(-2) + " at " + maxtime);
                }
            }
        }
        return ret;
    }

    static double maxtime = 0;

    private int doSpawnForType(World world, Vector3 loc, PokedexEntry dbe, JEP parser, TerrainSegment t)
    {
        SpawnData entry = dbe.getSpawnData();

        int totalSpawnCount = 0;
        Vector3 offset = v1.clear();
        Vector3 point = v2.clear();

        int biome = t.getBiome(loc);
        byte distGroupZone = 6;
        Random rand = new Random();

        int n = Math.max(entry.getMax(biome) - entry.getMin(biome), 1);
        int spawnNumber = entry.getMin(biome) + rand.nextInt(n);

        for (int i = 0; i < spawnNumber; i++)
        {
            offset.set(rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone), rand.nextInt(1) - rand.nextInt(1),
                    rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone));
            v.set(loc);
            point.set(v.addTo(offset));

            if (!isPointValidForSpawn(world, point, dbe)) continue;

            float x = (float) point.x + 0.5F;
            float y = (float) point.y;
            float z = (float) point.z + 0.5F;

            boolean playerNearCheck = world.getClosestPlayer(x, y, z, Mod_Pokecube_Helper.mobSpawnRadius) == null;
            if (!playerNearCheck) continue;

            float var28 = x - world.getSpawnPoint().getX();
            float var29 = y - world.getSpawnPoint().getY();
            float var30 = z - world.getSpawnPoint().getZ();
            float distFromSpawnPoint = var28 * var28 + var29 * var29 + var30 * var30;

            if (!checkNoSpawnerInArea(world, (int) x, (int) y, (int) z)) continue;

            if (distFromSpawnPoint >= 576.0F)
            {
                EntityLiving entityliving = null;
                try
                {

                    if (dbe.getPokedexNb() > 0)
                    {
                        entityliving = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(dbe.getPokedexNb(),
                                world);
                        entityliving.setHealth(entityliving.getMaxHealth());
                        entityliving.setLocationAndAngles((double) x + 0.5F, (double) y + 0.5F, (double) z + 0.5F,
                                world.rand.nextFloat() * 360.0F, 0.0F);

                        time = System.nanoTime();
                        if (entityliving.getCanSpawnHere())
                        {
                            if (creatureSpecificInit(entityliving, world, x, y, z, v3.set(entityliving)))
                            {
                                SpawnEvent.Post evt = new SpawnEvent.Post(dbe, v3, world, (IPokemob) entityliving);
                                MinecraftForge.EVENT_BUS.post(evt);
                                world.spawnEntityInWorld(entityliving);
                                totalSpawnCount++;
                            }
                            else
                            {
                                entityliving.setDead();
                            }
                        }
                        else
                        {
                            entityliving.setDead();
                        }
                    }
                }
                catch (Throwable e)
                {
                    if (entityliving != null)
                    {
                        entityliving.setDead();
                    }

                    System.err.println("Wrong Id while spawn: " + dbe.getName());
                    e.printStackTrace();

                    return totalSpawnCount;
                }
            }

        }
        return totalSpawnCount;
    }

    /** Checks there's no spawner in the area
     * 
     * @param world
     * @param chunkPosX
     * @param chunkPosY
     * @param chunkPosZ
     * @return */
    public static boolean checkNoSpawnerInArea(World world, int chunkPosX, int chunkPosY, int chunkPosZ)
    {
        ArrayList<ChunkCoordinate> coords = new ArrayList<ChunkCoordinate>(forbiddenSpawningCoords.keySet());

        for (ChunkCoordinate coord : coords)
        {
            int tolerance = forbiddenSpawningCoords.get(coord);
            if (chunkPosX >= coord.getX() - tolerance && chunkPosZ >= coord.getZ() - tolerance
                    && chunkPosY >= coord.getY() - tolerance && chunkPosY <= coord.getY() + tolerance
                    && chunkPosX <= coord.getX() + tolerance && chunkPosZ <= coord.getZ() + tolerance
                    && world.provider.getDimensionId() == coord.dim) { return false; }
        }
        return true;
    }

    public static boolean canPokemonSpawnHere(Vector3 location, World worldObj, PokedexEntry entry)
    {
        if (!location.clearOfBlocks(worldObj)) return false;
        if (!temp.set(location).addTo(0, entry.height, 0).clearOfBlocks(worldObj)) return false;
        if (!temp.set(location).addTo(entry.width / 2, 0, 0).clearOfBlocks(worldObj)) return false;
        if (!temp.set(location).addTo(0, 0, entry.width / 2).clearOfBlocks(worldObj)) return false;
        if (!temp.set(location).addTo(0, 0, -entry.width / 2).clearOfBlocks(worldObj)) return false;
        if (!temp.set(location).addTo(-entry.width / 2, 0, 0).clearOfBlocks(worldObj)) return false;

        SpawnEvent.Pre evt = new SpawnEvent.Pre(entry, location, worldObj);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled()) return false;

        SpawnData dat = entry.getSpawnData();
        boolean water = dat == null ? false : dat.types[WATER];
        Material here = location.getBlockMaterial(worldObj);
        Material up = temp.set(location).addTo(0, entry.height, 0).getBlockMaterial(worldObj);
        boolean inAir = entry.mobType == PokecubeMod.Type.FLOATING || entry.mobType == PokecubeMod.Type.FLYING;

        if (water) { return location.getBlockMaterial(worldObj) == Material.water && (!up.blocksMovement()); }
        if (inAir && !temp.set(location).addTo(0, -1, 0).isSideSolid(worldObj,
                EnumFacing.UP)) { return !here.blocksMovement() && !here.isLiquid() && !up.blocksMovement()
                        && !up.isLiquid(); }

        if (!temp.set(location).addTo(0, -1, 0).isSideSolid(worldObj, EnumFacing.UP)) return false;

        Block down = temp.set(location).addTo(0, -1, 0).getBlock(worldObj);

        boolean validMaterial = !here.blocksMovement() && !here.isLiquid() && !up.blocksMovement() && !up.isLiquid();

        if (!validMaterial) return false;

        return down.canCreatureSpawn(worldObj, temp.getPos(),
                net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND);// validSurfaces.contains(down);
    }

    public static boolean lvlCap   = false;
    public static int     capLevel = 50;

    public static boolean creatureSpecificInit(EntityLiving entityliving, World world, double posX, double posY,
            double posZ, Vector3 spawnPoint)
    {
        if (ForgeEventFactory.doSpecialSpawn(entityliving, world, (float) posX, (float) posY,
                (float) posZ)) { return false; }

        if (entityliving instanceof IPokemob)
        {
            int maxXP = getSpawnXp(world, vec.set(entityliving), ((IPokemob) entityliving).getPokedexEntry());

            if (lvlCap) maxXP = Math.min(maxXP,
                    Tools.levelToXp(((IPokemob) entityliving).getPokedexEntry().getEvolutionMode(), capLevel));
            maxXP = Math.max(10, maxXP);

            ((IPokemob) entityliving).setExp(maxXP, false, true);
            ((IPokemob) entityliving).levelUp(((IPokemob) entityliving).getLevel());
            ((IPokemob) entityliving).specificSpawnInit();

            return true;
        }
        return false;
    }

    public static final HashMap<Integer, JEP> parsers = new HashMap<Integer, JEP>();

    public static int getSpawnXp(World world, Vector3 location, PokedexEntry pokemon)
    {
        int maxXp = 10;

        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        int b = t.getBiome(location);
        if (subBiomeLevels.containsKey(b))
        {
            Integer[] range = subBiomeLevels.get(b);
            int dl = range[1] - range[0];
            if (dl > 0) dl = new Random().nextInt(dl) + 1;
            int level = range[0] + dl;
            maxXp = Math.max(10, Tools.levelToXp(pokemon.getEvolutionMode(), level));
            return maxXp;
        }

        Vector3 spawn = temp.set(world.getSpawnPoint());
        JEP toUse;
        int type = world.getWorldType().getWorldTypeID();
        boolean isNew = false;
        String function = "";
        if (functions.containsKey(type))
        {
            function = functions.get(type);
        }
        else
        {
            function = functions.get(0);
        }
        if (parsers.containsKey(type))
        {
            toUse = (JEP) parsers.get(type);
        }
        else
        {
            parsers.put(type, new JEP());
            toUse = (JEP) parsers.get(type);
            isNew = true;
        }
        if (Double.isNaN(toUse.getValue()))
        {
            toUse = new JEP();
            parsers.put(type, toUse);
            isNew = true;
        }

        boolean r = function.split(";").length == 2;
        if (!r)
        {
            parseExpression(toUse, function, location.x - spawn.x, location.z - spawn.z, r, isNew);
        }
        else
        {
            double d = location.distToSq(spawn);
            parseExpression(toUse, function.split(";")[0], d, location.y, r, isNew);
        }
        maxXp = (int) Math.abs(toUse.getValue());
        maxXp = Math.max(maxXp, 10);
        maxXp = new Random().nextInt(maxXp);
        maxXp = Math.max(maxXp, 10);
        return maxXp;
    }

    private static void parseExpression(JEP parser, String toParse, double xValue, double yValue, boolean r,
            boolean isNew)
    {
        if (isNew)
        {
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex(); // among other things adds i to the symbol
                                 // table
            if (!r)
            {
                parser.addVariable("x", xValue);
                parser.addVariable("y", yValue);
            }
            else
            {
                parser.addVariable("r", xValue);
            }
            parser.parseExpression(toParse);
        }
        else
        {
            if (!r)
            {
                parser.setVarValue("x", xValue);
                parser.setVarValue("y", yValue);
            }
            else
            {
                parser.setVarValue("r", xValue);
            }
        }
    }

    public static long time   = 0;
    public JEP         parser = new JEP();
    Vector3            v      = Vector3.getNewVector();
    Vector3            v1     = Vector3.getNewVector();
    Vector3            v2     = Vector3.getNewVector();
    Vector3            v3     = Vector3.getNewVector();

    public void tick(World world)
    {
        if (PokecubeCore.isOnClientSide())
        {
            System.out.println(FMLCommonHandler.instance().getEffectiveSide());
            return;
        }
        try
        {
            spawn(world);
            if (Mod_Pokecube_Helper.meteors)
            {
                if (!world.provider.isSurfaceWorld()) return;
                if (world.provider.getHasNoSky()) return;

                List<Object> players = new ArrayList<Object>(world.playerEntities);
                if (players.size() < 1) return;
                Collections.shuffle(players);

                if (Math.random() > 0.999)
                {
                    Entity player = (Entity) players.get(0);
                    Random rand = new Random();
                    int dx = rand.nextInt(200) - 100;
                    int dz = rand.nextInt(200) - 100;

                    Vector3 v = this.v.set(player).add(dx, 0, dz);

                    if (world.getClosestPlayer(v.x, v.y, v.z, 64) != null) return;

                    v.add(0, 255 - player.posY, 0);

                    if (PokecubeSerializer.getInstance().canMeteorLand(v))
                    {
                        Vector3 direction = v1.set(rand.nextGaussian() / 2, -1, rand.nextGaussian() / 2);
                        Vector3 location = Vector3.getNextSurfacePoint(world, v, direction, 255);

                        if (location != null)
                        {
                            float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
                            ExplosionCustom boom = new ExplosionCustom(world, PokecubeMod.getFakePlayer(world),
                                    location, energy).setMeteor(true);
                            String message = "Meteor at " + v + " with Direction of " + direction + " and energy of "
                                    + energy;
                            System.out.println(message);
                            boom.doExplosion();
                            PokecubeSerializer.getInstance().addMeteorLocation(v);

                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadFunctionsFromStrings(String[] args)
    {
        for (String s : args)
        {
            loadFunctionFromString(s);
        }
    }

    public static void loadFunctionFromString(String args)
    {
        String[] strings = args.split(":");
        if (strings.length == 0) return;
        int id = Integer.parseInt(strings[0]);
        functions.put(id, strings[1]);
    }

    public static boolean addForbiddenSpawningCoord(BlockPos pos, int dimensionId, int distance)
    {
        return addForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId, distance);
    }

    public static boolean removeForbiddenSpawningCoord(BlockPos pos, int dimensionId)
    {
        return removeForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId);
    }

    public static boolean canSpawn(TerrainSegment terrain, SpawnData data, Vector3 v, World world)
    {
        int biome2 = terrain.getBiome(v.intX(), v.intY(), v.intZ());

        if (data == null) return false;

        if (biome2 == PokecubeTerrainChecker.INSIDE.getType())
        {
            biome2 = BiomeType.VILLAGE.getType();
        }

        if ((data.getWeight(BiomeType.ALL.getType())) > 0) return true;

        int b = biome2;
        int b1 = v.getBiomeID(world);

        boolean ret = data.isValid(b1, b);

        return ret;
    }
}

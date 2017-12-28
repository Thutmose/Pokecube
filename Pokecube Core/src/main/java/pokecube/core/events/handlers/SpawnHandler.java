package pokecube.core.events.handlers;

import static thut.api.terrain.TerrainSegment.GRIDSIZE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.MakeCommand;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.boom.ExplosionCustom;
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
    public static boolean                              refreshSubbiomes        = false;
    public static HashSet<Integer>                     dimensionBlacklist      = Sets.newHashSet();
    public static HashSet<Integer>                     dimensionWhitelist      = Sets.newHashSet();
    public static Predicate<Integer>                   biomeToRefresh          = new Predicate<Integer>()
                                                                               {
                                                                                   @Override
                                                                                   public boolean apply(Integer input)
                                                                                   {
                                                                                       if (input == -1) return false;
                                                                                       if (input < 256
                                                                                               || refreshSubbiomes)
                                                                                           return true;
                                                                                       return input == BiomeType.SKY
                                                                                               .getType()
                                                                                               || input == BiomeType.CAVE
                                                                                                       .getType()
                                                                                               || input == BiomeType.CAVE_WATER
                                                                                                       .getType()
                                                                                               || input == BiomeType.VILLAGE
                                                                                                       .getType()
                                                                                               || input == BiomeType.ALL
                                                                                                       .getType()
                                                                                               || input == PokecubeTerrainChecker.INSIDE
                                                                                                       .getType()
                                                                                               || input == BiomeType.NONE
                                                                                                       .getType();
                                                                                   }
                                                                               };

    static
    {
        functions.put(-1, "(50)*(sin(x*8*10^-3)^8 + sin(y*8*10^-3)^8)");
        functions.put(0, "(50)*(sin(x*10^-3)^8 + sin(y*10^-3)^8)");
        functions.put(1, "10+r/130;r");
        functions.put(2, "(50)*(sin(x*0.5*10^-3)^8 + sin(y*0.5*10^-3)^8)");
    }

    private static Vector3                    vec1        = Vector3.getNewVector();
    private static Vector3                    vec2        = Vector3.getNewVector();
    private static Vector3                    temp        = Vector3.getNewVector();
    public static double                      MAX_DENSITY = 1;
    public static int                         MAXNUM      = 10;
    public static boolean                     lvlCap      = false;
    public static boolean                     expFunction = false;
    public static int                         capLevel    = 50;
    public static final HashMap<Integer, JEP> parsers     = new HashMap<Integer, JEP>();

    public static boolean addForbiddenSpawningCoord(BlockPos pos, int dimensionId, int distance)
    {
        return addForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId, distance);
    }

    public static boolean addForbiddenSpawningCoord(int x, int y, int z, int dim, int range)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        if (forbiddenSpawningCoords.containsKey(coord)) return false;
        forbiddenSpawningCoords.put(coord, range);
        return true;
    }

    public static void clear()
    {
        forbiddenSpawningCoords.clear();
    }

    public static boolean canPokemonSpawnHere(Vector3 location, World world, PokedexEntry entry)
    {
        if (!location.clearOfBlocks(world) || !canSpawn(null, entry.getSpawnData(), location, world, true))
            return false;
        if (!temp.set(location).addTo(0, entry.height, 0).clearOfBlocks(world)) return false;
        if (!temp.set(location).addTo(entry.width / 2, 0, 0).clearOfBlocks(world)) return false;
        if (!temp.set(location).addTo(0, 0, entry.width / 2).clearOfBlocks(world)) return false;
        if (!temp.set(location).addTo(0, 0, -entry.width / 2).clearOfBlocks(world)) return false;
        if (!temp.set(location).addTo(-entry.width / 2, 0, 0).clearOfBlocks(world)) return false;
        IBlockState state = temp.set(location).addTo(0, -1, 0).getBlockState(world);
        Block down = state.getBlock();
        net.minecraft.entity.EntityLiving.SpawnPlacementType type = SpawnPlacementType.ON_GROUND;
        if (entry.flys())
        {
            type = SpawnPlacementType.IN_AIR;
            if (down.canCreatureSpawn(state, world, temp.getPos(), type) || location.isAir(world)) return true;
        }
        if (entry.swims())
        {
            type = SpawnPlacementType.IN_WATER;
            if (down.canCreatureSpawn(state, world, temp.getPos(), type) || state.getMaterial() == Material.WATER)
                return true;
        }
        return down.canCreatureSpawn(state, world, temp.getPos(), type);
    }

    public static boolean canSpawn(TerrainSegment terrain, SpawnData data, Vector3 v, World world,
            boolean respectDensity)
    {
        if (data == null) return false;
        if (respectDensity)
        {
            int count = Tools.countPokemon(world, v, PokecubeMod.core.getConfig().maxSpawnRadius);
            if (count > PokecubeMod.core.getConfig().mobSpawnNumber * PokecubeMod.core.getConfig().mobDensityMultiplier)
                return false;
        }
        return data.isValid(world, v);
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
                    && world.provider.getDimension() == coord.dim) { return false; }
        }
        return true;
    }

    public static EntityLiving creatureSpecificInit(EntityLiving entityliving, World world, double posX, double posY,
            double posZ, Vector3 spawnPoint, int overrideLevel, int variance)
    {
        if (ForgeEventFactory.doSpecialSpawn(entityliving, world, (float) posX, (float) posY,
                (float) posZ)) { return null; }
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityliving);
        if (pokemob != null)
        {

            long time = System.nanoTime();
            int maxXP = 10;
            int level = 1;
            if (expFunction && overrideLevel == -1)
            {
                maxXP = getSpawnXp(world, spawnPoint, pokemob.getPokedexEntry(), variance, overrideLevel);
                SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint, world,
                        Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), maxXP), variance);
                MinecraftForge.EVENT_BUS.post(event);
                level = event.getLevel();
            }
            else if (overrideLevel == -1)
            {
                level = getSpawnLevel(world, Vector3.getNewVector().set(posX, posY, posZ), pokemob.getPokedexEntry(),
                        variance, overrideLevel);
            }
            else
            {
                SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), spawnPoint, world,
                        overrideLevel, variance);
                MinecraftForge.EVENT_BUS.post(event);
                level = event.getLevel();
            }
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
            pokemob.getEntity().getEntityData().setInteger("spawnExp", maxXP);
            pokemob = pokemob.specificSpawnInit();
            double dt = (System.nanoTime() - time) / 10e3D;
            if (PokecubeMod.debug && dt > 100)
            {
                temp.set(temp.set(posX, posY, posZ).getPos());
                String toLog = "location: %1$s took: %2$s\u00B5s to spawn Init for %3$s";
                PokecubeMod.log(String.format(toLog, temp, dt, pokemob.getPokemonDisplayName().getFormattedText()));
            }
            return pokemob.getEntity();
        }
        return null;
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

    /** Given a player, find a random position near it. */
    public static Vector3 getRandomSpawningPointNearEntity(World world, Entity player, int maxRange, int maxTries)
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
        if (maxTries > 0) num = Math.min(num, maxTries);
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

        double maxTempY = temp.y - player.posY - 10;
        if (maxTempY <= 0) return null;

        temp1 = Vector3.getNextSurfacePoint2(world, temp, vec2.set(EnumFacing.DOWN), maxTempY);

        if (temp1 != null)
        {
            temp1.y++;
            return temp1;
        }
        return temp;
    }

    private static int parse(World world, Vector3 location)
    {
        Vector3 spawn = temp.set(world.getSpawnPoint());
        if (!PokecubeMod.core.getConfig().spawnCentered) spawn.clear();
        JEP toUse;
        int type = world.provider.getDimension();
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
            toUse = parsers.get(type);
        }
        else
        {
            parsers.put(type, new JEP());
            toUse = parsers.get(type);
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
        return (int) Math.abs(toUse.getValue());
    }

    public static int getSpawnLevel(World world, Vector3 location, PokedexEntry pokemon, int variance, int baseLevel)
    {
        int spawnLevel = baseLevel;
        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        int b = t.getBiome(location);
        if (variance == -1)
        {
            if (subBiomeLevels.containsKey(b))
            {
                Integer[] range = subBiomeLevels.get(b);
                variance = range[1] - range[0];
            }
            else
            {
                variance = PokecubeMod.core.getConfig().levelVariance;
            }
        }
        if (spawnLevel == -1)
        {
            if (subBiomeLevels.containsKey(b))
            {
                Integer[] range = subBiomeLevels.get(b);
                spawnLevel = range[0];
            }
            else
            {
                spawnLevel = parse(world, location);
            }
        }
        if (variance < 1) variance = 1;
        spawnLevel = spawnLevel + world.rand.nextInt(variance);
        SpawnEvent.Level event = new SpawnEvent.Level(pokemon, location, world, spawnLevel, variance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getLevel();
    }

    public static int getSpawnLevel(World world, Vector3 location, PokedexEntry pokemon)
    {
        return getSpawnLevel(world, location, pokemon, -1, -1);
    }

    public static int getSpawnXp(World world, Vector3 location, PokedexEntry pokemon, int variance, int baseLevel)
    {
        int maxXp = 10;
        if (!expFunction) { return Tools.levelToXp(pokemon.getEvolutionMode(),
                getSpawnLevel(world, location, pokemon, variance, baseLevel)); }

        // TODO properly implement base level and variance overriding

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
        maxXp = parse(world, location);
        maxXp = Math.max(maxXp, 10);
        int level = Tools.xpToLevel(pokemon.getEvolutionMode(), maxXp);
        variance = variance == -1 ? PokecubeMod.core.getConfig().levelVariance : variance;
        level = level + new Random().nextInt(Math.max(1, variance));
        level = Math.max(1, level);
        return Tools.levelToXp(pokemon.getEvolutionMode(), level);
    }

    public static int getSpawnXp(World world, Vector3 location, PokedexEntry pokemon)
    {
        int maxXp = 10;
        if (!expFunction) { return Tools.levelToXp(pokemon.getEvolutionMode(),
                getSpawnLevel(world, location, pokemon)); }
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
        maxXp = parse(world, location);
        maxXp = Math.max(maxXp, 10);
        int level = Tools.xpToLevel(pokemon.getEvolutionMode(), maxXp);
        int variance = PokecubeMod.core.getConfig().levelVariance;
        level = level + new Random().nextInt(Math.max(1, variance));
        level = Math.max(1, level);
        return Tools.levelToXp(pokemon.getEvolutionMode(), level);
    }

    public static boolean isPointValidForSpawn(World world, Vector3 point, PokedexEntry dbe)
    {
        int i = point.intX();
        int j = point.intY();
        int k = point.intZ();
        if (!checkNoSpawnerInArea(world, i, j, k)) { return false; }
        boolean validLocation = canPokemonSpawnHere(point, world, dbe);
        return validLocation;
    }

    public static void loadFunctionFromString(String args)
    {
        String[] strings = args.split(":");
        if (strings.length == 0) return;
        int id = Integer.parseInt(strings[0]);
        functions.put(id, strings[1]);
    }

    public static void loadFunctionsFromStrings(String[] args)
    {
        for (String s : args)
        {
            loadFunctionFromString(s);
        }
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

    public static boolean removeForbiddenSpawningCoord(BlockPos pos, int dimensionId)
    {
        return removeForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), dimensionId);
    }

    public static boolean removeForbiddenSpawningCoord(int x, int y, int z, int dim)
    {
        ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        return forbiddenSpawningCoords.remove(coord) != null;
    }

    public JEP parser = new JEP();
    Vector3    v      = Vector3.getNewVector();
    Vector3    v1     = Vector3.getNewVector();
    Vector3    v2     = Vector3.getNewVector();

    Vector3    v3     = Vector3.getNewVector();

    public SpawnHandler()
    {
        if (PokecubeMod.core.getConfig().pokemonSpawn) MinecraftForge.EVENT_BUS.register(this);
    }

    private int doSpawnForLocation(World world, Vector3 v)
    {
        int ret = 0;
        if (!v.doChunksExist(world, 32)) return ret;
        int radius = PokecubeMod.core.getConfig().maxSpawnRadius;
        int num = 0;
        int height = v.getMaxY(world);
        AxisAlignedBB box = v.getAABB();
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                box.grow(radius, Math.max(height, radius), radius));
        for (EntityLivingBase o : list)
        {
            if (CapabilityPokemob.getPokemobFor(o) != null) num++;
        }
        boolean player = Tools.isAnyPlayerInRange(radius, 10, world, v);
        if (num > MAX_DENSITY * MAXNUM || !player) return ret;
        if (v.y < 0 || !checkNoSpawnerInArea(world, v.intX(), v.intY(), v.intZ())) return ret;
        refreshTerrain(v, world);
        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, v);
        int b = t.getBiome(v);
        if (onlySubbiomes && b <= 255) { return ret; }
        long time = System.nanoTime();
        SpawnEvent.Pick event = new SpawnEvent.Pick.Pre(null, v, world);
        MinecraftForge.EVENT_BUS.post(event);
        PokedexEntry dbe = event.getPicked();
        if (dbe == null) return ret;
        event = new SpawnEvent.Pick.Post(dbe, v, world);
        MinecraftForge.EVENT_BUS.post(event);
        dbe = event.getPicked();
        v = event.getLocation();
        if (v == null) { return ret; }
        if (!isPointValidForSpawn(world, v, dbe)) return ret;
        double dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            temp.set(v.getPos());
            String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn and location";
            PokecubeMod.log(String.format(toLog, temp, dt));
        }
        num = 0;
        time = System.nanoTime();
        ret += num = doSpawnForType(world, v, dbe, parser, t);
        dt = (System.nanoTime() - time) / 10e3D;
        if (PokecubeMod.debug && dt > 500)
        {
            temp.set(v.getPos());
            String toLog = "location: %1$s took: %2$s\u00B5s to find a valid spawn for %3$s %4$s";
            PokecubeMod.log(String.format(toLog, temp, dt, num, dbe));
        }
        return ret;
    }

    private int doSpawnForType(World world, Vector3 loc, PokedexEntry dbe, JEP parser, TerrainSegment t)
    {
        SpawnData entry = dbe.getSpawnData();

        int totalSpawnCount = 0;
        Vector3 offset = v1.clear();
        Vector3 point = v2.clear();
        SpawnBiomeMatcher matcher = entry.getMatcher(world, loc);
        byte distGroupZone = 6;
        Random rand = new Random();

        int n = Math.max(entry.getMax(matcher) - entry.getMin(matcher), 1);
        int spawnNumber = entry.getMin(matcher) + rand.nextInt(n);

        for (int i = 0; i < spawnNumber; i++)
        {
            offset.set(rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone), rand.nextInt(1) - rand.nextInt(1),
                    rand.nextInt(distGroupZone) - rand.nextInt(distGroupZone));
            v.set(loc);
            point.set(v.addTo(offset));
            if (!isPointValidForSpawn(world, point, dbe))
            {
                continue;
            }

            float x = (float) point.x + 0.5F;
            float y = (float) point.y;
            float z = (float) point.z + 0.5F;

            float var28 = x - world.getSpawnPoint().getX();
            float var29 = y - world.getSpawnPoint().getY();
            float var30 = z - world.getSpawnPoint().getZ();
            float distFromSpawnPoint = var28 * var28 + var29 * var29 + var30 * var30;

            if (!checkNoSpawnerInArea(world, (int) x, (int) y, (int) z)) continue;
            float dist = PokecubeMod.core.getConfig().minSpawnRadius;
            boolean player = Tools.isAnyPlayerInRange(dist, dist, world, point);
            if (player) continue;
            if (distFromSpawnPoint >= 256.0F)
            {
                EntityLiving entityliving = null;
                try
                {
                    if (dbe.getPokedexNb() > 0)
                    {
                        SpawnEvent.Pick.Final event = new SpawnEvent.Pick.Final(dbe, point, world);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.getPicked() == null) continue;
                        entityliving = (EntityLiving) PokecubeMod.core.createPokemob(event.getPicked(), world);
                        entityliving.setHealth(entityliving.getMaxHealth());
                        entityliving.setLocationAndAngles((double) x + 0.5F, (double) y + 0.5F, (double) z + 0.5F,
                                world.rand.nextFloat() * 360.0F, 0.0F);
                        if (entityliving.getCanSpawnHere())
                        {
                            if ((entityliving = creatureSpecificInit(entityliving, world, x, y, z, v3.set(entityliving),
                                    entry.getLevel(matcher), entry.getVariance(matcher))) != null)
                            {
                                IPokemob pokemob = CapabilityPokemob.getPokemobFor(entityliving);
                                if (!event.getSpawnArgs().isEmpty())
                                {
                                    String[] args = event.getSpawnArgs().split(" ");
                                    MakeCommand.setToArgs(args, pokemob, 0, v, false);
                                }
                                else if (matcher.spawnRule.values.containsKey(SpawnBiomeMatcher.SPAWNCOMMAND))
                                {
                                    String[] args = matcher.spawnRule.values.get(SpawnBiomeMatcher.SPAWNCOMMAND)
                                            .split(" ");
                                    MakeCommand.setToArgs(args, pokemob, 0, v, false);
                                }
                                SpawnEvent.Post evt = new SpawnEvent.Post(dbe, v3, world, pokemob);
                                MinecraftForge.EVENT_BUS.post(evt);
                                world.spawnEntity(entityliving);
                                totalSpawnCount++;
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

    public void spawn(World world)
    {
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL || !doSpawns) return;
        List<EntityPlayer> players = Lists.newArrayList(world.playerEntities);
        if (players.isEmpty()) return;
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++)
        {
            if (players.get(i).dimension != world.provider.getDimension()) continue;
            Vector3 v = getRandomSpawningPointNearEntity(world, players.get(i),
                    PokecubeMod.core.getConfig().maxSpawnRadius, 0);
            if (v == null) continue;
            AxisAlignedBB box = v.getAABB();
            int radius = PokecubeMod.core.getConfig().maxSpawnRadius;
            int height = v.getMaxY(world);
            List<EntityPokemobBase> list = world.getEntitiesWithinAABB(EntityPokemobBase.class,
                    box.grow(radius, Math.max(height, radius), radius));
            if (list.size() < MAXNUM * MAX_DENSITY)
            {
                long time = System.nanoTime();
                int num = doSpawnForLocation(world, v);
                double dt = (System.nanoTime() - time) / 10e3D;
                if (PokecubeMod.debug && dt > 1000)
                {
                    PokecubeMod.log(dt + "\u00B5" + "s for player " + players.get(0).getDisplayNameString() + " at " + v
                            + ", spawned " + num);
                }
            }
        }
    }

    public static void refreshTerrain(Vector3 location, World world)
    {
        if (!PokecubeCore.core.getConfig().autoDetectSubbiomes) return;
        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        Vector3 temp1 = Vector3.getNewVector();
        int x0 = t.chunkX * 16, y0 = t.chunkY * 16, z0 = t.chunkZ * 16;
        int dx = GRIDSIZE / 2;
        int dy = GRIDSIZE / 2;
        int dz = GRIDSIZE / 2;
        int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        // outer:
        for (int i = x1; i < x1 + 16; i += GRIDSIZE)
            for (int j = y1; j < y1 + 16; j += GRIDSIZE)
                for (int k = z1; k < z1 + 16; k += GRIDSIZE)
                {
                    temp1.set(i, j, k);
                    int biome = t.getBiome(i, j, k);
                    if (biomeToRefresh.apply(biome) || refreshSubbiomes)
                    {
                        biome = t.adjustedCaveBiome(world, temp1);
                        if (biome == -1) biome = t.adjustedNonCaveBiome(world, temp1);
                        t.setBiome(i, j, k, biome);
                    }
                }
    }

    public void tick(World world)
    {
        if (dimensionBlacklist.contains(world.provider.getDimension())) return;
        if (PokecubeMod.core.getConfig().whiteListEnabled
                && !SpawnHandler.dimensionWhitelist.contains(world.provider.getDimension()))
            return;
        if (world.provider instanceof WorldProviderSecretBase) return;
        try
        {
            spawn(world);
            if (PokecubeMod.core.getConfig().meteors)
            {
                if (!world.provider.isSurfaceWorld()) return;
                if (world.provider.isNether()) return;

                List<Object> players = new ArrayList<Object>(world.playerEntities);
                if (players.size() < 1) return;
                if (Math.random() > 0.999)
                {
                    Random rand = new Random();
                    Entity player = (Entity) players.get(rand.nextInt(players.size()));
                    int dx = rand.nextInt(200) - 100;
                    int dz = rand.nextInt(200) - 100;

                    Vector3 v = this.v.set(player).add(dx, 0, dz);
                    if (world.getClosestPlayer(v.x, v.y, v.z, 96, false) != null) return;

                    v.add(0, 255 - player.posY, 0);

                    if (PokecubeSerializer.getInstance().canMeteorLand(v))
                    {
                        Vector3 direction = v1.set(rand.nextGaussian() / 2, -1, rand.nextGaussian() / 2);
                        Vector3 location = Vector3.getNextSurfacePoint(world, v, direction, 255);

                        if (location != null)
                        {
                            float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
                            ExplosionCustom boom = new ExplosionCustom(world, null, location, energy).setMeteor(true);
                            String message = "Meteor at " + v + " with Direction of " + direction + " and energy of "
                                    + energy;
                            if (PokecubeMod.debug) PokecubeMod.log(message);
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
}

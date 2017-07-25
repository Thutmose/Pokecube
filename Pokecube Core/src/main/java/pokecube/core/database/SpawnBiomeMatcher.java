package pokecube.core.database;

import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatWrapper;

public class SpawnBiomeMatcher
{
    public static final QName BIOMES          = new QName("biomes");
    public static final QName TYPES           = new QName("types");
    public static final QName BIOMESBLACKLIST = new QName("biomesBlacklist");
    public static final QName TYPESBLACKLIST  = new QName("typesBlacklist");
    public static final QName NIGHT           = new QName("night");
    public static final QName DAY             = new QName("day");
    public static final QName AIR             = new QName("air");
    public static final QName WATER           = new QName("water");
    public static final QName MINLIGHT        = new QName("minLight");
    public static final QName MAXLIGHT        = new QName("maxLight");

    public static class SpawnCheck
    {
        final boolean   day;
        final Material  material;
        final float     light;
        final Biome     biome;
        final BiomeType type;
        final World     world;
        final Vector3   location;

        public SpawnCheck(Vector3 location, World world)
        {
            this.world = world;
            this.location = location;
            day = world.isDaytime();
            material = location.getBlockMaterial(world);
            int lightBlock = world.getLightFor(EnumSkyBlock.BLOCK, location.getPos());
            int lightDay = world.getLightFor(EnumSkyBlock.SKY, location.getPos());
            if (lightBlock == 0 && world.isDaytime()) lightBlock = lightDay;
            light = lightBlock / 15f;
            biome = location.getBiome(world);
            TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
            int subBiomeId = t.getBiome(location);
            if (subBiomeId > 255) type = BiomeType.getType(subBiomeId);
            else type = null;
        }
    }

    public Set<Biome>      validBiomes        = Sets.newHashSet();
    public Set<BiomeType>  validSubBiomes     = Sets.newHashSet();
    public Set<Biome>      blackListBiomes    = Sets.newHashSet();
    public Set<BiomeType>  blackListSubBiomes = Sets.newHashSet();

    float                  minLight           = 0;
    float                  maxLight           = 1;
    boolean                day                = true;
    boolean                night              = true;
    boolean                air                = true;
    boolean                water              = false;

    public final SpawnRule spawnRule;

    boolean                parsed             = false;
    boolean                valid              = true;

    public SpawnBiomeMatcher(SpawnRule rules)
    {
        this.spawnRule = rules;
    }

    public void preParseSubBiomes()
    {
        String typeString = spawnRule.values.get(TYPES);
        if (typeString != null)
        {
            String[] args = typeString.split(",");
            for (String s : args)
            {
                BiomeType subBiome = null;
                for (BiomeType b : BiomeType.values())
                {
                    if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                    {
                        subBiome = b;
                        break;
                    }
                }
                if (subBiome == null && CompatWrapper.getBiomeType(s) == null)
                {
                    BiomeType.getBiome(s.trim(), true);
                }
            }
        }
    }

    public void reset()
    {
        validBiomes.clear();
        validSubBiomes.clear();
        blackListBiomes.clear();
        blackListSubBiomes.clear();
        parsed = false;
        valid = true;
    }

    public void parse()
    {
        if (parsed) return;
        parsed = true;
        validBiomes.clear();
        validSubBiomes.clear();
        blackListBiomes.clear();
        blackListSubBiomes.clear();
        preParseSubBiomes();
        String biomeString = spawnRule.values.get(BIOMES);
        String typeString = spawnRule.values.get(TYPES);
        String biomeBlacklistString = spawnRule.values.get(BIOMESBLACKLIST);
        String typeBlacklistString = spawnRule.values.get(TYPESBLACKLIST);
        Set<BiomeDictionary.Type> blackListTypes = Sets.newHashSet();
        Set<BiomeDictionary.Type> validTypes = Sets.newHashSet();
        if (spawnRule.values.containsKey(DAY))
        {
            day = Boolean.parseBoolean(spawnRule.values.get(DAY));
        }
        if (spawnRule.values.containsKey(NIGHT))
        {
            night = Boolean.parseBoolean(spawnRule.values.get(NIGHT));
        }
        if (spawnRule.values.containsKey(WATER))
        {
            water = Boolean.parseBoolean(spawnRule.values.get(WATER));
        }
        if (spawnRule.values.containsKey(AIR))
        {
            air = Boolean.parseBoolean(spawnRule.values.get(AIR));
            if (!air && !water) water = true;
        }
        if (spawnRule.values.containsKey(MINLIGHT))
        {
            minLight = Float.parseFloat(spawnRule.values.get(MINLIGHT));
        }
        if (spawnRule.values.containsKey(MAXLIGHT))
        {
            maxLight = Float.parseFloat(spawnRule.values.get(MAXLIGHT));
        }
        if (biomeString != null)
        {
            String[] args = biomeString.split(",");
            for (String s : args)
            {
                Biome biome = null;
                for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (BiomeDatabase.getBiomeName(b).replaceAll(" ", "").equalsIgnoreCase(s))
                        {
                            biome = b;
                            break;
                        }
                    }
                }
                if (biome != null)
                {
                    validBiomes.add(biome);
                }
            }
        }
        boolean hasForgeTypes = false;
        if (typeString != null)
        {
            String[] args = typeString.split(",");
            for (String s : args)
            {
                BiomeDictionary.Type type;
                type = CompatWrapper.getBiomeType(s);
                if (type != null)
                {
                    hasForgeTypes = true;
                    if (type == BiomeDictionary.Type.WATER)
                    {
                        validTypes.add(BiomeDictionary.Type.RIVER);
                        validTypes.add(BiomeDictionary.Type.OCEAN);
                    }
                    else validTypes.add(type);
                    continue;
                }
                BiomeType subBiome = BiomeType.getBiome(s.trim(), false);
                validSubBiomes.add(subBiome);
            }
        }
        if (biomeBlacklistString != null)
        {
            String[] args = biomeBlacklistString.split(",");
            for (String s : args)
            {
                Biome biome = null;
                for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (BiomeDatabase.getBiomeName(b).replaceAll(" ", "").equalsIgnoreCase(s))
                        {
                            biome = b;
                            break;
                        }
                    }
                }
                if (biome != null)
                {
                    blackListBiomes.add(biome);
                }
            }
        }
        if (typeBlacklistString != null)
        {
            String[] args = typeBlacklistString.split(",");
            for (String s : args)
            {

                BiomeDictionary.Type type = CompatWrapper.getBiomeType(s);
                if (type != null)
                {
                    if (type == BiomeDictionary.Type.WATER)
                    {
                        blackListTypes.add(BiomeDictionary.Type.RIVER);
                        blackListTypes.add(BiomeDictionary.Type.OCEAN);
                    }
                    else blackListTypes.add(type);
                    continue;
                }

                BiomeType subBiome = null;
                for (BiomeType b : BiomeType.values())
                {
                    if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                    {
                        subBiome = b;
                        break;
                    }
                }
                if (subBiome != BiomeType.NONE)
                {
                    blackListSubBiomes.add(subBiome);
                }
            }
        }

        for (ResourceLocation key : Biome.REGISTRY.getKeys())
        {
            Biome b = Biome.REGISTRY.getObject(key);
            if (b != null && !blackListBiomes.contains(b))
            {
                boolean matches = true;
                for (BiomeDictionary.Type type : validTypes)
                {
                    matches = matches && CompatWrapper.isOfType(b, type);
                    if (!matches) break;
                }
                if (matches) validBiomes.add(b);
            }
        }
        Set<Biome> toRemove = Sets.newHashSet();
        for (BiomeDictionary.Type type : blackListTypes)
        {
            for (Biome b : validBiomes)
            {
                if (CompatWrapper.isOfType(b, type))
                {
                    toRemove.add(b);
                    blackListBiomes.add(b);
                }
            }
        }
        validBiomes.removeAll(toRemove);
        if (hasForgeTypes && validBiomes.isEmpty()) valid = false;
        if (validSubBiomes.isEmpty() && validBiomes.isEmpty() && blackListBiomes.isEmpty()
                && blackListSubBiomes.isEmpty())
        {
            valid = false;
        }
    }

    public boolean matches(SpawnCheck checker)
    {
        boolean biome = biomeMatches(checker);
        if (!biome) return false;
        boolean loc = conditionsMatch(checker);
        return loc;
    }

    private boolean conditionsMatch(SpawnCheck checker)
    {
        boolean isDay = checker.day;
        if (isDay && !day) return false;
        if (!isDay && !night) return false;
        Material m = checker.material;
        boolean isWater = m == Material.WATER;
        if (isWater && !water) return false;
        if (m.isLiquid() && !isWater) return false;
        if (!air && !isWater) return false;
        float light = checker.light;
        return light <= maxLight && light >= minLight;
    }

    private boolean biomeMatches(SpawnCheck checker)
    {
        parse();
        if (!valid) return false;
        if (validSubBiomes.contains(BiomeType.ALL)) { return true; }
        if (validSubBiomes.contains(BiomeType.NONE) || (validBiomes.isEmpty() && validSubBiomes.isEmpty()
                && blackListSubBiomes.isEmpty() && blackListBiomes.isEmpty()))
            return false;
        boolean rightBiome = validBiomes.contains(checker.biome) || validBiomes.isEmpty();
        boolean rightSubBiome = (validSubBiomes.isEmpty() && checker.type == null)
                || validSubBiomes.contains(checker.type);
        if (validBiomes.isEmpty() && validSubBiomes.isEmpty()) rightSubBiome = true;
        BiomeType type = checker.type;
        if (checker.type == null) type = BiomeType.ALL;
        boolean blackListed = blackListBiomes.contains(checker.biome) || blackListSubBiomes.contains(type);
        if (blackListed) return false;
        return rightBiome && rightSubBiome;
    }
}

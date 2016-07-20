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

public class SpawnBiomeMatcher
{
    private static final QName BIOMES             = new QName("biomes");
    private static final QName TYPES              = new QName("types");
    private static final QName BIOMESBLACKLIST    = new QName("biomesBlacklist");
    private static final QName TYPESBLACKLIST     = new QName("typesBlacklist");
    private static final QName NIGHT              = new QName("night");
    private static final QName DAY                = new QName("day");
    private static final QName AIR                = new QName("air");
    private static final QName WATER              = new QName("water");
    private static final QName MINLIGHT           = new QName("minLight");
    private static final QName MAXLIGHT           = new QName("maxLight");

    public Set<Biome>          validBiomes        = Sets.newHashSet();
    public Set<BiomeType>      validSubBiomes     = Sets.newHashSet();
    public Set<Biome>          blackListBiomes    = Sets.newHashSet();
    public Set<BiomeType>      blackListSubBiomes = Sets.newHashSet();

    float                      minLight           = 0;
    float                      maxLight           = 1;
    boolean                    day                = true;
    boolean                    night              = true;
    boolean                    air                = true;
    boolean                    water              = false;

    final SpawnRule            spawnRule;

    public SpawnBiomeMatcher(SpawnRule rules)
    {
        this.spawnRule = rules;
    }

    public void parse()
    {
        validBiomes.clear();
        validSubBiomes.clear();
        blackListBiomes.clear();
        blackListSubBiomes.clear();

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
                        if (b.getBiomeName().replaceAll(" ", "").equalsIgnoreCase(s))
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
                if (subBiome == null)
                {
                    BiomeDictionary.Type type = BiomeDictionary.Type.valueOf(s.toUpperCase());
                    if (type != null)
                    {
                        if (type == BiomeDictionary.Type.WATER)
                        {
                            validTypes.add(BiomeDictionary.Type.RIVER);
                            validTypes.add(BiomeDictionary.Type.OCEAN);
                        }
                        else validTypes.add(type);
                    }
                }
                else
                {
                    validSubBiomes.add(subBiome);
                }
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
                        if (b.getBiomeName().replaceAll(" ", "").equalsIgnoreCase(s))
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
                BiomeType subBiome = null;
                for (BiomeType b : BiomeType.values())
                {
                    if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                    {
                        subBiome = b;
                        break;
                    }
                }
                if (subBiome == null)
                {
                    BiomeDictionary.Type type = BiomeDictionary.Type.valueOf(s.toUpperCase());
                    if (type != null)
                    {
                        if (type == BiomeDictionary.Type.WATER)
                        {
                            blackListTypes.add(BiomeDictionary.Type.RIVER);
                            blackListTypes.add(BiomeDictionary.Type.OCEAN);
                        }
                        else blackListTypes.add(type);
                    }
                }
                else
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
                    matches = matches && BiomeDictionary.isBiomeOfType(b, type);
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
                if (BiomeDictionary.isBiomeOfType(b, type))
                {
                    toRemove.add(b);
                    blackListBiomes.add(b);
                }
            }
        }
        validBiomes.removeAll(toRemove);
    }

    public boolean matches(Vector3 location, World world)
    {
        boolean biome = biomeMatches(location, world);
        if (!biome) return false;
        boolean loc = conditionsMatch(location, world);
        return loc;
    }

    private boolean conditionsMatch(Vector3 location, World world)
    {
        boolean isDay = world.isDaytime();
        if (isDay && !day) return false;
        if (!isDay && !night) return false;
        Material m = location.getBlockMaterial(world);
        boolean isWater = m == Material.WATER;
        if (isWater && !water) return false;
        if (m.isLiquid() && !isWater) return false;
        if (!air && !isWater) return false;
        int lightBlock = world.getLightFor(EnumSkyBlock.BLOCK, location.getPos());
        int lightDay = world.getLightFor(EnumSkyBlock.SKY, location.getPos());
        if (lightBlock == 0 && world.isDaytime()) lightBlock = lightDay;
        float light = lightBlock / 15f;
        return light <= maxLight && light >= minLight;
    }

    private boolean biomeMatches(Vector3 location, World world)
    {
        if (validSubBiomes.isEmpty() && validBiomes.isEmpty() && blackListBiomes.isEmpty()
                && blackListSubBiomes.isEmpty())
        {
            parse();
        }
        if (validSubBiomes.contains(BiomeType.ALL)) { return true; }
        if (validSubBiomes.contains(BiomeType.NONE) || (validBiomes.isEmpty() && validSubBiomes.isEmpty()))
            return false;
        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        Biome biome = location.getBiome(world);
        int subBiomeId = t.getBiome(location);
        BiomeType subBiome = BiomeDatabase.biomeTypeRegistry.getObjectById(subBiomeId);
        boolean rightBiome = validBiomes.contains(biome) || validBiomes.isEmpty();
        boolean rightSubBiome = (validSubBiomes.isEmpty() && subBiome == null) || validSubBiomes.contains(subBiome);
        if (validBiomes.isEmpty() && validSubBiomes.isEmpty()) rightSubBiome = true;
        if (subBiome == null) subBiome = BiomeType.ALL;
        boolean blackListed = blackListBiomes.contains(biome) || blackListSubBiomes.contains(subBiome);
        if (blackListed) return false;
        else return rightBiome && rightSubBiome;
    }
}

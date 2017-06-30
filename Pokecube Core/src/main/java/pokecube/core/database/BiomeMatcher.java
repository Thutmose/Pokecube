package pokecube.core.database;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatWrapper;

public class BiomeMatcher
{
    final String          toMatch;
    public Set<Biome>     validBiomes        = Sets.newHashSet();
    public Set<BiomeType> validSubBiomes     = Sets.newHashSet();
    public Set<Biome>     blackListBiomes    = Sets.newHashSet();
    public Set<BiomeType> blackListSubBiomes = Sets.newHashSet();
    boolean               needAll            = false;

    public BiomeMatcher(String string)
    {
        toMatch = string;
    }

    public void parse()
    {
        validBiomes.clear();
        validSubBiomes.clear();
        blackListBiomes.clear();
        blackListSubBiomes.clear();

        String parse = toMatch;
        if (!parse.startsWith("B")) throw new IllegalArgumentException("Error with matching format for:" + toMatch);
        parse = parse.substring(1);
        String[] args = parse.split("'");
        Set<BiomeDictionary.Type> blackListTypes = Sets.newHashSet();
        Set<BiomeDictionary.Type> validTypes = Sets.newHashSet();
        for (String arg : args)
        {
            if (arg.startsWith("B"))
            {
                // Blacklisted.
                arg = arg.replaceFirst("B", "T");
                parseType(arg, blackListSubBiomes, blackListBiomes, blackListTypes);
            }
            else if (arg.startsWith("T"))
            {
                // Biome Type to match
                parseType(arg, validSubBiomes, validBiomes, validTypes);
            }
            else
            {
                // Biome to match.
                arg = "S" + arg;
                parseType(arg, validSubBiomes, validBiomes, validTypes);
            }
        }

        if (!needAll) for (BiomeDictionary.Type type : validTypes)
        {
            Set<Biome> biomes = CompatWrapper.getBiomes(type);
            for (Biome b : biomes)
            {
                validBiomes.add(b);
            }
        }
        else
        {
            for (BiomeDictionary.Type type : validTypes)
            {
                Set<Biome> biomes = CompatWrapper.getBiomes(type);
                biome:
                for (Biome b : biomes)
                {
                    for (BiomeDictionary.Type type1 : validTypes)
                    {
                        if (!CompatWrapper.isOfType(b, type1)) continue biome;
                    }
                    validBiomes.add(b);
                }
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
    }

    private void parseType(String arg, Set<BiomeType> subBiomes, Set<Biome> biomes, Set<BiomeDictionary.Type> types)
    {
        String biomeName = arg.substring(1);
        boolean specific = biomeName.startsWith("S");
        if (specific)
        {
            biomeName = biomeName.substring(1);
        }
        if (biomeName.startsWith("W"))
        {
            needAll = true;
            biomeName = biomeName.substring(1);
        }
        BiomeType subBiome = null;
        Biome biome = null;
        if (!specific) for (BiomeType b : BiomeType.values())
        {
            if (b.name.replaceAll(" ", "").equalsIgnoreCase(biomeName))
            {
                subBiome = b;
                break;
            }
        }
        if (subBiome == null)
        {
            if (!specific)
            {
                BiomeDictionary.Type type = CompatWrapper.getBiomeType(biomeName);
                if (type != null)
                {
                    if (type == BiomeDictionary.Type.WATER)
                    {
                        types.add(BiomeDictionary.Type.RIVER);
                        types.add(BiomeDictionary.Type.OCEAN);
                    }
                    else types.add(type);
                }
            }
            for (ResourceLocation key : Biome.REGISTRY.getKeys())
            {
                Biome b = Biome.REGISTRY.getObject(key);
                if (b != null)
                {
                    if (BiomeDatabase.getBiomeName(b).replaceAll(" ", "").equalsIgnoreCase(biomeName))
                    {
                        biome = b;
                        break;
                    }
                }
            }
        }
        if (subBiome != null)
        {
            subBiomes.add(subBiome);
        }
        if (biome != null)
        {
            biomes.add(biome);
        }
    }

    public boolean matches(Vector3 location, World world)
    {
        if (validSubBiomes.isEmpty() && validBiomes.isEmpty() && blackListBiomes.isEmpty()
                && blackListSubBiomes.isEmpty())
        {
            parse();
        }
        if (validSubBiomes.contains(BiomeType.ALL)) return true;
        if (validSubBiomes.contains(BiomeType.NONE)) return false;
        TerrainSegment t = TerrainManager.getInstance().getTerrian(world, location);
        Biome biome = location.getBiome(world);
        int subBiomeId = t.getBiome(location);
        BiomeType subBiome = BiomeType.getType(subBiomeId);
        boolean rightBiome = validBiomes.contains(biome);
        boolean rightSubBiome = subBiome != null && validSubBiomes.contains(subBiome);

        if (validBiomes.isEmpty() && validSubBiomes.isEmpty()) rightSubBiome = true;
        if (rightBiome && validSubBiomes.isEmpty())
        {
            rightSubBiome = true;
        }
        if (rightSubBiome && validBiomes.isEmpty())
        {
            rightBiome = true;
        }
        if (subBiome == null) subBiome = BiomeType.ALL;
        boolean blackListed = blackListBiomes.contains(biome) || blackListSubBiomes.contains(subBiome);
        if (blackListed) return false;
        if (needAll) return rightBiome && rightSubBiome;
        return rightBiome || rightSubBiome;
    }
}

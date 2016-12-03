package pokecube.compat.jer;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import jeresources.api.IJERAPI;
import jeresources.api.JERPlugin;
import jeresources.api.conditionals.LightLevel;
import jeresources.api.distributions.DistributionBase;
import jeresources.api.distributions.DistributionSquare;
import jeresources.api.drop.LootDrop;
import jeresources.api.drop.PlantDrop;
import jeresources.api.render.IMobRenderHook;
import jeresources.api.restrictions.BiomeRestriction;
import jeresources.api.restrictions.Restriction;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.lib.CompatClass;
import thut.lib.CompatWrapper;
import thut.lib.CompatClass.Phase;

public class JERCompat
{
    @JERPlugin
    public static IJERAPI                             JERAPI;

    public static final IMobRenderHook<EntityPokemob> POKEMOB = new IMobRenderHook<EntityPokemob>()
                                                              {
                                                                  @Override
                                                                  public IMobRenderHook.RenderInfo transform(
                                                                          IMobRenderHook.RenderInfo renderInfo,
                                                                          EntityPokemob pokemob)
                                                                  {
                                                                      float mobScale = pokemob.getSize();
                                                                      pokemob.prevRotationYawHead = pokemob.rotationYawHead;
                                                                      pokemob.prevRotationPitch = pokemob.rotationPitch;
                                                                      float size = Math.max(
                                                                              pokemob.getPokedexEntry().width
                                                                                      * mobScale,
                                                                              Math.max(
                                                                                      pokemob.getPokedexEntry().height
                                                                                              * mobScale,
                                                                                      pokemob.getPokedexEntry().length
                                                                                              * mobScale));
                                                                      float zoom = (float) (1f / Math.pow(size, 0.7));
                                                                      renderInfo.scale = zoom;
                                                                      GL11.glTranslated(0, 0, 0);
                                                                      GL11.glScalef(zoom, zoom, zoom);
                                                                      renderInfo.pitch = 0;
                                                                      renderInfo.yaw = 0;
                                                                      return renderInfo;
                                                                  }
                                                              };

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "jeresources")
    @CompatClass(phase = Phase.POSTPOST)
    public static void JERInit()
    {
        new pokecube.compat.jer.JERCompat().register();
    }

    public JERCompat()
    {
    }

    private LootDrop[] getDrops(PokedexEntry entry)
    {
        boolean hasDrops = false;
        hasDrops = !entry.drops.isEmpty() || !entry.held.isEmpty();
        if (!hasDrops) return null;

        ArrayList<LootDrop> drops = new ArrayList<LootDrop>();
        LootDrop drop = null;
        for (ItemStack stack : entry.drops.keySet())
        {
            if (stack == null) continue;
            float chance = entry.drops.get(stack);
            drops.add(drop = new LootDrop(stack, chance));
            drop.minDrop = 1;
            drop.maxDrop = CompatWrapper.getStackSize(stack);
        }
        for (ItemStack stack : entry.held.keySet())
        {
            if (stack == null) continue;
            float chance = entry.held.get(stack);
            drops.add(drop = new LootDrop(stack, chance));
            drop.minDrop = drop.maxDrop;
        }
        return drops.toArray(new LootDrop[0]);
    }

    private LightLevel getLightLevel(PokedexEntry entry)
    {
        if (entry.getSpawnData() != null)
        {
            List<SpawnBiomeMatcher> matchers = Lists.newArrayList(entry.getSpawnData().matchers.keySet());
            if (matchers.get(0) != null)
            {
                // SpawnRule spawnRule = matchers.get(0).spawnRule;
                // float maxLight = 1;
                // float minLight = 0;
                // boolean day = true;
                // boolean night = true;
                // Float.parseFloat(spawnRule.values.get(SpawnBiomeMatcher.MAXLIGHT));
                // if (spawnRule.values.containsKey(SpawnBiomeMatcher.DAY))
                // {
                // day =
                // Boolean.parseBoolean(spawnRule.values.get(SpawnBiomeMatcher.DAY));
                // }
                // if (spawnRule.values.containsKey(SpawnBiomeMatcher.NIGHT))
                // {
                // night =
                // Boolean.parseBoolean(spawnRule.values.get(SpawnBiomeMatcher.NIGHT));
                // }
                // if (spawnRule.values.containsKey(SpawnBiomeMatcher.MINLIGHT))
                // {
                // minLight =
                // Float.parseFloat(spawnRule.values.get(SpawnBiomeMatcher.MINLIGHT));
                // }
                // if (spawnRule.values.containsKey(SpawnBiomeMatcher.MAXLIGHT))
                // {
                // maxLight =
                // Float.parseFloat(spawnRule.values.get(SpawnBiomeMatcher.MAXLIGHT));
                // }
                // Relative relative = Relative.below;
                // int light = 15;
                // if (maxLight != 1)
                // {
                // relative = Relative.below;
                // light = (int) (maxLight * 15);
                // }
                // TODO see about asking JER to open up this constructor.
                // LightLevel ret = new LightLevel(light, relative);
                // return ret;
            }
        }

        return LightLevel.any;
    }

    private String[] getSpawns(PokedexEntry entry)
    {
        ArrayList<String> biomes = new ArrayList<String>();
        PokedexEntry spawnable = entry;
        if (entry.getSpawnData() == null) spawnable = entry.getChild();
        SpawnData data;
        if ((data = spawnable.getSpawnData()) != null)
        {
            for (ResourceLocation key : Biome.REGISTRY.getKeys())
            {
                Biome biome = Biome.REGISTRY.getObject(key);
                if (biome != null && biome.getBiomeName() != null && data.isValid(biome))
                {
                    biomes.add(biome.getBiomeName());
                }
            }
            for (BiomeType type : BiomeType.values())
            {
                if (data.isValid(type))
                {
                    biomes.add(type.readableName);
                }
            }
        }
        if (biomes.isEmpty()) biomes.add("Unknown");
        return biomes.toArray(new String[0]);
    }

    public void register()
    {
        try
        {
            registerMobs();
        }
        catch (Throwable e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        registerOres();
        registerPlants();
    }

    @SuppressWarnings("unchecked")
    private void registerMobs()
    {
        System.out.println("Registering Mobs for JER");
        for (PokedexEntry e : Database.allFormes)
        {
            LootDrop[] drops = getDrops(e);
            if (drops == null) continue;
            Entity poke = PokecubeMod.core.createPokemob(e, PokecubeCore.proxy.getWorld());
            if (poke == null) continue;
            ((IPokemob) poke).setShiny(false);
            ((IPokemob) poke).setSize(1);
            JERAPI.getMobRegistry().register((EntityLivingBase) poke, getLightLevel(e), getSpawns(e), drops);
            JERAPI.getMobRegistry()
                    .registerRenderHook(PokecubeMod.core.getEntityClassFromPokedexNumber(e.getPokedexNb()), POKEMOB);
        }
    }

    private void registerOres()
    {
        System.out.println("Registering Ores for JER");
        ItemStack fossilStone = PokecubeItems.getStack("fossilstone");

        DistributionBase distrubution = new DistributionSquare(5, 44, 12 / 265f);
        Restriction restriction = new Restriction(new BiomeRestriction(BiomeDatabase.getBiome("desertHills"),
                BiomeDatabase.getBiome("desert"), BiomeDatabase.getBiome("jungle"),
                BiomeDatabase.getBiome("jungleHills"), BiomeDatabase.getBiome("ocean")));
        ArrayList<LootDrop> drops = new ArrayList<LootDrop>();
        LootDrop drop = null;
        int num = PokecubeItems.fossils.size();
        for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            if (stack == null) continue;
            float chance = 1f / (2 * num);
            drops.add(drop = new LootDrop(stack, chance));
            drop.minDrop = 1;
            drop.maxDrop = 1;
        }
        drops.add(new LootDrop(new ItemStack(Items.BONE), 0.5f));
        JERAPI.getWorldGenRegistry().register(fossilStone, distrubution, restriction, drops.toArray(new LootDrop[0]));
    }

    private void registerPlants()
    {
        System.out.println("Registering Crops for JER");
        for (Integer i : BerryManager.berryCrops.keySet())
        {
            Block crop = BerryManager.berryCrops.get(i);
            ItemStack berry = BerryManager.getBerryItem(BerryManager.berryNames.get(i));
            JERAPI.getPlantRegistry().register(new ItemStack(crop), new PlantDrop(berry, 1, 1));
        }
    }
}

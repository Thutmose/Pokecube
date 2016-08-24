package pokecube.compat.jer;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import jeresources.api.IJERAPI;
import jeresources.api.JERPlugin;
import jeresources.api.conditionals.LightLevel;
import jeresources.api.drop.LootDrop;
import jeresources.api.drop.PlantDrop;
import jeresources.api.render.IMobRenderHook;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import thut.api.terrain.BiomeType;

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
                                                                      float size = Math.max(
                                                                              pokemob.getPokedexEntry().width
                                                                                      * mobScale,
                                                                              Math.max(
                                                                                      pokemob.getPokedexEntry().height
                                                                                              * mobScale,
                                                                                      pokemob.getPokedexEntry().length
                                                                                              * mobScale));
                                                                      float zoom = (float) (1f / Math.sqrt(size));
                                                                      renderInfo.scale = zoom;
                                                                      GL11.glTranslated(0, 0.5, 0);
                                                                      GL11.glScalef(zoom, zoom, zoom);
                                                                      return renderInfo;
                                                                  }
                                                              };

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
            drop.maxDrop = stack.stackSize;
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
        return LightLevel.any;
    }

    private String[] getSpawns(PokedexEntry entry)
    {
        ArrayList<String> biomes = new ArrayList<String>();
        PokedexEntry spawnable = entry;
        if (entry.getSpawnData() == null) spawnable = Database.getEntry(entry.getChildNb());
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
        registerMobs();
        registerOres();
        registerPlants();
    }

    @SuppressWarnings("unchecked")
    private void registerMobs()
    {
        for (PokedexEntry e : Database.allFormes)
        {
            LootDrop[] drops = getDrops(e);
            if (drops == null) continue;
            Entity poke = PokecubeMod.core.createEntityByPokedexNb(e.getPokedexNb(), null);
            if (poke == null) continue;
            ((IPokemob) poke).changeForme(e.getName());
            ((IPokemob) poke).setShiny(false);
            ((IPokemob) poke).setSize(1);
            JERAPI.getMobRegistry().register((EntityLivingBase) poke, getLightLevel(e), getSpawns(e), drops);
            JERAPI.getMobRegistry()
                    .registerRenderHook(PokecubeMod.core.getEntityClassFromPokedexNumber(e.getPokedexNb()), POKEMOB);
        }
    }

    private void registerOres()
    {

    }

    private void registerPlants()
    {
        for (Integer i : BerryManager.berryCrops.keySet())
        {
            Block crop = BerryManager.berryCrops.get(i);
            ItemStack berry = BerryManager.getBerryItem(BerryManager.berryNames.get(i));
            JERAPI.getPlantRegistry().register(new ItemStack(crop), new PlantDrop(berry, 1, 1));
        }
    }
}

package pokecube.compat.jer;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

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
import jeresources.compatibility.CompatBase;
import jeresources.util.LootTableHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.berries.BerryManager;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.lib.CompatClass;
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
                                                                          EntityPokemob entity)
                                                                  {
                                                                      IPokemob pokemob = CapabilityPokemob
                                                                              .getPokemobFor(entity);
                                                                      float mobScale = pokemob.getSize();
                                                                      entity.prevRotationYawHead = entity.rotationYawHead;
                                                                      entity.prevRotationPitch = entity.rotationPitch;
                                                                      Vector3f dims = pokemob.getPokedexEntry()
                                                                              .getModelSize();
                                                                      float size = Math.max(dims.z * mobScale, Math.max(
                                                                              dims.y * mobScale, dims.x * mobScale));
                                                                      float zoom = (float) (1f / Math.pow(size, 0.5));
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
        new JERCompat();
    }

    public JERCompat()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private LootDrop[] getDrops(PokedexEntry entry)
    {
        boolean hasDrops = false;
        hasDrops = !entry.drops.isEmpty() || !entry.held.isEmpty() || entry.heldTable != null
                || entry.lootTable != null;
        if (!hasDrops) return null;

        ArrayList<LootDrop> drops = new ArrayList<LootDrop>();
        LootDrop drop = null;
        for (ItemStack stack : entry.drops.keySet())
        {
            if (stack == null) continue;
            float chance = entry.drops.get(stack);
            drops.add(drop = new LootDrop(stack, chance));
            drop.minDrop = 1;
            drop.maxDrop = stack.getCount();
        }
        for (ItemStack stack : entry.held.keySet())
        {
            if (stack == null) continue;
            float chance = entry.held.get(stack);
            drops.add(drop = new LootDrop(stack, chance));
            drop.minDrop = drop.maxDrop;
        }
        World world = CompatBase.getWorld();
        if (entry.heldTable != null)
        {
            drops.addAll(LootTableHelper.toDrops(world, entry.heldTable));

        }
        if (entry.lootTable != null)
        {
            drops.addAll(LootTableHelper.toDrops(world, entry.lootTable));
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
                // TODO fill this out.
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void register(WorldEvent.Load event)
    {
        registerMobs();
        registerOres();
        registerPlants();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SuppressWarnings("unchecked")
    private void registerMobs()
    {
        if (PokecubeMod.debug) PokecubeMod.log("Registering Mobs for JER");
        for (PokedexEntry e : Database.getSortedFormes())
        {
            if (!Pokedex.getInstance().isRegistered(e)) continue;
            Entity poke = PokecubeMod.core.createPokemob(e, PokecubeCore.proxy.getWorld());
            if (poke == null) continue;
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(poke);
            LootDrop[] drops = getDrops(e);
            if (drops != null)
            {
                pokemob.setShiny(false);
                pokemob.setSize(1);
                JERAPI.getMobRegistry().register((EntityLivingBase) poke, getLightLevel(e), getSpawns(e), drops);
                JERAPI.getMobRegistry().registerRenderHook(PokecubeMod.core.getEntityClassForEntry(e), POKEMOB);
            }
        }
    }

    private void registerOres()
    {
        if (PokecubeMod.debug) PokecubeMod.log("Registering Ores for JER");
        ItemStack fossilStone = new ItemStack(PokecubeItems.fossilStone);
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
        if (PokecubeMod.debug) PokecubeMod.log("Registering Crops for JER");
        for (Integer i : BerryManager.berryCrops.keySet())
        {
            Block crop = BerryManager.berryCrops.get(i);
            ItemStack berry = BerryManager.getBerryItem(BerryManager.berryNames.get(i));
            JERAPI.getPlantRegistry().register(new ItemStack(crop), new PlantDrop(berry, 1, 1));
        }
    }
}

package pokecube.compat.jer;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import jeresources.api.conditionals.LightLevel;
import jeresources.api.distributions.DistributionSquare;
import jeresources.api.drop.DropItem;
import jeresources.api.render.IMobRenderHook;
import jeresources.api.restrictions.BiomeRestriction;
import jeresources.api.restrictions.Restriction;
import jeresources.api.restrictions.Type;
import jeresources.compatibility.CompatBase;
import jeresources.compatibility.ModList;
import jeresources.entries.MobEntry;
import jeresources.entries.WorldGenEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.EnumHelper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.terrain.BiomeType;

public class JERCompat extends CompatBase
{
    public static void register()
    {
        EnumHelper.addEnum(ModList.class, "pokecube", new Class[] { String.class, Class.class },
                new Object[] { "pokecube", JERCompat.class });
    }

    @Override
    protected void init(boolean initOres)
    {
        registerMobs();
        registerOres();
    }

    private void registerOres()
    {
        BiomeRestriction biomes = new BiomeRestriction(Type.WHITELIST, BiomeGenBase.desertHills, BiomeGenBase.desert,
                BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.ocean);
        Restriction restriction = new Restriction(biomes);
        
        registerWorldGen(new WorldGenEntry(PokecubeItems.getStack("fossilstone"), new DistributionSquare(7, 5, 5, 45),
                restriction, true, getFossils()));
    }

    @SuppressWarnings("unchecked")
    private void registerMobs()
    {
        for (PokedexEntry e : Database.allFormes)
        {
            DropItem[] drops = getDrops(e);
            if (drops == null) continue;
            Entity poke = PokecubeMod.core.createEntityByPokedexNb(e.getPokedexNb(), world);
            if (poke == null) continue;
            ((IPokemob) poke).changeForme(e.getName());
            ((IPokemob) poke).setShiny(false);
            ((IPokemob) poke).setSize(1);
            MobEntry entry = new MobEntry((EntityLivingBase) poke, getLightLevel(e), getSpawns(e), drops);
            registerMob(entry);
            registerMobRenderHook(PokecubeMod.core.getEntityClassFromPokedexNumber(e.getPokedexNb()), POKEMOB);
        }
    }

    private LightLevel getLightLevel(PokedexEntry entry)
    {
        return LightLevel.any;
    }

    private String[] getSpawns(PokedexEntry entry)
    {
        int num = entry.getChildNb();
        ArrayList<String> biomes = new ArrayList<String>();
        PokedexEntry spawnable = Database.getEntry(num);
        SpawnData data;
        if ((data = spawnable.getSpawnData()) != null)
        {
            for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
            {
                if (biome != null && biome.biomeName != null && data.isValid(biome.biomeID))
                {
                    biomes.add(biome.biomeName);
                }
            }
            for (BiomeType type : BiomeType.values())
            {
                if (data.isValid(type.getType()))
                {
                    biomes.add(type.readableName);
                }
            }
        }
        if (biomes.isEmpty()) biomes.add("None");
        return biomes.toArray(new String[0]);
    }
    
    private DropItem[] getFossils()
    {
        ArrayList<DropItem> drops = new ArrayList<DropItem>();
        for(ItemStack stack: PokecubeItems.fossils.keySet())
        {
            drops.add(new DropItem(stack, 0.5f/(float)PokecubeItems.fossils.size()));
        }
        return drops.toArray(new DropItem[0]);
    }

    private DropItem[] getDrops(PokedexEntry entry)
    {
        boolean hasDrops = false;
        ItemStack foodDrop = entry.getFoodDrop(0);
        hasDrops = foodDrop != null;
        hasDrops = hasDrops || !entry.rareDrops.isEmpty();
        hasDrops = hasDrops || !entry.commonDrops.isEmpty();
        hasDrops = hasDrops || !entry.heldItems.isEmpty();
        if (!hasDrops) return null;

        ArrayList<DropItem> drops = new ArrayList<DropItem>();
        DropItem drop = null;
        if (foodDrop != null) drops.add(drop = new DropItem(foodDrop));
        if (drop != null) drop.conditionals.add("food");
        int totalRare = entry.rareDrops.size();
        int totalCommon = entry.commonDrops.size();

        if (totalCommon > 0)
        {
            for (ItemStack stack : entry.commonDrops.keySet())
            {
                if (stack == null) continue;
                float chance = entry.commonDrops.get(stack) / 100f;
                chance /= (float) totalCommon;
                drops.add(drop = new DropItem(stack, chance));
            }
        }
        if (totalRare > 0)
        {
            for (ItemStack stack : entry.rareDrops.keySet())
            {
                if (stack == null) continue;
                float chance = (1 / 7f) * entry.rareDrops.get(stack) / 100f;
                chance /= (float) totalRare;
                drops.add(drop = new DropItem(stack, chance));
            }
        }
        for (ItemStack stack : entry.heldItems.keySet())
        {
            if (stack == null) continue;
            float chance = entry.heldItems.get(stack) / 100f;
            drops.add(drop = new DropItem(stack, chance));
            drop.minDrop = drop.maxDrop;
            drop.conditionals.add("held");
        }
        return drops.toArray(new DropItem[0]);
    }

    public static final IMobRenderHook<EntityPokemob> POKEMOB = new IMobRenderHook<EntityPokemob>()
    {
        @Override
        public IMobRenderHook.RenderInfo transform(IMobRenderHook.RenderInfo renderInfo, EntityPokemob pokemob)
        {
            GL11.glRotated(180, 0, 1, 0);
            float mobScale = pokemob.getSize();
            float size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                    Math.max(pokemob.getPokedexEntry().height * mobScale, pokemob.getPokedexEntry().length * mobScale));
            float zoom = (float) (1f / Math.sqrt(size));
            renderInfo.scale = zoom;
            GL11.glScalef(zoom, zoom, zoom);
            return renderInfo;
        }
    };
}

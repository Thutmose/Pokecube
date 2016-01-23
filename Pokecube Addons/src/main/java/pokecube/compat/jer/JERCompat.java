package pokecube.compat.jer;

import java.util.ArrayList;
import java.util.Arrays;

import jeresources.api.utils.DropItem;
import jeresources.api.utils.LightLevel;
import jeresources.compatibility.CompatBase;
import jeresources.entries.MobEntry;
import jeresources.utils.ModList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class JERCompat extends CompatBase
{
    public static void register()
    {
        EnumHelper.addEnum(ModList.class, "pokecube", new Class[] { String.class, Class.class },
                new Object[] { "pokecube", JERCompat.class });
        System.out.println("Added Enum");
        new Exception().printStackTrace();
        System.out.println(Arrays.toString(ModList.values()));
    }

    @Override
    protected void init(boolean initOres)
    {
        registerMobs();
        System.out.println("Registered Mob Drops");
    }

    private void registerMobs()
    {
        for (PokedexEntry e : Database.allFormes)
        {
            DropItem[] drops = getDrops(e);
            if (drops == null) continue;
            Entity poke = PokecubeMod.core.createEntityByPokedexNb(e.getPokedexNb(), world);
            if (poke == null) continue;
            ((IPokemob) poke).changeForme(e.getName());
            MobEntry entry = new MobEntry((EntityLivingBase) poke, LightLevel.any, drops);
            registerMob(entry);
        }
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
            chance /= (float) entry.heldItems.size();
            drops.add(drop = new DropItem(stack, chance));
            drop.conditionals.add("held");
        }
        return drops.toArray(new DropItem[0]);
    }
}

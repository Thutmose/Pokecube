package pokecube.adventures.handlers.loot;

import java.util.Collection;
import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;

public class Loot
{
    public static LootFunction getFunction()
    {
        return new LootFunction(new LootCondition[0])
        {
            @Override
            public ItemStack apply(ItemStack stack, Random rand, LootContext context)
            {
                return stack;
            }
        };
    }

    public static LootEntry getEntryItem(final ItemStack loot, int weightIn, int num, String entryName)
    {
        final LootFunction function = getFunction();
        return new LootEntry(weightIn, num, new LootCondition[0], entryName)
        {
            @Override
            public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
            {
                int i = 0;
                ItemStack itemstack = loot;
                LootFunction lootfunction = function;

                if (LootConditionManager.testAllConditions(lootfunction.getConditions(), rand, context))
                {
                    itemstack = lootfunction.apply(itemstack, rand, context);
                }

                int size = itemstack.getCount();
                if (size > 0)
                {
                    if (size < itemstack.getItem().getItemStackLimit(itemstack))
                    {
                        stacks.add(itemstack.copy());
                    }
                    else
                    {
                        i = size;
                        while (i > 0)
                        {
                            ItemStack itemstack1 = itemstack.copy();
                            int size1 = Math.min(itemstack.getMaxStackSize(), i);
                            itemstack1.setCount(size1);
                            i -= size1;
                            stacks.add(itemstack1);
                        }
                    }
                }
            }

            @Override
            protected void serialize(JsonObject json, JsonSerializationContext context)
            {
                ResourceLocation resourcelocation = new ResourceLocation(entryName);
                json.addProperty("name", resourcelocation.toString());
            }
        };
    }

}

package pokecube.core.items.loot.functions;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemHeldItems;
import thut.lib.CompatWrapper;

public class MakeHeldItem extends LootFunction
{
    @ObjectHolder(value = "pokecube:held")
    public static final Item                  ITEM    = null;

    private static final Map<String, Integer> nameMap = Maps.newHashMap();

    final String                              arg;

    protected MakeHeldItem(LootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        if (ITEM == null)
        {
            PokecubeMod.log(Level.SEVERE, "No Held Item Registered?");
            return stack;
        }
        if (nameMap.isEmpty())
        {
            for (int i = 0; i < ItemHeldItems.variants.size(); i++)
            {
                nameMap.put(ItemHeldItems.variants.get(i), i);
            }
        }
        if (nameMap.containsKey(arg))
        {
            ItemStack newStack = new ItemStack(ITEM, CompatWrapper.getStackSize(stack), nameMap.get(arg));
            newStack.setTagCompound(stack.getTagCompound());
            return newStack;
        }
        else
        {
            PokecubeMod.log(Level.SEVERE, "Error making held item for " + arg);
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeHeldItem>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_held"), MakeHeldItem.class);
        }

        @Override
        public void serialize(JsonObject object, MakeHeldItem functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }

        @Override
        public MakeHeldItem deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            String arg = object.get("type").getAsString();
            return new MakeHeldItem(conditionsIn, arg);
        }
    }
}
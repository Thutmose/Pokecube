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
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.PokecubeMod;

public class MakeMegastone extends LootFunction
{
    @ObjectHolder(value = "pokecube:megastone")
    public static final Item                  ITEM    = null;

    private static final Map<String, Integer> nameMap = Maps.newHashMap();

    final String                              arg;

    protected MakeMegastone(LootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        if (ITEM == null)
        {
            PokecubeMod.log(Level.SEVERE, "No Megastone Item Registered?");
            return stack;
        }
        if (nameMap.isEmpty())
        {
            for (int i = 0; i < ItemGenerator.variants.size(); i++)
            {
                nameMap.put(ItemGenerator.variants.get(i), i);
            }
        }
        if (nameMap.containsKey(arg))
        {
            ItemStack newStack = new ItemStack(ITEM, stack.getCount(), nameMap.get(arg));
            newStack.setTagCompound(stack.getTagCompound());
            return newStack;
        }
        else
        {
            PokecubeMod.log(Level.SEVERE, "Error making megastone for " + arg);
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeMegastone>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_megastone"), MakeMegastone.class);
        }

        @Override
        public void serialize(JsonObject object, MakeMegastone functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }

        @Override
        public MakeMegastone deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            String arg = object.get("type").getAsString();
            return new MakeMegastone(conditionsIn, arg);
        }
    }
}

package pokecube.core.items.loot.functions;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;

public class MakeHeldItem extends LootFunction
{
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
        if (nameMap.containsKey(arg))
        {
            ItemStack newStack = PokecubeItems.getStack(arg);
            newStack.setCount(stack.getCount());
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
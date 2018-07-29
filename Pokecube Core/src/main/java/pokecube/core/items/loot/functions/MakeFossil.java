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

public class MakeFossil extends LootFunction
{
    private static final Map<String, Integer> fossils = Maps.newHashMap();

    final String                              arg;

    protected MakeFossil(LootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        if (fossils.containsKey(arg))
        {
            ItemStack newStack = PokecubeItems.getStack("fossil_" + arg);
            newStack.setCount(stack.getCount());
            newStack.setTagCompound(stack.getTagCompound());
            return newStack;
        }
        else
        {
            PokecubeMod.log(Level.SEVERE, "Error making fossil for " + arg);
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeFossil>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_fossil"), MakeFossil.class);
        }

        @Override
        public void serialize(JsonObject object, MakeFossil functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }

        @Override
        public MakeFossil deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            String arg = object.get("type").getAsString();
            return new MakeFossil(conditionsIn, arg);
        }
    }
}
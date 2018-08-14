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
import pokecube.core.items.vitamins.ItemVitamin;

public class MakeVitamin extends LootFunction
{
    @ObjectHolder(value = "pokecube:vitamin")
    public static final Item                  VITAMIN  = null;

    private static final Map<String, Integer> vitamins = Maps.newHashMap();

    final String                              arg;

    protected MakeVitamin(LootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        if (VITAMIN == null)
        {
            PokecubeMod.log(Level.SEVERE, "No Vitamin Item Registered?");
            return stack;
        }
        if (vitamins.isEmpty())
        {
            for (int i = 0; i < ItemVitamin.vitamins.size(); i++)
            {
                vitamins.put(ItemVitamin.vitamins.get(i), i);
            }
        }
        if (vitamins.containsKey(arg))
        {
            ItemStack vitamin = new ItemStack(VITAMIN, stack.getCount(), vitamins.get(arg));
            vitamin.setTagCompound(stack.getTagCompound());
            return vitamin;
        }
        else
        {
            PokecubeMod.log(Level.SEVERE, "Error making vitamin for " + arg);
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeVitamin>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_vitamin"), MakeVitamin.class);
        }

        @Override
        public void serialize(JsonObject object, MakeVitamin functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }

        @Override
        public MakeVitamin deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            String arg = object.get("type").getAsString();
            return new MakeVitamin(conditionsIn, arg);
        }
    }
}
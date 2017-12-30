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
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatWrapper;

public class MakeFossil extends LootFunction
{
    @ObjectHolder(value = "pokecube:fossil")
    public static final Item                  FOSSIL  = null;

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
        if (FOSSIL == null)
        {
            PokecubeMod.log(Level.SEVERE, "No Fossil Item Registered?");
            return stack;
        }
        if (fossils.isEmpty())
        {
            for (int i = 0; i < HeldItemHandler.fossilVariants.size(); i++)
            {
                fossils.put(HeldItemHandler.fossilVariants.get(i), i);
            }
        }
        if (fossils.containsKey(arg))
        {
            ItemStack fossil = new ItemStack(FOSSIL, CompatWrapper.getStackSize(stack), fossils.get(arg));
            fossil.setTagCompound(stack.getTagCompound());
            return fossil;
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
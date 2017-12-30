package pokecube.core.items.loot.functions;

import java.util.Random;
import java.util.logging.Level;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import thut.lib.CompatWrapper;

public class MakeBerry extends LootFunction
{
    final String arg;

    protected MakeBerry(LootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        ItemStack berry = BerryManager.getBerryItem(arg);
        if (!CompatWrapper.isValid(berry))
        {
            PokecubeMod.log(Level.SEVERE, "Error making berry for " + arg);
        }
        else return berry;
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeBerry>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_berry"), MakeBerry.class);
        }

        @Override
        public void serialize(JsonObject object, MakeBerry functionClazz, JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }

        @Override
        public MakeBerry deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            String arg = object.get("type").getAsString();
            return new MakeBerry(conditionsIn, arg);
        }
    }
}

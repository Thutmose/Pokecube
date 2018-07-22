package pokecube.adventures.handlers.loot;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

public class MakeDnaBottle extends LootFunction
{
    @ObjectHolder(value = "minecraft:potion")
    public static final Item            DNABOTTLE = null;

    final Map<ResourceLocation, String> arg;

    protected MakeDnaBottle(LootCondition[] conditionsIn, Map<ResourceLocation, String> arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        if (DNABOTTLE == null)
        {
            PokecubeMod.log(Level.SEVERE, "No water bottle Item Registered?");
            return stack;
        }
        if (arg.isEmpty())
        {
            PokecubeMod.log(Level.SEVERE, "no dna here?");
            return stack;
        }
        IMobGenetics genes = IMobGenetics.GENETICS_CAP.getDefaultInstance();
        for (ResourceLocation loc : arg.keySet())
        {
            String val = arg.get(loc);
            if (GeneRegistry.getClass(loc) != null)
            {
                try
                {
                    Gene gene = GeneRegistry.getClass(loc).newInstance();
                    Alleles alleles = new Alleles(gene, gene);
                    if (val.equals("random"))
                    {
                        alleles.getAlleles()[0] = alleles.getAlleles()[0].mutate();
                        alleles.getAlleles()[1] = alleles.getAlleles()[1].mutate();
                        alleles.refreshExpressed();
                    }
                    else
                    {
                        NBTTagCompound tag = JsonToNBT.getTagFromJson(val);
                        alleles.load(tag);
                    }
                    genes.getAlleles().put(loc, alleles);
                }
                catch (Exception e)
                {

                    PokecubeMod.log(Level.SEVERE, "Error making gene: " + loc, e);
                }
            }
            else
            {
                PokecubeMod.log("Uknown Gene: " + loc);
            }
        }
        if (genes.getAlleles().isEmpty())
        {
            PokecubeMod.log(Level.SEVERE, "no dna here? " + arg);
            return stack;
        }
        ClonerHelper.setGenes(stack, genes);
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<MakeDnaBottle>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_dna"), MakeDnaBottle.class);
        }

        @Override
        public void serialize(JsonObject object, MakeDnaBottle functionClazz,
                JsonSerializationContext serializationContext)
        {
            for (ResourceLocation l : functionClazz.arg.keySet())
            {
                object.addProperty(l.toString(), functionClazz.arg.get(l));
            }
        }

        @Override
        public MakeDnaBottle deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                LootCondition[] conditionsIn)
        {
            Map<ResourceLocation, String> arg = Maps.newHashMap();
            Set<Entry<String, JsonElement>> entries = object.entrySet();
            for (Entry<String, JsonElement> element : entries)
            {
                ResourceLocation key = new ResourceLocation(element.getKey());
                arg.put(key, element.getValue().getAsString());
            }
            return new MakeDnaBottle(conditionsIn, arg);
        }
    }
}
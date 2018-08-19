package pokecube.adventures.blocks.cloner;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

public class ClonerHelper
{
    public static final String SELECTORTAG = "DNASelector";

    public static class DNAPack
    {
        public final Alleles alleles;
        public final float   chance;

        public DNAPack(Alleles alleles, float chance)
        {
            this.alleles = alleles;
            this.chance = chance;
        }
    }

    public static Map<ItemStack, DNAPack> DNAITEMS = Maps.newHashMap();

    public static void registerDNA(DNAPack entry, ItemStack stack)
    {
        DNAITEMS.put(stack, entry);
    }

    public static List<ItemStack> getStacks(TileClonerBase cloner)
    {
        if (cloner.getProcess() == null) return Lists.newArrayList();
        return Lists.newArrayList(cloner.getProcess().recipe.getRemainingItems(cloner.getCraftMatrix()));
    }

    public static IMobGenetics getGenes(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return null;
        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey(GeneticsManager.GENES))
        {
            if (PokecubeManager.isFilled(stack))
            {
                NBTTagCompound poketag = nbt.getCompoundTag(TagNames.POKEMOB);
                if (!poketag.getCompoundTag("ForgeCaps").hasKey(GeneticsManager.POKECUBEGENETICS.toString()))
                    return null;
                if (!poketag.getCompoundTag("ForgeCaps").getCompoundTag(GeneticsManager.POKECUBEGENETICS.toString())
                        .hasKey("V"))
                    return null;
                NBTBase genes = poketag.getCompoundTag("ForgeCaps")
                        .getCompoundTag(GeneticsManager.POKECUBEGENETICS.toString()).getTag("V");
                IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
                IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, eggs, null, genes);
                return eggs;
            }
            return null;
        }
        NBTBase genes = nbt.getTag(GeneticsManager.GENES);
        IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
        IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, eggs, null, genes);
        return eggs;
    }

    public static void setGenes(ItemStack stack, IMobGenetics genes)
    {
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return;
        NBTTagCompound nbt = stack.getTagCompound();
        NBTBase geneTag = IMobGenetics.GENETICS_CAP.getStorage().writeNBT(IMobGenetics.GENETICS_CAP, genes, null);
        if (PokecubeManager.isFilled(stack))
        {
            NBTTagCompound poketag = nbt.getCompoundTag(TagNames.POKEMOB);
            poketag.getCompoundTag("ForgeCaps").getCompoundTag(GeneticsManager.POKECUBEGENETICS.toString()).setTag("V",
                    geneTag);
        }
        else
        {
            nbt.setTag(GeneticsManager.GENES, geneTag);
        }
    }

    public static PokedexEntry getFromGenes(ItemStack stack)
    {
        IMobGenetics genes = getGenes(stack);
        if (genes == null) return null;
        Alleles gene = genes.getAlleles().get(GeneticsManager.SPECIESGENE);
        if (gene != null)
        {
            SpeciesInfo info = gene.getExpressed().getValue();
            return info.entry;
        }
        return null;
    }

    public static boolean isDNAContainer(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return false;
        String potion = stack.getTagCompound().getString("Potion");
        return potion.equals("minecraft:water") || potion.equals("minecraft:mundane");
    }

    public static SelectorValue getSelectorValue(ItemStack selector)
    {
        SelectorValue def = RecipeSelector.getSelectorValue(selector);
        if (!CompatWrapper.isValid(selector) || !selector.hasTagCompound()) return def;
        NBTTagCompound selectorTag = selector.getTagCompound().getCompoundTag(SELECTORTAG);
        return SelectorValue.load(selectorTag);
    }

    public static int getIndex(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return -1;
        if (!stack.getDisplayName().startsWith("Selector")) return -1;
        if (stack.getTagCompound().hasKey("pages") && stack.getTagCompound().getTag("pages") instanceof NBTTagList)
        {
            NBTTagList pages = (NBTTagList) stack.getTagCompound().getTag("pages");
            try
            {
                ITextComponent comp = ITextComponent.Serializer.jsonToComponent(pages.getStringTagAt(0));
                for (String line : comp.getUnformattedText().split("\n"))
                {
                    if (line.equalsIgnoreCase("ALL")) { return -1; }
                    String[] args = line.split("#");
                    if (args.length == 2) { return Integer.parseInt(args[1]); }
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error checking index for " + stack + " " + stack.getTagCompound(), e);
            }
        }
        return -1;
    }

    public static Set<Class<? extends Gene>> getGeneSelectors(ItemStack stack)
    {
        Set<Class<? extends Gene>> ret = Sets.newHashSet();
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return ret;
        if (!stack.getDisplayName().startsWith("Selector")) return ret;
        if (stack.getTagCompound().hasKey("pages") && stack.getTagCompound().getTag("pages") instanceof NBTTagList)
        {
            NBTTagList pages = (NBTTagList) stack.getTagCompound().getTag("pages");
            try
            {
                ITextComponent comp = ITextComponent.Serializer.jsonToComponent(pages.getStringTagAt(0));
                for (String line : comp.getUnformattedText().split("\n"))
                {
                    if (line.equalsIgnoreCase("ALL"))
                    {
                        ret.addAll(GeneRegistry.getGenes());
                        break;
                    }
                    Class<? extends Gene> geneClass = getGene(line);
                    if (geneClass != null)
                    {
                        ret.add(geneClass);
                    }
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error locating selectors for " + stack + " " + stack.getTagCompound(),
                        e);
            }
        }
        return ret;
    }

    public static Class<? extends Gene> getGene(String line)
    {
        String[] args = line.split(":");
        String domain = "pokecube";
        String path = "";
        if (args.length == 1) path = args[0].toLowerCase(Locale.ENGLISH);
        else
        {
            domain = args[0];
            path = args[1].toLowerCase(Locale.ENGLISH);
        }
        path = path.split("#")[0];
        ResourceLocation location = new ResourceLocation(domain, path);
        Class<? extends Gene> geneClass = GeneRegistry.getClass(location);
        return geneClass;
    }

    public static float destroyChance(ItemStack selector)
    {
        if (!CompatWrapper.isValid(selector) || !selector.hasTagCompound()) return 1;
        if (!selector.getDisplayName().startsWith("Selector")) return 1;
        return 0;
    }

    public static void mergeGenes(IMobGenetics genesIn, ItemStack destination, IGeneSelector selector)
    {
        IMobGenetics eggs = getGenes(destination);
        if (eggs == null)
        {
            eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
        }
        for (Map.Entry<ResourceLocation, Alleles> entry : genesIn.getAlleles().entrySet())
        {
            ResourceLocation loc = entry.getKey();
            Alleles alleles = entry.getValue();
            Alleles eggsAllele = eggs.getAlleles().get(loc);
            eggsAllele = selector.merge(alleles, eggsAllele);
            if (eggsAllele != null)
            {
                eggs.getAlleles().put(loc, eggsAllele);
            }
        }
        setGenes(destination, eggs);
    }

    public static void spliceGenes(IMobGenetics genesIn, ItemStack destination, IGeneSelector selector)
    {
        IMobGenetics eggs = getGenes(destination);
        if (eggs == null)
        {
            eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
        }
        for (Map.Entry<ResourceLocation, Alleles> entry : genesIn.getAlleles().entrySet())
        {
            ResourceLocation loc = entry.getKey();
            Alleles alleles = entry.getValue();
            Alleles eggsAllele = eggs.getAlleles().get(loc);
            alleles = selector.merge(alleles, eggsAllele);
            if (alleles != null) eggs.getAlleles().put(loc, alleles);
        }
        setGenes(destination, eggs);
    }
}

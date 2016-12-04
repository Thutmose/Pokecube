package pokecube.adventures.blocks.cloner;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

public class ClonerHelper
{
    public static List<ItemStack> getStacks(TileEntityCloner cloner)
    {
        if (cloner.currentProcess == null) return Lists.newArrayList();
        return Lists.newArrayList(cloner.currentProcess.recipe.getRemainingItems(cloner.getCraftMatrix()));
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

    public static boolean isDNAContainer(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || !stack.hasTagCompound()) return false;
        return stack.getTagCompound().getString("Potion").equals("minecraft:water");
    }

    public static Set<Class<? extends Gene>> getGeneSelectors(ItemStack stack)
    {
        Set<Class<? extends Gene>> ret = Sets.newHashSet();
        return ret;
    }

    public static float destroyChance(ItemStack selector)
    {
        return 1;
    }

    public static void mergeGenes(ItemStack genesIn, ItemStack egg)
    {

    }
}

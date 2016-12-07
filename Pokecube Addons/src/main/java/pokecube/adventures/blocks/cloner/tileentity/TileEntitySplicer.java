package pokecube.adventures.blocks.cloner.tileentity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntitySplicer extends TileClonerBase implements SimpleComponent
{
    public TileEntitySplicer()
    {
        /** 1 slot for egg, 1 slot for gene container,1 slot for output, 1 slot
         * for stabiliser. */
        super(4, 3);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getGenes(stack) != null;
        case 1:// DNA Selector
            boolean hasGenes = !ClonerHelper.getGeneSelectors(stack).isEmpty();
            boolean selector = hasGenes || RecipeSelector.getSelectorValue(stack) != RecipeSelector.defaultSelector;
            return hasGenes || selector;
        case 2:// DNA Destination
            return ItemPokemobEgg.getEntry(stack) != null;
        }
        return false;
    }

    @Override
    public String getName()
    {
        return "splicer";
    }

    @Override
    public CraftMatrix getCraftMatrix()
    {
        if (craftMatrix == null) this.craftMatrix = new CraftMatrix(null, this, 1, 3);
        return craftMatrix;
    }

    @Override
    public boolean isValid(Class<? extends IPoweredRecipe> recipe)
    {
        return recipe == RecipeSplice.class;
    }

    @Override
    public String getComponentName()
    {
        return "dnaSplicer";
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] getSourceInfo(Context context, Arguments args) throws Exception
    {
        IMobGenetics genes = ClonerHelper.getGenes(getStackInSlot(2));
        if (genes == null) throw new Exception("No Genes found in source slot.");
        List<String> values = Lists.newArrayList();
        for (ResourceLocation l : genes.getAlleles().keySet())
        {
            Alleles a = genes.getAlleles().get(l);
            Gene expressed = a.getExpressed();
            Gene parent1 = a.getAlleles()[0];
            Gene parent2 = a.getAlleles()[1];
            values.add(l.getResourcePath());
            values.add(expressed.toString());
            values.add(parent1.toString());
            values.add(parent2.toString());
        }
        return values.toArray(new String[0]);
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] getDestInfo(Context context, Arguments args) throws Exception
    {
        IMobGenetics genes = ClonerHelper.getGenes(getStackInSlot(0));
        if (genes == null) throw new Exception("No Genes found in destination slot.");
        List<String> values = Lists.newArrayList();
        for (ResourceLocation l : genes.getAlleles().keySet())
        {
            Alleles a = genes.getAlleles().get(l);
            Gene expressed = a.getExpressed();
            Gene parent1 = a.getAlleles()[0];
            Gene parent2 = a.getAlleles()[1];
            values.add(l.getResourcePath());
            values.add(expressed.toString());
            values.add(parent1.toString());
            values.add(parent2.toString());
        }
        return values.toArray(new String[0]);
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] getSelectorInfo(Context context, Arguments args) throws Exception
    {
        ItemStack selector = getStackInSlot(1);
        Set<Class<? extends Gene>> getSelectors = ClonerHelper.getGeneSelectors(selector);
        if (getSelectors.isEmpty()) throw new Exception("No Selector found.");
        List<String> values = Lists.newArrayList();
        for (Class<? extends Gene> geneC : getSelectors)
        {
            try
            {
                Gene gene = geneC.newInstance();
                values.add(gene.getKey().getResourcePath());
            }
            catch (InstantiationException | IllegalAccessException e)
            {
            }
        }
        SelectorValue value = ClonerHelper.getSelectorValue(selector);
        values.add(value.toString());
        return values.toArray(new String[0]);
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] splice(Context context, Arguments args) throws Exception
    {
        return null;
    }
}

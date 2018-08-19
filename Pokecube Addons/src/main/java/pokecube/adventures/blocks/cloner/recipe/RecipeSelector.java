package pokecube.adventures.blocks.cloner.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.IGeneSelector;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeSelector implements IDefaultRecipe
{
    public static class ItemBasedSelector implements IGeneSelector
    {
        final ItemStack selector;
        final int       arrIndex;

        public ItemBasedSelector(ItemStack selector)
        {
            this(selector, ClonerHelper.getIndex(selector));
        }

        public ItemBasedSelector(ItemStack selector, int arrIndex)
        {
            this.selector = selector;
            this.arrIndex = arrIndex;
        }

        @Override
        public Alleles merge(Alleles source, Alleles destination)
        {
            Set<Class<? extends Gene>> selected = ClonerHelper.getGeneSelectors(selector);
            if (selected.contains(source.getExpressed().getClass()))
            {
                if (destination == null) return source;
                return IGeneSelector.super.merge(source, destination);
            }
            return null;
        }

        @Override
        public int arrIndex()
        {
            return arrIndex;
        }
    }

    public static class SelectorValue
    {
        public final float selectorDestructChance;
        public final float dnaDestructChance;

        public SelectorValue(float select, float dna)
        {
            this.selectorDestructChance = select;
            this.dnaDestructChance = dna;
        }

        @Override
        public String toString()
        {
            return selectorDestructChance + " " + dnaDestructChance;
        }

        public NBTTagCompound save()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setFloat("S", selectorDestructChance);
            tag.setFloat("D", dnaDestructChance);
            return tag;
        }

        public static SelectorValue load(NBTTagCompound tag)
        {
            if (!tag.hasKey("S") || !tag.hasKey("D")) return defaultSelector;
            return new SelectorValue(tag.getFloat("S"), tag.getFloat("D"));
        }

        @SideOnly(Side.CLIENT)
        public void addToTooltip(List<String> toolTip)
        {
            toolTip.add(I18n.format("container.geneselector.tooltip.a", selectorDestructChance));
            toolTip.add(I18n.format("container.geneselector.tooltip.b", dnaDestructChance));
        }
    }

    public static SelectorValue defaultSelector = new SelectorValue(0.0f, 0.9f);

    public static SelectorValue getSelectorValue(ItemStack stack)
    {
        SelectorValue value = defaultSelector;
        if (CompatWrapper.isValid(stack)) for (ItemStack stack1 : selectorValues.keySet())
        {
            if (Tools.isSameStack(stack1, stack))
            {
                value = selectorValues.get(stack1);
                break;
            }
        }
        return value;
    }

    public static boolean isSelector(ItemStack stack)
    {
        if (!ClonerHelper.getGeneSelectors(stack).isEmpty()) return true;
        if (CompatWrapper.isValid(stack)) for (ItemStack stack1 : selectorValues.keySet())
        {
            if (Tools.isSameStack(stack1, stack)) { return true; }
        }
        return false;
    }

    private static Map<ItemStack, SelectorValue> selectorValues = Maps.newHashMap();

    public static void addSelector(ItemStack stack, SelectorValue value)
    {
        selectorValues.put(stack, value);
    }

    ItemStack output = ItemStack.EMPTY;

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if (inv.getSizeInventory() < getRecipeSize()) return false;
        ItemStack book = inv.getStackInSlot(0);
        ItemStack modifier = inv.getStackInSlot(1);
        if (ClonerHelper.getGeneSelectors(book).isEmpty() || modifier.isEmpty()) return false;
        SelectorValue value = getSelectorValue(modifier);
        if (value == defaultSelector) return false;
        output = book.copy();
        output.setCount(1);
        output.getTagCompound().setTag(ClonerHelper.SELECTORTAG, value.save());
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

}

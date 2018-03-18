package pokecube.core.items.megastuff;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.PokecubeItems;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeWearables implements IDefaultRecipe
{
    private ItemStack                        output   = CompatWrapper.nullStack;

    public static List<Predicate<ItemStack>> dyeables = Lists.newArrayList();

    static
    {
        dyeables.add(new Predicate<ItemStack>()
        {
            @Override
            public boolean test(ItemStack t)
            {
                return t.getItem() instanceof ItemMegawearable
                        // Is pokewatch.
                        || t.getItem() == PokecubeItems.pokedex && t.getItemDamage() == 8;
            }
        });
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = CompatWrapper.nullStack;
        boolean ring = false;
        boolean dye = false;
        ItemStack dyeStack = CompatWrapper.nullStack;
        ItemStack ringStack = CompatWrapper.nullStack;
        inventory:
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                for (Predicate<ItemStack> c : dyeables)
                {
                    if (c.test(stack))
                    {
                        ring = true;
                        ringStack = stack;
                        continue inventory;
                    }
                }
                List<ItemStack> dyes = OreDictionary.getOres("dye");
                boolean isDye = false;
                for (ItemStack dye1 : dyes)
                {
                    if (OreDictionary.itemMatches(dye1, stack, false))
                    {
                        isDye = true;
                        break;
                    }
                }
                if (isDye)
                {
                    dye = true;
                    dyeStack = stack;
                }
            }
        }
        if (dye && ring)
        {
            output = ringStack.copy();
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            int[] ids = OreDictionary.getOreIDs(dyeStack);
            int colour = dyeStack.getItemDamage();
            for (int i : ids)
            {
                String name = OreDictionary.getOreName(i);
                if (name.startsWith("dye") && name.length() > 3)
                {
                    String val = name.replace("dye", "").toUpperCase(Locale.ENGLISH);
                    try
                    {
                        EnumDyeColor type = EnumDyeColor.valueOf(val);
                        colour = type.getDyeDamage();
                        break;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            output.getTagCompound().setInteger("dyeColour", colour);
        }
        return CompatWrapper.isValid(output);
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
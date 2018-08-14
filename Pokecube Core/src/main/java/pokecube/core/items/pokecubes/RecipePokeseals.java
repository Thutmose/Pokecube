package pokecube.core.items.pokecubes;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.utils.TagNames;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipePokeseals implements IDefaultRecipe
{
    private ItemStack toCraft = ItemStack.EMPTY;
    // private static final String __OBFID = "CL_00000083";

    /** Returns an Item that is the result of this recipe */
    @Override
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
    {
        return this.toCraft;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.toCraft;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @SuppressWarnings({ "unused", "rawtypes" })
    @Override
    public boolean matches(InventoryCrafting craft, World world)
    {
        this.toCraft = ItemStack.EMPTY;
        int cube = 0;
        int paper = 0;
        int gunpowder = 0;
        int dye = 0;
        int fireworkcharge = 0;
        int sparklystuff = 0;
        int boomboomstuff = 0;
        int addons = 0;

        for (int k1 = 0; k1 < craft.getSizeInventory(); ++k1)
        {
            ItemStack itemstack = craft.getStackInSlot(k1);

            if (CompatWrapper.isValid(itemstack))
            {
                if (itemstack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)
                        && PokecubeManager.isFilled(itemstack) == false)
                {
                    ++cube;
                    toCraft = itemstack.copy();
                }
                /*
                 * if (itemstack.getItem() == Items.gunpowder) { ++j; } else if
                 * (itemstack.getItem() == Items.firework_charge) { ++l; }
                 */ else if (itemstack.getItem() == Items.DYE)
                {
                    ++addons;
                }
                /*
                 * else if (itemstack.getItem() == Items.paper) { ++paper; }
                 */ else if (itemstack.getItem() == Items.WATER_BUCKET)
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Items.COAL)
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Item.getItemFromBlock(Blocks.LEAVES))
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Items.FEATHER)
                {
                    ++boomboomstuff;
                }
                else if (itemstack.getItem() == Items.GOLD_NUGGET)
                {
                    ++boomboomstuff;
                }
                else
                {
                    if (itemstack.getItem() != Items.SKULL) { return false; }

                    ++boomboomstuff;
                }
            }
        }

        sparklystuff += dye + boomboomstuff;

        NBTTagCompound nbttagcompound;
        NBTTagCompound nbttagcompound1;

        if (cube == 1 && addons > 0)
        {
            toCraft = new ItemStack(PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL), 1);
            nbttagcompound = new NBTTagCompound();
            nbttagcompound1 = new NBTTagCompound();
            byte b0 = 0;
            ArrayList arraylist = new ArrayList();

            for (int l1 = 0; l1 < craft.getSizeInventory(); ++l1)
            {
                ItemStack itemstack2 = craft.getStackInSlot(l1);

                if (CompatWrapper.isValid(itemstack2))
                {
                    if (itemstack2.getItem() == Items.COAL)
                    {
                        nbttagcompound1.setBoolean("Flames", true);
                    }
                    if (itemstack2.getItem() == Items.WATER_BUCKET)
                    {
                        nbttagcompound1.setBoolean("Bubbles", true);
                    }
                    if (itemstack2.getItem() == Item.getItemFromBlock(Blocks.LEAVES))
                    {
                        nbttagcompound1.setBoolean("Leaves", true);
                    }
                    if (itemstack2.getItem() == Items.DYE)
                    {
                        nbttagcompound1.setInteger("dye", itemstack2.getItemDamage());
                    }

                }
            }

            int[] aint1 = new int[arraylist.size()];

            for (int l2 = 0; l2 < aint1.length; ++l2)
            {
                aint1[l2] = ((Integer) arraylist.get(l2)).intValue();
            }
            nbttagcompound.setTag(TagNames.POKESEAL, nbttagcompound1);
            this.toCraft.setTagCompound(nbttagcompound);
            return true;
        }
        return false;
    }

    public static ItemStack process(ItemStack cube, ItemStack seal)
    {
        if (!seal.hasTagCompound()) return cube;
        NBTTagCompound pokecubeTag = TagNames.getPokecubePokemobTag(cube.getTagCompound())
                .getCompoundTag(TagNames.VISUALSTAG).getCompoundTag(TagNames.POKECUBE);
        if (!pokecubeTag.hasKey("tag")) pokecubeTag.setTag("tag", new NBTTagCompound());
        NBTTagCompound cubeTag = pokecubeTag.getCompoundTag("tag");
        cubeTag.setTag(TagNames.POKESEAL, seal.getTagCompound().getCompoundTag(TagNames.POKESEAL));
        return cube;
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

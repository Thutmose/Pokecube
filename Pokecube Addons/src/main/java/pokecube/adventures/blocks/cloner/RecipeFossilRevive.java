package pokecube.adventures.blocks.cloner;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class RecipeFossilRevive extends ShapelessRecipes implements IClonerRecipe
{
    private static List<RecipeFossilRevive>                  recipeList = Lists.newArrayList();
    private static HashMap<PokedexEntry, RecipeFossilRevive> entryMap   = Maps.newHashMap();

    private static Comparator<RecipeFossilRevive>            comparator = new Comparator<RecipeFossilRevive>()
                                                                        {
                                                                            @Override
                                                                            public int compare(RecipeFossilRevive arg0,
                                                                                    RecipeFossilRevive arg1)
                                                                            {
                                                                                return arg1.priority - arg0.priority;
                                                                            }
                                                                        };

    public static List<RecipeFossilRevive> getRecipeList()
    {
        return Lists.newArrayList(recipeList);
    }

    public static void addRecipe(RecipeFossilRevive toAdd)
    {
        recipeList.add(toAdd);
        if (toAdd.pokedexEntry != null) entryMap.put(toAdd.pokedexEntry, toAdd);
        recipeList.sort(comparator);
    }

    public static RecipeFossilRevive getRecipe(PokedexEntry entry)
    {
        return entryMap.get(entry);
    }

    public PokedexEntry  pokedexEntry;
    public int           energyCost;
    public int           priority    = 0;
    public int           level       = 20;
    public List<Integer> remainIndex = Lists.newArrayList();
    public boolean       tame        = true;
    private boolean      splicer     = false;
    private IPokemob     pokemob;

    public RecipeFossilRevive(ItemStack output, List<ItemStack> inputList, PokedexEntry entry, int cost)
    {
        super(output, inputList);
        this.pokedexEntry = entry;
        this.energyCost = cost;
    }

    public RecipeFossilRevive setTame(boolean tame)
    {
        this.tame = tame;
        return this;
    }

    public RecipeFossilRevive setLevel(int level)
    {
        this.level = level;
        return this;
    }

    public IPokemob getPokemob()
    {
        if (pokemob == null && pokedexEntry != null)
        {
            pokemob = (IPokemob) PokecubeMod.core.createPokemob(pokedexEntry, null);
            if (pokemob == null)
            {
                this.pokedexEntry = null;
            }
            else
            {
                pokemob.setPokedexEntry(pokedexEntry);
            }
        }
        return pokemob;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        List<ItemStack> list = Lists.newArrayList(this.recipeItems);

        for (int i = 0; i < inv.getHeight(); ++i)
        {
            for (int j = 0; j < inv.getWidth(); ++j)
            {
                ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

                if (itemstack != null)
                {
                    boolean flag = false;

                    for (ItemStack itemstack1 : list)
                    {
                        boolean matches = false;
                        if (itemstack1.getMetadata() == 32767) matches = itemstack.getItem() == itemstack1.getItem();
                        else matches = Tools.isSameStack(itemstack, itemstack1);
                        if (matches)
                        {
                            flag = true;
                            list.remove(itemstack1);
                            break;
                        }
                    }
                    if (!flag) { return false; }
                }
            }
        }
        return list.isEmpty();
    }

    @Override
    public int getEnergyCost()
    {
        return energyCost;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        if (remainIndex.isEmpty()) return super.getRemainingItems(inv);
        NonNullList<ItemStack> aitemstack = ForgeHooks.defaultRecipeGetRemainingItems(inv);
        for (int i = 0; i < aitemstack.size(); ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);

            boolean remain = false;
            if (CompatWrapper.isValid(itemstack)) for (Integer i1 : remainIndex)
            {
                ItemStack stack = recipeItems.get(i1);
                if (stack.getMetadata() == 32767) remain = itemstack.getItem() == stack.getItem();
                else remain = Tools.isSameStack(itemstack, stack);
            }
            if (remain)
            {
                aitemstack.set(i, itemstack.copy());
            }
        }
        return aitemstack;
    }

    public RecipeFossilRevive setSplicer()
    {
        splicer = true;
        return this;
    }

    @Override
    public boolean splicerRecipe()
    {
        return splicer;
    }
}

package pokecube.adventures.blocks.cloner.recipe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.block.BlockReanimator;
import pokecube.adventures.commands.Config;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

public class RecipeFossilRevive implements IPoweredRecipe
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
        if (toAdd.getPokedexEntry() != null)
        {
            entryMap.put(toAdd.getPokedexEntry(), toAdd);
            recipeList.sort(comparator);
        }
    }

    public static RecipeFossilRevive getRecipe(PokedexEntry entry)
    {
        return entryMap.get(entry);
    }

    public static RecipeFossilRevive ANYMATCH    = new RecipeFossilRevive(Lists.newArrayList(), Database.missingno,
            Config.instance.fossilReanimateCost);

    private PokedexEntry             pokedexEntry;
    private PokedexEntry             actualEntry;
    public int                       energyCost;
    public int                       priority    = 0;
    public int                       level       = 20;
    public List<Integer>             remainIndex = Lists.newArrayList();
    public List<String>              neededGenes = Lists.newArrayList();
    public final List<ItemStack>     recipeItems;
    public boolean                   tame        = true;
    private IPokemob                 pokemob;

    public RecipeFossilRevive(List<ItemStack> inputList, PokedexEntry entry, int cost)
    {
        this.recipeItems = inputList;
        this.pokedexEntry = entry;
        actualEntry = entry;
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

    public PokedexEntry getPokedexEntry()
    {
        return actualEntry;
    }

    public IPokemob getPokemob()
    {
        if (pokemob == null && getPokedexEntry() != null)
        {
            pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(getPokedexEntry(), null));
            if (pokemob == null)
            {
                this.actualEntry = null;
            }
            else
            {
                pokemob.setPokedexEntry(getPokedexEntry());
            }
        }
        return pokemob;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if (inv.getSizeInventory() < getRecipeSize()) return false;
        ItemStack dna = inv.getStackInSlot(0);
        ItemStack egg = inv.getStackInSlot(1);
        if (!((CompatWrapper.isValid(egg) && CompatWrapper.isValid(dna)) || recipeItems.size() > 0)) return false;
        PokedexEntry entry = ClonerHelper.getFromGenes(dna);
        if (pokedexEntry == Database.missingno && entry != null)
        {
            tame = !entry.legendary;
            actualEntry = entry;
        }
        if ((entry != null && entry != getPokedexEntry()) || getPokedexEntry() == null) return false;
        List<ItemStack> list = Lists.newArrayList(recipeItems);
        for (int i = 2; i < inv.getSizeInventory(); i++)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(itemstack))
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
        return list.isEmpty();
    }

    @Override
    public int getEnergyCost()
    {
        return energyCost;
    }

    @Override
    public ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        boolean remain = false;
        if (CompatWrapper.isValid(stackIn))
        {
            for (Integer i1 : remainIndex)
            {
                ItemStack stack = recipeItems.get(i1).copy();
                if (stack.getMetadata() == 32767) remain = stackIn.getItem() == stack.getItem();
                else remain = Tools.isSameStack(stackIn, stack);
            }
        }
        if (!remain)
        {
            ItemStack stack = net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
            if (!CompatWrapper.isValid(stack))
            {
                if (CompatWrapper.isValid(stackIn)) stackIn.splitStack(1);
            }
            else stackIn = stack;
        }
        return stackIn;
    }

    @Override
    public boolean complete(IPoweredProgress tile)
    {
        ItemStack dnaSource = tile.getStackInSlot(0);
        if (CompatWrapper.isValid(dnaSource)) dnaSource = dnaSource.copy();
        List<ItemStack> remaining = Lists.newArrayList(getRemainingItems(tile.getCraftMatrix()));
        for (int i = 0; i < remaining.size(); i++)
        {
            ItemStack stack = remaining.get(i);
            if (CompatWrapper.isValid(stack)) tile.setInventorySlotContents(i, stack.copy());
            else tile.decrStackSize(i, 1);
        }
        tile.setInventorySlotContents(tile.getOutputSlot(), getRecipeOutput());
        World world = ((TileEntity) tile).getWorld();
        BlockPos pos = ((TileEntity) tile).getPos();
        EntityLiving entity = (EntityLiving) PokecubeMod.core.createPokemob(getPokedexEntry(), world);
        if (entity != null)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            entity.setHealth(entity.getMaxHealth());
            // to avoid the death on spawn
            int exp = Tools.levelToXp(getPokedexEntry().getEvolutionMode(), level);
            // that will make your pokemob around level 3-5.
            // You can give him more XP if you want
            entity = (pokemob = pokemob.setForSpawn(exp)).getEntity();
            if (tile.getUser() != null && tame) pokemob.setPokemonOwner(tile.getUser());
            EnumFacing dir = world.getBlockState(pos).getValue(BlockReanimator.FACING);
            entity.setLocationAndAngles(pos.getX() + 0.5 + dir.getFrontOffsetX(), pos.getY() + 1,
                    pos.getZ() + 0.5 + dir.getFrontOffsetZ(), world.rand.nextFloat() * 360F, 0.0F);
            world.spawnEntity(entity);
            IMobGenetics genes = ClonerHelper.getGenes(dnaSource);
            if (genes != null)
            {
                GeneticsManager.initFromGenes(genes, pokemob);
            }
            entity.playLivingSound();
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return ItemStack.EMPTY;
    }

    public int getRecipeSize()
    {
        return this.recipeItems.size();
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
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

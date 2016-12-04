package pokecube.adventures.blocks.cloner.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.core.database.Database;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.core.common.blocks.DefaultInventory;
import thut.lib.CompatWrapper;

public class TileEntitySplicer extends TileEntity implements DefaultInventory, IPoweredProgress
{
    public static int     MAXENERGY      = 256;
    public int            energy         = 0;
    public int            progress       = 0;
    public int            total          = 0;
    public PoweredProcess currentProcess = null;

    public TileEntitySplicer()
    {
        /** 1 slot for egg, 1 slot for gene container,1 slot for output, 1 slot
         * for stabiliser. */
        inventory = CompatWrapper.makeList(4);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getGenes(stack) != null;
        case 1:// DNA Selector
            return !ClonerHelper.getGeneSelectors(stack).isEmpty();
        case 2:// DNA Destination
            return ItemPokemobEgg.getEntry(stack) != null;
        }
        return false;
    }

    @Override
    public int getField(int id)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFieldCount()
    {
        // TODO Auto-generated method stub
        return 2;
    }

    @Override
    public String getName()
    {
        return "splicer";
    }

    final List<ItemStack> inventory;

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        NBTBase temp = nbt.getTag("Inventory");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, CompatWrapper.fromTag(tag));
                }
            }
        }
        if (nbt.hasKey("progress"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("progress");
            String entryName = tag.getString("entry");
            int needed = tag.getInteger("needed");
            RecipeFossilRevive recipe = RecipeFossilRevive.getRecipe(Database.getEntry(entryName));
            if (recipe != null)
            {
                currentProcess = new PoweredProcess(recipe, this);
                currentProcess.needed = needed;
                progress = needed;
                total = currentProcess.recipe.getEnergyCost();
            }
            else if (needed != 0)
            {
                currentProcess = new PoweredProcess(new RecipeSplice(), this);
                currentProcess.needed = needed;
                progress = needed;
                total = currentProcess.recipe.getEnergyCost();
            }
            if (currentProcess == null || !currentProcess.valid())
            {
                progress = 0;
                currentProcess = null;
                total = 0;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack;
            if (CompatWrapper.isValid(stack = inventory.get(i)))
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        if (currentProcess != null)
        {
            NBTTagCompound current = new NBTTagCompound();
            if (currentProcess.recipe instanceof RecipeFossilRevive)
                current.setString("entry", ((RecipeFossilRevive) currentProcess.recipe).pokedexEntry.getName());
            current.setInteger("needed", currentProcess.needed);
            nbt.setTag("progress", current);
        }
        nbt.setTag("Inventory", itemList);
        return nbt;
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public void setEnergy(int energy)
    {
        this.energy = energy;
    }

    @Override
    public int getProgress()
    {
        return progress;
    }

    @Override
    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    public CraftMatrix  craftMatrix;
    public EntityPlayer user;

    @Override
    public void setCraftMatrix(CraftMatrix matrix)
    {
        craftMatrix = matrix;
    }

    @Override
    public CraftMatrix getCraftMatrix()
    {
        if (craftMatrix == null) this.craftMatrix = new CraftMatrix(null, this, 3, 1);
        return craftMatrix;
    }

    @Override
    public EntityPlayer getUser()
    {
        return user;
    }
}

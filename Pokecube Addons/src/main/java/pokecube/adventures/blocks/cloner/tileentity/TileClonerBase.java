package pokecube.adventures.blocks.cloner.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import thut.lib.CompatWrapper;

public abstract class TileClonerBase extends TileEntity implements IPoweredProgress, ITickable
{
    final List<ItemStack>  inventory;
    final int              outputSlot;
    private boolean        check          = true;
    private int            progress       = 0;
    private int            total          = 0;
    private PoweredProcess currentProcess = null;
    protected CraftMatrix  craftMatrix;
    private EntityPlayer   user;

    public TileClonerBase(int size, int output)
    {
        inventory = CompatWrapper.makeList(size);
        this.outputSlot = output;
    }

    @Override
    public int getOutputSlot()
    {
        return outputSlot;
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        if (user == player) user = null;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
        if (user == null) user = player;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return user == null || user == player;
    }

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
    }

    @Override
    public int addEnergy(int energy, boolean simulate)
    {
        if (getProcess() == null || !getProcess().valid()) return 0;
        int num = Math.min(energy, getProcess().needed);
        if (!simulate)
        {
            getProcess().needed -= num;
        }
        return num;
    }

    @Override
    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    @Override
    public void setCraftMatrix(CraftMatrix matrix)
    {
        craftMatrix = matrix;
    }

    @Override
    public CraftMatrix getCraftMatrix()
    {
        if (craftMatrix == null) this.craftMatrix = new CraftMatrix(null, this, 3, 3);
        return craftMatrix;
    }

    @Override
    public EntityPlayer getUser()
    {
        return user;
    }

    @Override
    public PoweredProcess getProcess()
    {
        return currentProcess;
    }

    @Override
    public void setProcess(PoweredProcess process)
    {
        currentProcess = process;
    }

    @Override
    public int getField(int id)
    {
        return id == 0 ? progress : total;
    }

    @Override
    public void setField(int id, int value)
    {
        if (id == 0) progress = value;
        else if (id == 1) total = value;
    }

    @Override
    public int getFieldCount()
    {
        return 2;
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
            currentProcess = PoweredProcess.load(tag, this);
            if (currentProcess != null)
            {
                total = currentProcess.recipe.getEnergyCost();
            }
        }
    }

    public void checkRecipes()
    {
        if (currentProcess == null || !currentProcess.valid())
        {
            if (check)
            {
                check = false;
                currentProcess = new PoweredProcess();
                currentProcess.setTile(this);
                if (!currentProcess.valid())
                {
                    currentProcess = null;
                }
            }
            else currentProcess = null;
        }
        else
        {
            boolean valid = currentProcess.valid();
            boolean done = true;
            if (valid)
            {
                total = currentProcess.recipe.getEnergyCost();
                done = !currentProcess.tick();
            }
            if (!valid || done)
            {
                currentProcess = null;
                progress = 0;
                markDirty();
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
            nbt.setTag("progress", currentProcess.save());
        }
        nbt.setTag("Inventory", itemList);
        return nbt;
    }

    @Override
    public void update()
    {
        if (worldObj.isRemote) return;
        checkRecipes();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        check = true;
        if (!CompatWrapper.isValid(stack)) getInventory().set(index, CompatWrapper.nullStack);
        else getInventory().set(index, stack);
    }
}

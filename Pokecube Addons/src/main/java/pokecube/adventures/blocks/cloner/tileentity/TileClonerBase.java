package pokecube.adventures.blocks.cloner.tileentity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import thut.lib.CompatWrapper;

public abstract class TileClonerBase extends TileEntity implements IPoweredProgress, ITickable, ISidedInventory
{
    final List<ItemStack>  inventory;
    final int              outputSlot;
    private boolean        check          = true;
    private int            progress       = 0;
    private int            total          = 0;
    private PoweredProcess currentProcess = null;
    protected CraftMatrix  craftMatrix;
    private EntityPlayer   user;
    IItemHandler[]         wrappers       = new IItemHandler[6];

    public TileClonerBase(int size, int output)
    {
        inventory = NonNullList.<ItemStack> withSize(size, ItemStack.EMPTY);
        this.outputSlot = output;
        for (EnumFacing side : EnumFacing.VALUES)
        {
            wrappers[side.ordinal()] = new SidedInvWrapper(this, side);
        }
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
    public boolean isUsableByPlayer(EntityPlayer player)
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
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
        if (nbt.hasKey("progress"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("progress");
            setProcess(PoweredProcess.load(tag, this));
            if (getProcess() != null)
            {
                total = getProcess().recipe.getEnergyCost();
            }
        }
    }

    public void checkRecipes()
    {
        if (getProcess() == null || !getProcess().valid())
        {
            if (check)
            {
                check = false;
                if (getProcess() == null)
                {
                    setProcess(new PoweredProcess());
                }
                this.getProcess().setTile(this);
                this.getProcess().reset();
                if (!getProcess().valid())
                {
                    setProcess(null);
                }
            }
            else setProcess(null);
        }
        else
        {
            boolean valid = getProcess().valid();
            boolean done = true;
            if (valid)
            {
                total = getProcess().recipe.getEnergyCost();
                done = !getProcess().tick();
            }
            if (!valid || done)
            {
                setProcess(null);
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
        if (getProcess() != null)
        {
            nbt.setTag("progress", getProcess().save());
        }
        nbt.setTag("Inventory", itemList);
        return nbt;
    }

    @Override
    public void update()
    {
        if (world.isRemote) return;
        checkRecipes();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        check = true;
        if (!CompatWrapper.isValid(stack)) getInventory().set(index, ItemStack.EMPTY);
        else getInventory().set(index, stack);
    }

    int[] slots;

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (slots == null)
        {
            slots = new int[getSizeInventory()];
            for (int i = 0; i < slots.length; i++)
                slots[i] = i;
        }
        return slots;
    }

    @Override
    /** Returns true if automation can insert the given item in the given slot
     * from the given side. */
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    /** Returns true if automation can extract the given item in the given slot
     * from the given side. */
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return !isItemValidForSlot(index, stack);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(wrappers[facing.ordinal()]);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }
}

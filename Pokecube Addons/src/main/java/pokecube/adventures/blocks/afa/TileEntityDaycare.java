package pokecube.adventures.blocks.afa;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.comands.Config;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;

public class TileEntityDaycare extends TileEntityOwnable implements IInventory
{
    ItemStack[] inventory  = new ItemStack[3];
    int         range      = 4;
    double      multiplier = 1;

    public TileEntityDaycare()
    {
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void validate()
    {
        super.validate();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void applyExp(LivingUpdateEvent event)
    {
        if (worldObj == null) return;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            if (worldObj.isRemote)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
            }
            return;
        }
        int tickrate = Config.instance.daycareTicks;
        tickrate = Math.max(tickrate, 1);
        if (event.getEntity().ticksExisted % tickrate == 0 && event.getEntity() instanceof IPokemob)
        {
            double dist = event.getEntity().getDistanceSq(getPos());
            IPokemob pokemob = (IPokemob) event.getEntity();
            if (dist > range * range || pokemob.getLevel() == 100) return;
            if (!consumeShards(Config.instance.daycareCost, true))
            {
                event.getEntityLiving().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.25f, 1);
                return;
            }
            event.getEntityLiving().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 1);
            int exp = (int) (Config.instance.daycareExp * multiplier / dist);
            pokemob.setExp(pokemob.getExp() + exp, true);
        }
    }

    private boolean consumeShards(int shards, boolean apply)
    {
        int numShard = 0;
        int numEmerald = 0;
        int numBlocks = 0;
        boolean enough;
        ItemStack stack;
        if ((stack = getStackInSlot(0)) != null)
        {
            numShard = stack.stackSize;
        }
        if ((stack = getStackInSlot(1)) != null)
        {
            numEmerald = stack.stackSize;
        }
        if ((stack = getStackInSlot(2)) != null)
        {
            numBlocks = stack.stackSize;
        }
        if (numBlocks != 0 && numEmerald < 9)
        {
            numBlocks--;
            numEmerald += 9;
            decrStackSize(2, 1);
            if ((stack = getStackInSlot(1)) != null)
            {
                stack.stackSize += 9;
            }
            else
            {
                stack = new ItemStack(Items.EMERALD, 9, 0);
                setInventorySlotContents(1, stack);
            }
        }
        if (numEmerald != 0 && numShard < 9)
        {
            numEmerald--;
            numShard += 9;
            decrStackSize(1, 1);
            if ((stack = getStackInSlot(0)) != null)
            {
                stack.stackSize += 9;
            }
            else
            {
                stack = PokecubeItems.getStack("emerald_shard");
                stack.stackSize = 9;
                setInventorySlotContents(0, stack);
            }
        }
        if (apply)
        {
            if (shards <= numShard)
            {
                decrStackSize(0, shards);
                return true;
            }
            int num = numShard + numEmerald * 9;
            if (shards <= (num = numShard + numEmerald * 9))
            {
                int remainder = num - shards;
                if (remainder <= 9)
                {
                    if (remainder <= 0)
                    {
                        setInventorySlotContents(0, null);
                        setInventorySlotContents(1, null);
                        return true;
                    }
                    stack = PokecubeItems.getStack("emerald_shard");
                    stack.stackSize = remainder;
                    setInventorySlotContents(0, stack);
                    setInventorySlotContents(1, null);
                    return true;
                }
                numShard = remainder % 9;
                numEmerald = remainder / 9;
                stack = new ItemStack(Items.EMERALD, numEmerald, 0);
                if (numEmerald > 0) setInventorySlotContents(1, stack);
                stack = PokecubeItems.getStack("emerald_shard");
                stack.stackSize = numShard;
                if (numShard > 0) setInventorySlotContents(0, stack);
                return true;
            }
            if (shards <= (num = numShard + numEmerald * 9 + numBlocks * 81))
            {
                int remainder = num - shards;
                if (remainder <= 9)
                {
                    if (remainder <= 0)
                    {
                        setInventorySlotContents(0, null);
                        setInventorySlotContents(1, null);
                        setInventorySlotContents(2, null);
                        return true;
                    }
                    stack = PokecubeItems.getStack("emerald_shard");
                    stack.stackSize = remainder;
                    setInventorySlotContents(0, stack);
                    setInventorySlotContents(1, null);
                    setInventorySlotContents(2, null);
                    return true;
                }
                numShard = remainder % 9;
                numEmerald = (remainder / 9) % 9;
                numBlocks = remainder / 81;
                stack = new ItemStack(Items.EMERALD, numEmerald, 0);
                if (numEmerald > 0) setInventorySlotContents(1, stack);
                stack = PokecubeItems.getStack("emerald_shard");
                stack.stackSize = numShard;
                if (numShard > 0) setInventorySlotContents(0, stack);
                stack = new ItemStack(Blocks.EMERALD_BLOCK, numBlocks, 0);
                if (numBlocks > 0) setInventorySlotContents(2, stack);
            }
            return false;
        }
        int total = numShard + numEmerald * 9 + numBlocks * 81;
        enough = total <= shards;
        return enough;
    }

    @Override
    public String getName()
    {
        return "daycare";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString("daycare");
    }

    @Override
    public int getSizeInventory()
    {
        return 3;
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
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

                if (slot >= 0 && slot < inventory.length)
                {
                    inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
            }
        }
        range = nbt.getInteger("distance");
        multiplier = nbt.getDouble("multiplier");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Inventory", itemList);
        nbt.setInteger("distance", range);
        nbt.setDouble("multiplier", multiplier);
        return nbt;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (this.inventory[slot] != null)
        {
            ItemStack itemStack;
            itemStack = inventory[slot].splitStack(count);
            if (inventory[slot].stackSize <= 0)
            {
                inventory[slot] = null;
            }
            return itemStack;
        }
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (inventory[slot] != null)
        {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (stack == null || stack.stackSize <= 0) inventory[index] = null;
        else inventory[index] = stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return player.getUniqueID().equals(placer);
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if (index == 0) return Tools.isSameStack(stack, PokecubeItems.getStack("emerald_shard"));
        if (index == 1) return Tools.isSameStack(stack, new ItemStack(Items.EMERALD));
        if (index == 2) return Tools.isSameStack(stack, new ItemStack(Blocks.EMERALD_BLOCK));
        return false;
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return range;
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        if (id == 0) range = value;
    }

    @Override
    public int getFieldCount()
    {
        return 1;
    }

    @Override
    public void clear()
    {
    }
}

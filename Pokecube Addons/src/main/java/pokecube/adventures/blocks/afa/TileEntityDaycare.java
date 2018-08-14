package pokecube.adventures.blocks.afa;

import java.util.List;

import net.minecraft.entity.EntityLiving;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.commands.Config;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class TileEntityDaycare extends TileEntityOwnable implements IInventory, ITickable
{
    List<ItemStack>       inventory  = NonNullList.<ItemStack> withSize(3, ItemStack.EMPTY);
    int                   range      = 4;
    private int           tick       = 0;
    private AxisAlignedBB box;
    double                multiplier = 1;

    public TileEntityDaycare()
    {
    }

    private boolean consumeShards(int shards, boolean apply)
    {
        int numShard = 0;
        int numEmerald = 0;
        int numBlocks = 0;
        boolean enough;
        int size = 0;
        ItemStack stack;
        if (!(stack = getStackInSlot(0)).isEmpty())
        {
            size = stack.getCount();
            numShard = size;
        }
        if (!(stack = getStackInSlot(1)).isEmpty())
        {
            size = stack.getCount();
            numEmerald = size;
        }
        if (!(stack = getStackInSlot(2)).isEmpty())
        {
            size = stack.getCount();
            numBlocks = size;
        }
        if (numBlocks != 0 && numEmerald < 9)
        {
            numBlocks--;
            numEmerald += 9;
            decrStackSize(2, 1);
            if (!(stack = getStackInSlot(1)).isEmpty())
            {
                size = stack.getCount();
                stack.setCount(size + 9);
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
            if (!(stack = getStackInSlot(0)).isEmpty())
            {
                size = stack.getCount();
                stack.setCount(size + 9);
            }
            else
            {
                stack = PokecubeItems.getStack("emerald_shard");
                stack.setCount(9);
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
                        setInventorySlotContents(0, ItemStack.EMPTY);
                        setInventorySlotContents(1, ItemStack.EMPTY);
                        return true;
                    }
                    stack = PokecubeItems.getStack("emerald_shard");
                    stack.setCount(remainder);
                    setInventorySlotContents(0, stack);
                    setInventorySlotContents(1, ItemStack.EMPTY);
                    return true;
                }
                numShard = remainder % 9;
                numEmerald = remainder / 9;
                stack = new ItemStack(Items.EMERALD, numEmerald, 0);
                if (numEmerald > 0) setInventorySlotContents(1, stack);
                stack = PokecubeItems.getStack("emerald_shard");
                stack.setCount(numShard);
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
                        setInventorySlotContents(0, ItemStack.EMPTY);
                        setInventorySlotContents(1, ItemStack.EMPTY);
                        setInventorySlotContents(2, ItemStack.EMPTY);
                        return true;
                    }
                    stack = PokecubeItems.getStack("emerald_shard");
                    stack.setCount(remainder);
                    setInventorySlotContents(0, stack);
                    setInventorySlotContents(1, ItemStack.EMPTY);
                    setInventorySlotContents(2, ItemStack.EMPTY);
                    return true;
                }
                numShard = remainder % 9;
                numEmerald = (remainder / 9) % 9;
                numBlocks = remainder / 81;
                stack = new ItemStack(Items.EMERALD, numEmerald, 0);
                if (numEmerald > 0) setInventorySlotContents(1, stack);
                stack = PokecubeItems.getStack("emerald_shard");
                stack.setCount(numShard);
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

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
        range = nbt.getInteger("distance");
        tick = nbt.getInteger("tick");
        multiplier = nbt.getDouble("multiplier");
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
        nbt.setTag("Inventory", itemList);
        nbt.setInteger("distance", range);
        nbt.setInteger("tick", tick);
        nbt.setDouble("multiplier", multiplier);
        return nbt;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < inventory.size(); i++)
            inventory.set(i, ItemStack.EMPTY);
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (CompatWrapper.isValid(inventory.get(slot)))
        {
            ItemStack itemStack;
            itemStack = inventory.get(slot).splitStack(count);
            if (!CompatWrapper.isValid(inventory.get(slot)))
            {
                inventory.set(slot, ItemStack.EMPTY);
            }
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(inventory.get(slot)))
        {
            ItemStack stack = inventory.get(slot);
            inventory.set(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) inventory.set(index, ItemStack.EMPTY);
        inventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
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

    // 1.11
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public void update()
    {
        if (world == null || tick++ < Config.instance.daycareTicks) return;
        tick = 0;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) { return; }
        if (box == null) box = new AxisAlignedBB(getPos()).grow(range + 2);
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, box);
        for (EntityLiving entity : mobs)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null)
            {
                double dist = entity.getDistanceSq(getPos());
                if (dist > range * range || pokemob.getLevel() == 100) return;
                if (!consumeShards(Config.instance.daycareCost, true))
                {
                    entity.playSound(SoundEvents.BLOCK_NOTE_BASS, 0.25f, 1);
                    return;
                }
                entity.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 1);
                int exp = (int) (Config.instance.daycareExp * multiplier / dist);
                pokemob.setExp(pokemob.getExp() + exp, true);
            }
        }
    }
}

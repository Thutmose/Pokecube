package pokecube.adventures.blocks.afa;

import cofh.api.energy.TileEnergyHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokecubeSerializer;

public class TileEntityAFA extends TileEnergyHandler implements IInventory, ITickable
{
    public IPokemob     pokemob   = null;
    private ItemStack[] inventory = new ItemStack[1];
    public Ability      ability   = null;;
    int                 distance  = 4;
    boolean             noEnergy  = false;

    public TileEntityAFA()
    {
    }

    @Override
    public void update()
    {
        if (inventory[0] != null && pokemob == null)
        {
            refreshAbility();
        }
        else if (inventory[0] == null)
        {
            refreshAbility();
        }
        if (pokemob != null && ability != null)
        {
            //Tick increase incase ability tracks this for update.
            //Renderer can also then render it animated.
            ((Entity) pokemob).ticksExisted++;
            if (!noEnergy)
            {
                int level = pokemob.getLevel();
                int needed = (int) Math.ceil(distance * distance * distance / ((double) level));
                int energy = extractEnergy(EnumFacing.DOWN, needed, false);
                if (energy < needed) return;
            }
            //Do not call ability update on client.
            if (!worldObj.isRemote) ability.onUpdate(pokemob);
        }
    }

    public void refreshAbility()
    {
        if (pokemob != null)
        {
            ((Entity) pokemob).setDead();
            pokemob = null;
            ability = null;
        }
        if (ability != null) ability.destroy();
        if (inventory[0] == null) return;
        if (ability != null)
        {
            ability.destroy();
            ability = null;
        }
        pokemob = PokecubeManager.itemToPokemob(inventory[0], getWorld());
        if (pokemob != null && pokemob.getMoveStats().ability != null)
        {
            ability = pokemob.getMoveStats().ability;
            ability.destroy();
            ((Entity) pokemob).setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            ability.init(pokemob, distance);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
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
        nbt.setInteger("distance", distance);
        nbt.setBoolean("noEnergy", noEnergy);
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
        distance = nbt.getInteger("distance");
        noEnergy = nbt.getBoolean("noEnergy");
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    @Override
    public String getName()
    {
        return "AFA";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return new ChatComponentText("Ability Field Amplifier");
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (inventory[index] != null && inventory[index].stackSize <= 0) inventory[index] = null;

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
                pokemob = null;
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
            pokemob = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (stack == null || stack.stackSize <= 0) inventory[index] = null;
        else inventory[index] = stack;
        refreshAbility();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
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
        return PokecubeManager.isFilled(stack);
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return storage.getEnergyStored();
        if (id == 1) return distance;
        if (id == 2) return noEnergy ? 1 : 0;

        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        if (id == 0) storage.setEnergyStored(value);
        if (id == 1) distance = value;
        if (id == 2) noEnergy = value != 0;
        distance = Math.max(0, distance);
        refreshAbility();
    }

    @Override
    public int getFieldCount()
    {
        return 3;
    }

    @Override
    public void clear()
    {
        inventory[0] = null;
    }

    public static void setFromNBT(IPokemob pokemob, NBTTagCompound tag)
    {
        float scale = tag.getFloat("scale");
        if (scale > 0)
        {
            pokemob.setSize(scale);
        }
        pokemob.setSexe((byte) tag.getInteger(PokecubeSerializer.SEXE));
        boolean shiny = tag.getBoolean("shiny");
        pokemob.setShiny(shiny);
        String ability = tag.getString("ability");
        if (!ability.isEmpty())
        {
            Ability abil = AbilityManager.getAbility(ability);
            pokemob.getMoveStats().ability = abil;
        }
        byte[] rgbaBytes = new byte[4];
        // TODO remove the legacy colour support eventually.
        if (tag.hasKey("colours", 7))
        {
            rgbaBytes = tag.getByteArray("colours");
        }
        else
        {
            rgbaBytes[0] = tag.getByte("red");
            rgbaBytes[1] = tag.getByte("green");
            rgbaBytes[2] = tag.getByte("blue");
            rgbaBytes[3] = 127;
        }
        if (pokemob instanceof IMobColourable)
        {
            ((IMobColourable) pokemob).setRGBA(rgbaBytes[0] + 128, rgbaBytes[1] + 128, rgbaBytes[2] + 128,
                    rgbaBytes[2] + 128);
        }
        String forme = tag.getString("forme");
        pokemob.changeForme(forme);
        pokemob.setSpecialInfo(tag.getInteger("specialInfo"));
    }
}

package pokecube.adventures.blocks.afa;

import java.util.Random;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

@Optional.InterfaceList(value = { @Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "OpenComputers"),
        @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntityAFA extends TileEntityOwnable implements IInventory, ITickable, SimpleComponent, SidedComponent
{
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
            pokemob.setAbility(abil);
        }
        byte[] rgbaBytes = new byte[4];
        if (tag.hasKey("colours", 7))
        {
            rgbaBytes = tag.getByteArray("colours");
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

    public IPokemob     pokemob        = null;
    boolean             shiny          = false;
    private ItemStack[] inventory      = new ItemStack[1];
    public int[]        shift          = { 0, 0, 0 };
    public int          scale          = 1000;
    public Ability      ability        = null;
    int                 energy         = 0;
    int                 distance       = 4;
    public int          transparency   = 128;
    public boolean      rotates        = true;
    public float        angle          = 0;
    public boolean      noEnergy       = false;

    protected boolean   addedToNetwork = false;

    public TileEntityAFA()
    {
        super();
    }

    @Override
    public boolean canConnectNode(EnumFacing side)
    {
        return side == EnumFacing.DOWN;
    }

    @Override
    public void clear()
    {
        inventory[0] = null;
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
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

    @Callback(doc = "Returns the current loaded ability")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getAbility(Context context, Arguments args) throws Exception
    {
        if (ability != null)
        {
            String arg = ability.toString();
            return new Object[] { arg };
        }
        throw new Exception("no ability");
    }

    @Override
    public String getComponentName()
    {
        return "afa";
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString("Ability Field Amplifier");
    }

    @Callback(doc = "Returns the amount of stored energy")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getEnergy(Context context, Arguments args)
    {
        return new Object[] { energy };
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return energy;
        if (id == 1) return distance;
        if (id == 2) return noEnergy ? 1 : 0;
        if (id == 3) return scale;
        if (id == 4) return shift[0];
        if (id == 5) return shift[1];
        if (id == 6) return shift[2];
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 8;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    public int getMaxEnergyStored(EnumFacing facing)
    {
        return 3200;
    }

    @Override
    public String getName()
    {
        return "AFA";
    }

    @Callback(doc = "Returns the current set range")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getRange(Context context, Arguments args)
    {
        return new Object[] { distance };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
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
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        if (ability != null) ability.destroy();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return PokecubeManager.isFilled(stack)
                || ItemStack.areItemStackTagsEqual(PokecubeItems.getStack("shiny_charm"), stack);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
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
        shift = nbt.getIntArray("shift");
        if (nbt.hasKey("scale")) scale = nbt.getInteger("scale");
        distance = nbt.getInteger("distance");
        noEnergy = nbt.getBoolean("noEnergy");
        angle = nbt.getFloat("angle");
        rotates = nbt.getBoolean("rotates");
        transparency = nbt.getInteger("transparency");
        energy = nbt.getInteger("energy");
    }

    public int receiveEnergy(EnumFacing facing, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, getMaxEnergyStored(facing) - energy);
        if (!simulate && receive > 0)
        {
            energy += receive;
        }
        return receive;
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
        if (pokemob != null && pokemob.getAbility() != null)
        {
            ability = pokemob.getAbility();
            ability.destroy();
            ((Entity) pokemob).setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            ability.init(pokemob, distance);
        }
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
    public void setField(int id, int value)
    {
        if (id == 0) energy = value;
        if (id == 1) distance = value;
        if (id == 2) noEnergy = value != 0;
        if (id == 3) scale = value;
        if (id == 4) shift[0] = value;
        if (id == 5) shift[1] = value;
        if (id == 6) shift[2] = value;
        if (id == 7)
        {
            shift[0] = shift[1] = shift[2] = 0;
            scale = 1000;
        }
        distance = Math.max(0, distance);
        refreshAbility();
        if (!worldObj.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
    }

    @Callback(doc = "function(scale:number, dx:number, dy:number, dz:number)- Sets the parameters for the hologram.")
    @Optional.Method(modid = "OpenComputers")
    public Object[] setHoloState(Context context, Arguments args)
    {
        scale = args.checkInteger(0);
        shift[0] = args.checkInteger(1);
        shift[1] = args.checkInteger(2);
        shift[2] = args.checkInteger(3);
        if (!worldObj.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
        return new Object[0];
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (stack == null || stack.stackSize <= 0) inventory[index] = null;
        else inventory[index] = stack;
        refreshAbility();
    }

    @Callback(doc = "function(range:number) - sets the radius of affect")
    @Optional.Method(modid = "OpenComputers")
    public Object[] setRange(Context context, Arguments args)
    {
        distance = args.checkInteger(0);
        if (!worldObj.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
        return new Object[] { distance };
    }

    @SubscribeEvent
    public void spawnEvent(SpawnEvent.Post evt)
    {
        ItemStack reference = PokecubeItems.getStack("shiny_charm");
        shiny = Tools.isSameStack(reference, inventory[0]);
        if (shiny)
        {
            if (evt.location.distanceTo(Vector3.getNewVector().set(this)) <= distance)
            {
                Random rand = new Random();
                int rate = Math.max(PokecubeAdv.conf.afaShinyRate, 1);
                if (rand.nextInt(rate) == 0)
                {
                    if (!noEnergy && !worldObj.isRemote)
                    {
                        int needed = (int) Math.ceil(distance * distance * distance / ((double) 50));
                        if (this.energy < needed)
                        {
                            energy = 0;
                            worldObj.playSound(getPos().getX(), getPos().getY(), getPos().getZ(),
                                    SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                            return;
                        }
                        energy -= needed;
                    }
                    evt.pokemob.setShiny(true);
                    worldObj.playSound(evt.entity.posX, evt.entity.posY, evt.entity.posZ,
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    worldObj.playSound(getPos().getX(), getPos().getY(), getPos().getZ(),
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                }
            }
        }
    }

    @Override
    public void update()
    {
        if (!PokecubeAdv.hasEnergyAPI && !worldObj.isRemote)
        {
            energy += 256;
            energy = Math.min(energy, 3200);
        }
        if (inventory[0] != null && pokemob == null)
        {
            refreshAbility();
        }
        else if (inventory[0] == null)
        {
            refreshAbility();
        }

        boolean shouldUseEnergy = pokemob != null && ability != null;
        int levelFactor = 0;

        if (pokemob != null && ability != null)
        {
            shiny = false;
            // Tick increase incase ability tracks this for update.
            // Renderer can also then render it animated.
            ((Entity) pokemob).ticksExisted++;
            levelFactor = pokemob.getLevel();
            // Do not call ability update on client.
            if (!worldObj.isRemote) ability.onUpdate(pokemob);
        }
        shouldUseEnergy = shouldUseEnergy || shiny;

        if (shouldUseEnergy)
        {
            if (!noEnergy && !worldObj.isRemote)
            {
                int needed = (int) Math.ceil(distance * distance * distance / ((double) 50 + 5 * levelFactor));
                if (energy < needed)
                {
                    energy = 0;
                }
                else energy -= needed;
            }
        }
    }

    @Override
    public void validate()
    {
        super.validate();
        MinecraftForge.EVENT_BUS.register(this);
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
        nbt.setIntArray("shift", shift);
        nbt.setInteger("scale", scale);
        nbt.setTag("Inventory", itemList);
        nbt.setInteger("distance", distance);
        nbt.setBoolean("noEnergy", noEnergy);
        nbt.setFloat("angle", angle);
        nbt.setBoolean("rotates", rotates);
        nbt.setInteger("transparency", transparency);
        nbt.setInteger("energy", energy);
        return nbt;
    }
}

package pokecube.adventures.blocks.cloner;

import java.util.ArrayList;
import java.util.List;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.TileEnergyHandler;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.Tools;

public class TileEntityCloner extends TileEnergyHandler implements IInventory, ITickable, Environment
{
    // TODO Open Computer support, to get the egg/pokemob info out of the items
    // inside the inventory.
    public TileEntityCloner()
    {
        super();
        storage = new EnergyStorage(32000);
        try
        {
            node = Network.newNode(this, Visibility.Network).withConnector()
                    .withComponent("splicer", Visibility.Network).create();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public CraftMatrix          craftMatrix;
    public InventoryCraftResult result;
    private ItemStack[]         inventory = new ItemStack[10];
    EntityPlayer                user;

    protected boolean addedToNetwork = false;

    @Override
    public void update()
    {
        if (!addedToNetwork)
        {
            addedToNetwork = true;
            Network.joinOrCreateNetwork(this);
        }
        if (worldObj.getTotalWorldTime() % 10 == 0 && !worldObj.isRemote)
        {
            checkFossil();
            checkMewtwo();
        }
    }

    private void checkMewtwo()
    {
        int mewHairIndex = -1;
        int eggIndex = -1;
        int potionIndex = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = inventory[i];
            if(stack==null)
            {
                
            }
            else if (stack.isItemEqual(PokecubeItems.getStack("mewHair")))
            {
                mewHairIndex = i;
            }
            else if (stack.getItem() instanceof ItemPokemobEgg)
            {
                eggIndex = i;
            }
            else if( stack.getItem() instanceof ItemPotion)
            {
                ItemPotion potion = (ItemPotion) stack.getItem();
                List<PotionEffect> effects = potion.getEffects(stack);
                for(PotionEffect effect: effects)
                {
                    if(effect!=null && effect.getEffectName().contains("regeneration"))
                    {
                        potionIndex = i;
                        break;
                    }
                }
            }
            
        }
        
        if (mewHairIndex >= 0 && potionIndex >= 0 && eggIndex >= 0)
        {
            ItemStack hair = inventory[mewHairIndex];
            ItemStack egg = inventory[eggIndex];
            ItemStack potion = inventory[potionIndex];
            int energy = storage.getEnergyStored();
            if (energy >= 30000)
            {
                storage.extractEnergy(30000, false);
                egg = egg.splitStack(1);
                if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
                egg.getTagCompound().setInteger("pokemobNumber", 150);
                
                IPokemob mob = ItemPokemobEgg.getPokemob(getWorld(), egg);
                if(mob!=null)
                {
                    EntityLiving entity = (EntityLiving) mob;
                    entity.setHealth(entity.getMaxHealth());
                    ((IPokemob) entity).setExp(Tools.levelToXp(mob.getExperienceMode(), 70), true, true);
                    entity.setLocationAndAngles(pos.getX(), pos.getY() + 1, pos.getZ(),
                            worldObj.rand.nextFloat() * 360F, 0.0F);
                    worldObj.spawnEntityInWorld(entity);
                    entity.playLivingSound();
                    hair.stackSize--;
                    inventory[potionIndex] = null;
                }
                
            }
        }
                
    }

    private void checkFossil()
    {
        int fossilIndex = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = inventory[i];
            int num = PokecubeItems.getFossilNumber(stack);
            if (num > 0)
            {
                fossilIndex = i;
                break;
            }
        }
        if (fossilIndex >= 0)
        {
            ItemStack stack = inventory[fossilIndex];
            int num = PokecubeItems.getFossilNumber(stack);
            int energy = storage.getEnergyStored();
            if (energy >= 20000)
            {
                storage.extractEnergy(20000, false);
                EntityLiving entity = (EntityLiving) mod_Pokecube.core.createEntityByPokedexNb(num, worldObj);
                if (entity != null)
                {
                    entity.setHealth(entity.getMaxHealth());
                    // to avoid the death on spawn
                    int maxXP = 6000;
                    // that will make your pokemob around level 3-5.
                    // You can give him more XP if you want
                    ((IPokemob) entity).setExp(worldObj.rand.nextInt(maxXP) + 50, true, true);
                    if (user != null) ((IPokemob) entity).setPokemonOwner(user);
                    entity.setLocationAndAngles(pos.getX(), pos.getY() + 1, pos.getZ(),
                            worldObj.rand.nextFloat() * 360F, 0.0F);
                    worldObj.spawnEntityInWorld(entity);
                    entity.playLivingSound();
                    stack.stackSize--;
                }
            }
        }
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
        if (node != null && node.host() == this)
        {
            node.load(nbt.getCompoundTag("oc:node"));
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
        if (node != null && node.host() == this)
        {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            node.save(nodeNbt);
            nbt.setTag("oc:node", nodeNbt);
        }
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        if (craftMatrix != null)
        {
            craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
        }
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
            if (craftMatrix != null)
            {
                craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
            }
        }
    }

    @Override
    public String getName()
    {
        return "cloner";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return new ChatComponentText("cloner");
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
        return user == null || user == player;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
        user = player;
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        user = null;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index != 0;
    }

    @Override
    public int getField(int id)
    {
        return storage.getEnergyStored();
    }

    @Override
    public void setField(int id, int value)
    {
        storage.setEnergyStored(value);
    }

    @Override
    public int getFieldCount()
    {
        return 1;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < 10; i++)
            inventory[i] = null;
    }

    public static class CraftMatrix extends InventoryCrafting
    {
        /** Class containing the callbacks for the events on_GUIClosed and
         * on_CraftMaxtrixChanged. */
        protected final Container eventHandler;
        final TileEntityCloner    cloner;

        public CraftMatrix(Container eventHandlerIn, TileEntityCloner cloner)
        {
            super(eventHandlerIn, 3, 3);
            this.eventHandler = eventHandlerIn;
            this.cloner = cloner;
        }

        @Override
        /** Returns the stack in the given slot.
         * 
         * @param index
         *            The slot to retrieve from. */
        public ItemStack getStackInSlot(int index)
        {
            return index >= this.getSizeInventory() ? null : cloner.getStackInSlot(index);
        }

        @Override
        /** Returns the itemstack in the slot specified (Top left is 0, 0).
         * Args: row, column */
        public ItemStack getStackInRowAndColumn(int row, int column)
        {
            return row >= 0 && row < 3 && column >= 0 && column <= 3 ? this.getStackInSlot(row + column * 3) : null;
        }

        @Override
        /** Removes a stack from the given slot and returns it.
         * 
         * @param index
         *            The slot to remove a stack from. */
        public ItemStack removeStackFromSlot(int index)
        {
            return cloner.removeStackFromSlot(index);
        }

        @Override
        /** Removes up to a specified number of items from an inventory slot and
         * returns them in a new stack.
         * 
         * @param index
         *            The slot to remove from.
         * @param count
         *            The maximum amount of items to remove. */
        public ItemStack decrStackSize(int index, int count)
        {
            ItemStack ret = cloner.decrStackSize(index, count);
            this.eventHandler.onCraftMatrixChanged(this);
            return ret;
        }

        @Override
        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index, stack);
            this.eventHandler.onCraftMatrixChanged(this);
        }

        @Override
        public int getHeight()
        {
            return 3;
        }

        @Override
        public int getWidth()
        {
            return 3;
        }
    }

    public static class CraftResult extends InventoryCraftResult
    {
        final TileEntityCloner cloner;

        public CraftResult(TileEntityCloner cloner)
        {
            this.cloner = cloner;
        }

        /** Returns the stack in the given slot.
         * 
         * @param index
         *            The slot to retrieve from. */
        public ItemStack getStackInSlot(int index)
        {
            return cloner.getStackInSlot(index + 9);
        }

        /** Removes up to a specified number of items from an inventory slot and
         * returns them in a new stack.
         * 
         * @param index
         *            The slot to remove from.
         * @param count
         *            The maximum amount of items to remove. */
        public ItemStack decrStackSize(int index, int count)
        {
            return cloner.decrStackSize(index + 9, count);
        }

        /** Removes a stack from the given slot and returns it.
         * 
         * @param index
         *            The slot to remove a stack from. */
        public ItemStack removeStackFromSlot(int index)
        {
            return cloner.removeStackFromSlot(index + 9);
        }

        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index + 9, stack);
        }

        /** Returns the maximum stack size for a inventory slot. Seems to always
         * be 64, possibly will be extended. */
        public int getInventoryStackLimit()
        {
            return 64;
        }

        /** For tile entities, ensures the chunk containing the tile entity is
         * saved to disk later - the game won't think it hasn't changed and skip
         * it. */
        public void markDirty()
        {
        }

        /** Do not make give this method the name canInteractWith because it
         * clashes with Container */
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return true;
        }

        public void openInventory(EntityPlayer player)
        {
        }

        public void closeInventory(EntityPlayer player)
        {
        }

        /** Returns true if automation is allowed to insert the given stack
         * (ignoring stack size) into the given slot. */
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return true;
        }

        public int getField(int id)
        {
            return 0;
        }

        public void setField(int id, int value)
        {
        }

        public int getFieldCount()
        {
            return 0;
        }

        public void clear()
        {
            cloner.setInventorySlotContents(9, null);
        }

    }

    Node node;

    @Override
    public Node node()
    {
        return node;
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Override
    public void onConnect(Node arg0)
    {
    }

    @Override
    public void onDisconnect(Node arg0)
    {
    }

    @Override
    public void onMessage(Message arg0)
    {
    }

    @Callback
    /** Returns the info for the slot number given in args. the second argument
     * is which info to return.<br>
     * <br>
     * If the slot is out of bounds, it returns the info for slot 0.<br>
     * <br>
     * Returns the following: Stack name, ivs, size, nature.<br>
     * <br>
     * ivs are a long.
     * 
     * @param context
     * @param args
     * @return */
    public Object[] getInfo(Context context, Arguments args)
    {
        ArrayList<Object> ret = new ArrayList<>();
        int i = args.checkInteger(0);
        int j = args.checkInteger(1);
        if (i < 0 || i > inventory.length) i = 0;
        ItemStack stack = inventory[i];
        if (stack != null)
        {
            if (j == 0) ret.add(stack.getDisplayName());
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
            {
                if (j == 1) ret.add(stack.getTagCompound().getLong("ivs"));
                if (j == 2) ret.add(stack.getTagCompound().getFloat("size"));
                if (j == 3) ret.add(stack.getTagCompound().getByte("nature"));
                if (j == 4) ret.add(stack.getTagCompound().getString("moves"));
                if (j == 5) ret.add(stack.getTagCompound().getBoolean("shiny"));
            }
        }
        return ret.toArray();
    }

}

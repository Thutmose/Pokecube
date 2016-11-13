package pokecube.adventures.blocks.cloner;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.BlockCloner.EnumType;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.network.PacketHandler;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntityCloner extends TileEntity implements IInventory, ITickable, SimpleComponent
{
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
            if (eventHandler != null) this.eventHandler.onCraftMatrixChanged(this);
            return ret;
        }

        @Override
        public int getHeight()
        {
            return 3;
        }

        @Override
        /** Returns the itemstack in the slot specified (Top left is 0, 0).
         * Args: row, column */
        public ItemStack getStackInRowAndColumn(int row, int column)
        {
            return row >= 0 && row < 3 && column >= 0 && column <= 3 ? this.getStackInSlot(row + column * 3) : null;
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
        public int getWidth()
        {
            return 3;
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
        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index, stack);
            if (eventHandler != null) eventHandler.onCraftMatrixChanged(this);
        }
    }

    public static class CraftResult extends InventoryCraftResult
    {
        final TileEntityCloner cloner;

        public CraftResult(TileEntityCloner cloner)
        {
            this.cloner = cloner;
        }

        @Override
        public void clear()
        {
            cloner.setInventorySlotContents(9, null);
        }

        @Override
        public void closeInventory(EntityPlayer player)
        {
        }

        /** Removes up to a specified number of items from an inventory slot and
         * returns them in a new stack.
         * 
         * @param index
         *            The slot to remove from.
         * @param count
         *            The maximum amount of items to remove. */
        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            return cloner.decrStackSize(index + 9, count);
        }

        @Override
        public int getField(int id)
        {
            return 0;
        }

        @Override
        public int getFieldCount()
        {
            return 0;
        }

        /** Returns the maximum stack size for a inventory slot. Seems to always
         * be 64, possibly will be extended. */
        @Override
        public int getInventoryStackLimit()
        {
            return 64;
        }

        /** Returns the stack in the given slot.
         * 
         * @param index
         *            The slot to retrieve from. */
        @Override
        public ItemStack getStackInSlot(int index)
        {
            return cloner.getStackInSlot(index + 9);
        }

        /** Returns true if automation is allowed to insert the given stack
         * (ignoring stack size) into the given slot. */
        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return true;
        }

        /** Do not make give this method the name canInteractWith because it
         * clashes with Container */
        @Override
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return true;
        }

        /** For tile entities, ensures the chunk containing the tile entity is
         * saved to disk later - the game won't think it hasn't changed and skip
         * it. */
        @Override
        public void markDirty()
        {
        }

        @Override
        public void openInventory(EntityPlayer player)
        {
        }

        /** Removes a stack from the given slot and returns it.
         * 
         * @param index
         *            The slot to remove a stack from. */
        @Override
        public ItemStack removeStackFromSlot(int index)
        {
            return cloner.removeStackFromSlot(index + 9);
        }

        @Override
        public void setField(int id, int value)
        {
        }

        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        @Override
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index + 9, stack);
        }
    }

    public static class ClonerProcess
    {
        final IClonerRecipe    recipe;
        final TileEntityCloner tile;
        int                    needed = 0;

        public ClonerProcess(IClonerRecipe recipe, TileEntityCloner tile)
        {
            this.recipe = recipe;
            this.tile = tile;
            needed = recipe.getEnergyCost();
        }

        public boolean valid()
        {
            if (tile.getWorld() == null) return false;
            boolean reanimator = tile.getWorld().getBlockState(tile.getPos())
                    .getValue(BlockCloner.VARIANT) == EnumType.FOSSIL;
            if (reanimator)
            {
                if (!(recipe instanceof RecipeFossilRevive)) return false;
                RecipeFossilRevive recipe2 = (RecipeFossilRevive) recipe;
                return recipe2.reanimator && recipe.matches(tile.craftMatrix, tile.getWorld());
            }
            if ((recipe instanceof RecipeFossilRevive))
            {
                RecipeFossilRevive recipe2 = (RecipeFossilRevive) recipe;
                return !recipe2.reanimator && recipe.matches(tile.craftMatrix, tile.getWorld());
            }
            return recipe.matches(tile.craftMatrix, tile.getWorld());
        }

        public void reset()
        {
            needed = recipe.getEnergyCost();
            tile.progress = getProgress();
        }

        public boolean tick()
        {
            if (needed > 0)
            {
                needed -= Math.min(needed, tile.energy);
                tile.energy = 0;
                tile.progress = getProgress();
                tile.total = recipe.getEnergyCost();
                return true;
            }
            return !complete();
        }

        public int getProgress()
        {
            return recipe.getEnergyCost() - needed;
        }

        public boolean complete()
        {
            if (recipe instanceof RecipeFossilRevive)
            {
                ItemStack[] remaining = recipe.getRemainingItems(tile.craftMatrix);
                for (int i = 0; i < remaining.length; i++)
                {
                    if (remaining[i] != null) tile.setInventorySlotContents(i, remaining[i]);
                    else tile.decrStackSize(i, 1);
                }
                RecipeFossilRevive recipe = (RecipeFossilRevive) this.recipe;
                EntityLiving entity = (EntityLiving) PokecubeMod.core.createPokemob(recipe.pokedexEntry,
                        tile.getWorld());
                if (entity != null)
                {
                    entity.setHealth(entity.getMaxHealth());
                    // to avoid the death on spawn
                    int exp = Tools.levelToXp(recipe.pokedexEntry.getEvolutionMode(), recipe.level);
                    // that will make your pokemob around level 3-5.
                    // You can give him more XP if you want
                    entity = (EntityLiving) ((IPokemob) entity).setForSpawn(exp);
                    if (tile.user != null && recipe.tame) ((IPokemob) entity).setPokemonOwner(tile.user);
                    EnumFacing dir = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockCloner.FACING);
                    entity.setLocationAndAngles(tile.pos.getX() + 0.5 + dir.getFrontOffsetX(), tile.pos.getY() + 1,
                            tile.pos.getZ() + 0.5 + dir.getFrontOffsetZ(), tile.getWorld().rand.nextFloat() * 360F,
                            0.0F);
                    tile.getWorld().spawnEntityInWorld(entity);
                    entity.playLivingSound();
                }
                return true;
            }
            if (tile.getStackInSlot(9) == null)
            {
                tile.setInventorySlotContents(9, recipe.getCraftingResult(tile.craftMatrix));
                if (tile.craftMatrix.eventHandler != null) tile.craftMatrix.eventHandler.onCraftMatrixChanged(tile);
                PacketHandler.sendTileUpdate(tile);
            }
            return false;
        }
    }

    public static int           MAXENERGY      = 256;
    public int                  energy         = 0;
    private int                 progress       = 0;
    private int                 total          = 0;
    protected ClonerProcess     currentProcess = null;
    protected ClonerProcess     cloneProcess   = null;
    public CraftMatrix          craftMatrix;
    public InventoryCraftResult result;
    private ItemStack[]         inventory      = new ItemStack[10];

    EntityPlayer                user;

    public TileEntityCloner()
    {
        super();
        this.craftMatrix = new CraftMatrix(null, this);
        cloneProcess = new ClonerProcess(new RecipeClone(), this);
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < 10; i++)
            inventory[i] = null;
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        user = null;
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
    public String getComponentName()
    {
        return "splicer";
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString("cloner");
    }

    @Override
    public int getField(int id)
    {
        return id == 0 ? progress : total;
    }

    @Override
    public int getFieldCount()
    {
        return 2;
    }

    @Callback(doc = "function(slot:number, info:number) -- slot is which slot to get the info for,"
            + " info is which information to return." + " 0 is the name," + " 1 is the ivs," + " 2 is the size,"
            + " 3 is the nature," + " 4 is the list of egg moves," + " 5 is shininess")
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
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInfo(Context context, Arguments args) throws Exception
    {
        ArrayList<Object> ret = new ArrayList<>();
        int i = args.checkInteger(0);
        int j = args.checkInteger(1);
        if (i < 0 || i > inventory.length) throw new Exception("index out of bounds");
        ItemStack stack = inventory[i];
        if (stack != null)
        {
            if (j == 0) ret.add(stack.getDisplayName());
            else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
            {
                if (j == 1)
                {
                    if (!stack.getTagCompound().hasKey("ivs")) throw new Exception("no ivs found");
                    ret.add(stack.getTagCompound().getLong("ivs"));
                }
                if (j == 2)
                {
                    if (!stack.getTagCompound().hasKey("size")) throw new Exception("no size found");
                    ret.add(stack.getTagCompound().getFloat("size"));
                }
                if (j == 3)
                {
                    if (!stack.getTagCompound().hasKey("nature")) throw new Exception("no nature found");
                    ret.add(stack.getTagCompound().getByte("nature"));
                }
                if (j == 4)
                {
                    if (!stack.getTagCompound().hasKey("moves")) throw new Exception("no egg moves found");
                    Map<Integer, String> moves = Maps.newHashMap();
                    String eggMoves[] = stack.getTagCompound().getString("moves").split(";");
                    if (eggMoves.length == 0) throw new Exception("no egg moves found");
                    for (int k = 1; k < eggMoves.length + 1; k++)
                    {
                        moves.put(k, eggMoves[k - 1]);
                    }
                    ret.add(moves);
                }
                if (j == 5)
                {
                    if (!stack.getTagCompound().hasKey("shiny")) throw new Exception("no shinyInfo found");
                    ret.add(stack.getTagCompound().getBoolean("shiny"));
                }
                if (j == 6)
                {
                    if (!stack.getTagCompound().hasKey("abilityIndex")) throw new Exception("no ability Index found");
                    ret.add(stack.getTagCompound().getInteger("abilityIndex"));
                }
            }
            else throw new Exception("the itemstack does not contain the required info");

            return ret.toArray();
        }
        throw new Exception("no item in slot " + i);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public String getName()
    {
        return "cloner";
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
        if (craftMatrix != null && craftMatrix.eventHandler != null)
        {
            craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
        }
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index != 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return user == null || user == player;
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
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
            if (craftMatrix != null && craftMatrix.eventHandler != null)
            {
                craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
            }
        }
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
        user = player;
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
        if (nbt.hasKey("progress"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("progress");
            String entryName = tag.getString("entry");
            int needed = tag.getInteger("needed");
            RecipeFossilRevive recipe = RecipeFossilRevive.getRecipe(Database.getEntry(entryName));
            if (recipe != null)
            {
                currentProcess = new ClonerProcess(recipe, this);
                currentProcess.needed = needed;
                progress = needed;
                total = currentProcess.recipe.getEnergyCost();
            }
            else if (needed != 0)
            {
                currentProcess = new ClonerProcess(new RecipeClone(), this);
                currentProcess.needed = needed;
                progress = needed;
                total = currentProcess.recipe.getEnergyCost();
            }
            if (currentProcess == null || !currentProcess.valid())
            {
                progress = 0;
                currentProcess = cloneProcess;
                total = currentProcess.recipe.getEnergyCost();
            }
        }
    }

    public int receiveEnergy(EnumFacing facing, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, MAXENERGY - energy);
        if (!simulate && receive > 0)
        {
            energy += receive;
        }
        return receive;
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
    public void setField(int id, int value)
    {
        if (id == 0) progress = value;
        else total = value;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        boolean refresh = false;
        if (!Tools.isSameStack(stack, inventory[index])) refresh = true;
        if (stack == null || stack.stackSize <= 0) inventory[index] = null;
        else inventory[index] = stack;
        if (refresh)
        {
            checkRecipes();
        }
    }

    @Override
    public void update()
    {
        checkCollision();
        if (worldObj.isRemote) return;
        if (!PokecubeAdv.hasEnergyAPI) energy += 32;
        checkRecipes();
    }

    private void checkCollision()
    {
        BlockCloner.checkCollision(this);
    }

    public void checkRecipes()
    {
        if (currentProcess == null || !currentProcess.valid())
        {
            for (RecipeFossilRevive recipe : RecipeFossilRevive.getRecipeList())
            {
                if (recipe.matches(craftMatrix, getWorld()))
                {
                    currentProcess = new ClonerProcess(recipe, this);
                    break;
                }
            }
            if (currentProcess == null)
            {
                cloneProcess.reset();
                total = 0;
                currentProcess = cloneProcess;
            }
        }
        else
        {
            boolean valid = currentProcess.valid();
            boolean done = true;
            if (valid)
            {
                done = !currentProcess.tick();
            }
            if (!valid || done)
            {
                cloneProcess.reset();
                currentProcess = cloneProcess;
                progress = 0;
                total = 0;
                markDirty();
            }
        }
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
}

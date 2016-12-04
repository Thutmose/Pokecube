package pokecube.adventures.blocks.cloner.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
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
import pokecube.adventures.blocks.cloner.block.BlockCloner;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.core.database.Database;
import thut.core.common.blocks.DefaultInventory;
import thut.lib.CompatWrapper;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntityCloner extends TileEntity
        implements ITickable, DefaultInventory, SimpleComponent, IPoweredProgress
{
    public static int           MAXENERGY      = 256;
    public int                  energy         = 0;
    public int                  progress       = 0;
    public int                  total          = 0;
    public PoweredProcess       currentProcess = null;
    private CraftMatrix         craftMatrix;
    public InventoryCraftResult result;

    public EntityPlayer         user;

    public TileEntityCloner()
    {
        super();
        /** 1 slot for output, 1 slot for gene input, 1 slot for egg input and 7
         * slots for supporting item input. */
        inventory = CompatWrapper.makeList(10);
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        user = null;
    }

    @Override
    public String getComponentName()
    {
        return "reanimator";
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
        if (i < 0 || i > inventory.size()) throw new Exception("index out of bounds");
        ItemStack stack = inventory.get(i);
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
    public String getName()
    {
        return "cloner";
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        if (getCraftMatrix() != null && getCraftMatrix().eventHandler != null)
        {
            getCraftMatrix().eventHandler.onCraftMatrixChanged(getCraftMatrix());
        }
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
            if (getCraftMatrix() != null && getCraftMatrix().eventHandler != null)
            {
                getCraftMatrix().eventHandler.onCraftMatrixChanged(getCraftMatrix());
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
    public void setField(int id, int value)
    {
        if (id == 0) progress = value;
        else total = value;
    }

    @Override
    public void update()
    {
        checkCollision();
        if (worldObj.isRemote) return;
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
                if (recipe.matches(getCraftMatrix(), getWorld()))
                {
                    currentProcess = new PoweredProcess(recipe, this);
                    break;
                }
            }
            if (currentProcess == null)
            {
                total = 0;
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
                currentProcess = null;
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

    final List<ItemStack> inventory;

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
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
}

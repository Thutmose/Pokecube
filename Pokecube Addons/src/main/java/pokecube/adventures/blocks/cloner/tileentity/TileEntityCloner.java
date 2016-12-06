package pokecube.adventures.blocks.cloner.tileentity;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.block.BlockCloner;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntityCloner extends TileClonerBase implements SimpleComponent
{
    public static int MAXENERGY = 256;

    public TileEntityCloner()
    {
        /** 1 slot for output, 1 slot for gene input, 1 slot for egg input and 7
         * slots for supporting item input. */
        super(10, 9);
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
    public Object[] getOldInfo(Context context, Arguments args) throws Exception
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
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.isDNAContainer(stack);
        case 1:// Egg
            int[] eggIds = OreDictionary.getOreIDs(new ItemStack(Items.EGG));
            int[] stackIds = OreDictionary.getOreIDs(stack);
            for (int i = 0; i < eggIds.length; i++)
            {
                for (int j = 0; j < stackIds.length; j++)
                    if (eggIds[i] == stackIds[j]) return true;
            }
            return false;
        }
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

    @Override
    public boolean isValid(Class<? extends IPoweredRecipe> recipe)
    {
        return recipe == RecipeFossilRevive.class;
    }
}

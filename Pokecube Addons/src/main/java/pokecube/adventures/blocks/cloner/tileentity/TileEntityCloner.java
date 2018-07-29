package pokecube.adventures.blocks.cloner.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.block.BlockReanimator;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import thut.core.common.blocks.BlockRotatable;

public class TileEntityCloner extends TileClonerBase
{
    public static int MAXENERGY  = 256;
    int[][]           sidedSlots = new int[6][];

    public TileEntityCloner()
    {
        /** 1 slot for output, 1 slot for gene input, 1 slot for egg input and 7
         * slots for supporting item input. */
        super(10, 9);
        sidedSlots[EnumFacing.WEST.ordinal()] = new int[] { 0 };
        sidedSlots[EnumFacing.NORTH.ordinal()] = new int[] { 1 };
        for (int i = 0; i < 6; i++)
        {
            if (sidedSlots[i] == null)
            {
                sidedSlots[i] = new int[] { 2, 3, 4, 5, 6, 7, 8 };
            }
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        IBlockState state = getWorld().getBlockState(getPos());
        if (state.getBlock() instanceof BlockRotatable)
        {
            EnumFacing dir = state.getValue(BlockRotatable.FACING);
            if (dir == EnumFacing.EAST || dir == EnumFacing.WEST) dir = dir.getOpposite();
            int index = dir.getHorizontalIndex();
            for (int i = 0; i < index; i++)
                side = side.rotateAround(Axis.Y);
        }
        return sidedSlots[side.ordinal()];
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString("cloner");
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
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
            return ClonerHelper.getFromGenes(stack) != null;
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
        return index != getOutputSlot();
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
        if (world.isRemote)
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
        if (world.isRemote) return;
        checkRecipes();
    }

    private void checkCollision()
    {
        BlockReanimator.checkCollision(this);
    }

    @Override
    public boolean isValid(Class<? extends IPoweredRecipe> recipe)
    {
        return recipe == RecipeFossilRevive.class;
    }
}

package pokecube.core.blocks.pokecubeTable;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;

public class BlockPokecubeTable extends Block implements ITileEntityProvider
{
    // private ExtendedBlockState state = new ExtendedBlockState(this, new
    // IProperty[0],
    // new IUnlistedProperty[] { OBJModel.OBJProperty.INSTANCE });

    public BlockPokecubeTable()
    {
        super(Material.WOOD);
        this.setLightOpacity(0);
        this.setHardness(100);
        this.setResistance(100);
        this.setLightLevel(1f);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityPokecubeTable();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TileEntityPokecubeTable tileEntity = (TileEntityPokecubeTable)
        // world.getTileEntity(pos);
        // OBJModel.OBJState retState = new OBJModel.OBJState(
        // tileEntity == null ? Lists.newArrayList(OBJModel.Group.ALL) :
        // tileEntity.visible, true);
        return super.getExtendedState(state, world, pos);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    /** Called upon block activation (right click on the block.) */
    /** Called upon block activation (right click on the block.) */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            if (!PokecubeSerializer.getInstance().hasStarter(playerIn))
            {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(PokecubeClientPacket.CHOOSE1ST);
                buf.writeBoolean(true);
                buf.writeBoolean(
                        PokecubePacketHandler.specialStarters.containsKey(playerIn.getName().toLowerCase()));

                ArrayList<Integer> starters = new ArrayList<Integer>();
                TileEntity te = playerIn.worldObj.getTileEntity(pos.down(2));
                if (te != null && te instanceof IInventory)
                {
                    IInventory container = (IInventory) te;
                    for (int i1 = 0; i1 < container.getSizeInventory(); i1++)
                    {
                        ItemStack stack = container.getStackInSlot(i1);
                        if (stack != null && stack.getItem() instanceof ItemPokemobEgg)
                        {
                            IPokemob mob = ItemPokemobEgg.getPokemob(worldIn, stack);
                            if (mob != null)
                            {
                                starters.add(mob.getPokedexNb());
                                ((Entity) mob).setDead();
                            }
                        }
                    }
                }
                buf.writeInt(starters.size());
                for (Integer i : starters)
                {
                    buf.writeInt(i);
                }
                PokecubeClientPacket packet = new PokecubeClientPacket(buf);
                PokecubePacketHandler.sendToClient(packet, playerIn);
            }
        }
        return true;
    }
}

package pokecube.core.blocks.healtable;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.Config;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class BlockHealTable extends Block implements ITileEntityProvider
{
    public static final PropertyBool FIXED = PropertyBool.create("fixed");

    public BlockHealTable()
    {
        super(Material.cloth);
        setHardness(20);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FIXED, Boolean.FALSE));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        Vector3 v = Vector3.getNewVector();
        if (!world.isRemote && !((Boolean) state.getValue(BlockHealTable.FIXED)))
        {
            PokecubeSerializer.getInstance().removeChunks(world, v.set(pos));
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { FIXED });
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileHealTable();
    }

    private void dropItems(World world, BlockPos pos)
    {
        Random rand = new Random();
        TileEntity tile_entity = world.getTileEntity(pos);

        if (!(tile_entity instanceof IInventory)) { return; }

        IInventory inventory = (IInventory) tile_entity;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item = inventory.getStackInSlot(i);

            if (item != null && item.stackSize > 0)
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                EntityItem entity_item = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                        new ItemStack(item.getItem(), item.stackSize, item.getItemDamage()));

                if (item.hasTagCompound())
                {
                    entity_item.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }

                float factor = 0.5F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntityInWorld(entity_item);
                item.stackSize = 0;
            }
        }
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return !((Boolean) state.getValue(BlockHealTable.FIXED)) ? 0 : 1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FIXED, meta == 0 ? Boolean.FALSE : Boolean.TRUE);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        TileEntity tile_entity = world.getTileEntity(pos);

        if (tile_entity == null || player.isSneaking())
        {
            if (player.capabilities.isCreativeMode && !world.isRemote)
            {
                state = state.cycleProperty(FIXED);
                player.addChatMessage(new TextComponentString("Set Block to "
                        + (state.getValue(BlockHealTable.FIXED) ? "Breakable" : "Unbreakable")));
                world.setBlockState(pos, state);
            }
            return false;
        }
        player.openGui(PokecubeCore.instance, Config.GUIPOKECENTER_ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    /** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
     * side, hitX, hitY, hitZ, block metadata */
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        if (world.isRemote || meta == 1) return getStateFromMeta(meta);

        Chunk centre = world.getChunkFromBlockCoords(pos);
        Vector3 v = Vector3.getNewVector();
        PokecubeSerializer.getInstance().addChunks(world, v.set(pos), centre.getChunkCoordIntPair());
        return getStateFromMeta(meta);
    }
}
package pokecube.adventures.blocks.cloner;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class BlockCloner extends Block implements ITileEntityProvider
{

    public BlockCloner()
    {
        super(Material.iron);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setLightLevel(1f);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        player.openGui(PokecubeAdv.instance, PokecubeAdv.GUICLONER_ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCloner();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        Vector3 v = Vector3.getNewVectorFromPool();
        if(!world.isRemote && !((Boolean)state.getValue(BlockHealTable.FIXED)))
        {
            PokecubeSerializer.getInstance().removeChunks(world, v.set(pos));
        }
        v.freeVectorFromPool();
        super.breakBlock(world, pos, state);
    }

    private void dropItems(World world, BlockPos pos)
    {
        Random rand = new Random();
        TileEntity tile_entity = world.getTileEntity(pos);

        if (!(tile_entity instanceof IInventory))
        {
            return;
        }

        IInventory inventory = (IInventory) tile_entity;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item = inventory.getStackInSlot(i);

            if (item != null && item.stackSize > 0)
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                EntityItem entity_item = new EntityItem(world, pos.getX() + rx, pos.getY() + ry,
                        pos.getZ() + rz, new ItemStack(item.getItem(), item.stackSize,
                                item.getItemDamage()));

                if (item.hasTagCompound())
                {
                    entity_item.getEntityItem().setTagCompound((NBTTagCompound) item
                            .getTagCompound().copy());
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

}

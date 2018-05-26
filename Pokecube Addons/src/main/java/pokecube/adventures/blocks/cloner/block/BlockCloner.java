package pokecube.adventures.blocks.cloner.block;

import static java.lang.Math.max;

import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityGeneExtractor;
import pokecube.adventures.blocks.cloner.tileentity.TileEntitySplicer;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.core.common.blocks.BlockRotatable;
import thut.lib.CompatWrapper;

public class BlockCloner extends BlockRotatable implements ITileEntityProvider
{
    public static enum EnumType implements IStringSerializable
    {
        FOSSIL("reanimator"), SPLICER("splicer"), EXTRACTOR("extractor");

        final String name;

        private EnumType(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

    public BlockCloner()
    {
        super(Material.IRON);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setLightLevel(1f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.FOSSIL).withProperty(FACING,
                EnumFacing.NORTH));
    }

    /** Used to determine ambient occlusion and culling when rebuilding chunks
     * for render */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { VARIANT, FACING });
    }

    /** Get the damage value that this Block should drop */
    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(VARIANT).ordinal();
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = 0;
        meta = meta | state.getValue(VARIANT).ordinal();
        int direction;
        switch (state.getValue(FACING))
        {
        case NORTH:
            direction = 0 * 4;
            break;
        case EAST:
            direction = 1 * 4;
            break;
        case SOUTH:
            direction = 2 * 4;
            break;
        case WEST:
            direction = 3 * 4;
            break;
        default:
            direction = 0;
        }
        meta |= direction;
        return meta;
    }

    /** Convert the given metadata into a BlockState for this Block */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        int variant = meta & 3;
        if (variant >= EnumType.values().length) variant = 0;
        int direction = meta / 4;
        EnumFacing dir = EnumFacing.NORTH;
        switch (direction)
        {
        case 0:
            dir = EnumFacing.NORTH;
            break;
        case 1:
            dir = EnumFacing.EAST;
            break;
        case 2:
            dir = EnumFacing.SOUTH;
            break;
        case 3:
            dir = EnumFacing.WEST;
            break;
        }
        return this.getDefaultState().withProperty(VARIANT, EnumType.values()[variant]).withProperty(FACING, dir);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    /** The type of render function called. 3 for standard block models, 2 for
     * TESR's, 1 for liquids, -1 is no render */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        int variant = meta & 3;
        switch (variant)
        {
        case 0:
            return new TileEntityCloner();
        case 1:
            return new TileEntitySplicer();
        case 2:
            return new TileEntityGeneExtractor();
        }
        return new TileEntityCloner();
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
            if (CompatWrapper.isValid(item))
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                EntityItem entity_item = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                        new ItemStack(item.getItem(), CompatWrapper.getStackSize(item), item.getItemDamage()));
                if (item.hasTagCompound())
                {
                    entity_item.getItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }
                float factor = 0.005F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntity(entity_item);
                CompatWrapper.setStackSize(item, 0);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    // 1.11
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onBlockActivated(worldIn, pos, state, playerIn, hand, playerIn.getHeldItem(hand), side, hitX, hitY,
                hitZ);
    }

    // 1.10
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int gui = PokecubeAdv.GUICLONER_ID;
        int variant = state.getValue(VARIANT).ordinal();
        switch (variant)
        {
        case 0:
            break;
        case 1:
            gui = PokecubeAdv.GUISPLICER_ID;
            break;
        case 2:
            gui = PokecubeAdv.GUIEXTRACTOR_ID;
            break;
        }
        playerIn.openGui(PokecubeAdv.instance, gui, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    public static void checkCollision(TileEntityCloner tile)
    {
        Vector3 v = Vector3.getNewVector().set(tile).addTo(0.5, 0, 0.5);
        AxisAlignedBB bb = new AxisAlignedBB(v.x - 1, v.y, v.z - 1, v.x + 1, v.y + 3, v.z + 1);
        List<Entity> entities = tile.getWorld().getEntitiesWithinAABB(Entity.class, bb.grow(1));
        if (entities.isEmpty()) return;

        Matrix3 matBox = new Matrix3();
        matBox.boxMin().set(tile);
        matBox.boxMax().set(tile);
        matBox.boxMax().addTo(1, 2.5, 1);
        AxisAlignedBB box = matBox.getBoundingBox();
        MutableBlockPos pos = new MutableBlockPos();
        for (Entity entity : entities)
        {
            pos.setPos(tile.getPos());
            Vector3f temp1 = new Vector3f();
            Vector3f diffs = new Vector3f((float) (0 - entity.motionX), (float) (0 - entity.motionY),
                    (float) (0 - entity.motionY));

            double minX = entity.getEntityBoundingBox().minX;
            double minY = entity.getEntityBoundingBox().minY;
            double minZ = entity.getEntityBoundingBox().minZ;
            double maxX = entity.getEntityBoundingBox().maxX;
            double maxY = entity.getEntityBoundingBox().maxY;
            double maxZ = entity.getEntityBoundingBox().maxZ;
            double factor = 0.75d;
            double dx = max(maxX - minX, 0.5) / factor + (entity.motionX - 0),
                    dz = max(maxZ - minZ, 0.5) / factor + (entity.motionZ - 0), r;
            AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
            double yTop = Math.min(entity.stepHeight + entity.posY + 0, maxY);
            boolean floor = false;
            boolean ceiling = false;
            double yMaxFloor = minY;

            // for each box, compute collision.
            AxisAlignedBB aabb = box;

            dx = 10e3;
            dz = 10e3;
            boolean thisFloor = false;
            boolean thisCieling = false;
            boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                    || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

            boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                    || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

            boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                    || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

            collidesZ = collidesZ && (collidesX || collidesY);
            collidesX = collidesX && (collidesZ || collidesY);

            if (collidesX && collidesZ && yTop >= aabb.maxY
                    && boundingBox.minY - entity.stepHeight - 0 <= aabb.maxY - diffs.y)
            {
                if (!floor)
                {
                    temp1.y = (float) Math.max(aabb.maxY - boundingBox.minY, temp1.y);
                }
                floor = true;
                thisFloor = aabb.maxY >= yMaxFloor;
                if (thisFloor) yMaxFloor = aabb.maxY;
            }
            if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
            {
                if (!(floor || ceiling))
                {
                    double dy = aabb.minY - boundingBox.maxY - diffs.y;
                    temp1.y = (float) Math.min(dy, temp1.y);
                }
                thisCieling = !(thisFloor || floor);
                ceiling = true;
            }

            boolean vert = thisFloor || thisCieling;

            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX)
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX)
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (Math.abs(dx) > Math.abs(dz) && dx < 10e2 || dx == 10e3 && dz < 10e2)
            {
                temp1.z = (float) dz;
            }
            else if (dx < 10e2)
            {
                temp1.x = (float) dx;
            }

            // If entity has collided, adjust motion accordingly.
            boolean collidedY = false;
            if (temp1.lengthSquared() > 0)
            {
                if (temp1.y >= 0)
                {
                    entity.onGround = true;
                    entity.fallDistance = 0;
                    entity.fall(entity.fallDistance, 0);
                }
                else if (temp1.y < 0)
                {
                    boolean below = entity.posY + entity.height - (entity.motionY + 0) < tile.getPos().getY();
                    if (below)
                    {
                        temp1.y = 0;
                    }
                }
                if (temp1.x != 0) entity.motionX = 0;
                if (temp1.y != 0) entity.motionY = 0;
                if (temp1.z != 0) entity.motionZ = 0;
                if (temp1.y != 0) collidedY = true;
                // temp1.add(new Vector3f((float) entity.posX, (float)
                // entity.posY,
                // (float) entity.posZ));
                // entity.setPosition(temp1.x, temp1.y, temp1.z);
                CompatWrapper.moveEntitySelf(entity, temp1.x, temp1.y, temp1.z);
            }
            // Extra stuff to do with players.
            if (entity instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) entity;
                if (Math.abs(player.motionY) < 0.1 && !player.capabilities.isFlying)
                {
                    entity.onGround = true;
                    entity.fallDistance = 0;
                }
                // Meed to set floatingTickCount to prevent being kicked for
                // flying.
                if (!player.capabilities.isCreativeMode && !player.getEntityWorld().isRemote)
                {
                    EntityPlayerMP entityplayer = (EntityPlayerMP) player;
                    if (collidedY) entityplayer.connection.floatingTickCount = 0;
                }
                else if (player.getEntityWorld().isRemote)
                {
                }
            }
        }
    }

}

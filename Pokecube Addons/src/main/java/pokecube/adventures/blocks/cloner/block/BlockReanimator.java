package pokecube.adventures.blocks.cloner.block;

import static java.lang.Math.max;

import java.util.List;

import javax.vecmath.Vector3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class BlockReanimator extends BlockBase
{
    public BlockReanimator()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCloner();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUICLONER_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
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
                entity.move(MoverType.SELF, temp1.x, temp1.y, temp1.z);
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

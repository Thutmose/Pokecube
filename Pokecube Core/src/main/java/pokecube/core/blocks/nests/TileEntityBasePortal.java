package pokecube.core.blocks.nests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.dimensions.secretpower.SecretBaseManager;
import pokecube.core.world.dimensions.secretpower.SecretBaseManager.Coordinate;

public class TileEntityBasePortal extends TileEntityOwnable
{
    public boolean exit        = false;
    public boolean sendToUsers = false;
    public int     exitDim     = 0;

    public void transferPlayer(EntityPlayer playerIn)
    {
        if (!sendToUsers && placer == null) return;
        String owner = sendToUsers ? playerIn.getCachedUniqueIdString() : placer.toString();
        BlockPos exitLoc = PokecubeDimensionManager.getBaseEntrance(owner, world.provider.getDimension());
        if (exitLoc == null)
        {
            PokecubeDimensionManager.setBaseEntrance(owner, world.provider.getDimension(), pos);
            exitLoc = pos;
        }
        double dist = exitLoc.distanceSq(pos);
        if (dist > 36)
        {
            world.setBlockState(pos, Blocks.STONE.getDefaultState());
            playerIn.sendMessage(new TextComponentTranslation("pokemob.removebase.stale"));
        }
        else PokecubeDimensionManager.sendToBase(owner, playerIn, exitLoc.getX(), exitLoc.getY(), exitLoc.getZ(),
                exitDim);
    }

    @Override
    public boolean shouldBreak()
    {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        this.sendToUsers = tagCompound.getBoolean("allPlayer");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("allPlayer", sendToUsers);
        return tagCompound;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        Coordinate c = new Coordinate(getPos().getX(), getPos().getY(), getPos().getZ(),
                getWorld().provider.getDimension());
        SecretBaseManager.removeBase(c);
    }

    @Override
    public void validate()
    {
        super.validate();
        Coordinate c = new Coordinate(getPos().getX(), getPos().getY(), getPos().getZ(),
                getWorld().provider.getDimension());
        SecretBaseManager.addBase(c);
    }
}

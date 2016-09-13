package pokecube.core.blocks.nests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

public class TileEntityBasePortal extends TileEntityOwnable
{
    public boolean exit = false;

    public void transferPlayer(EntityPlayer playerIn)
    {
        if (placer == null) return;
        String owner = placer.toString();
        BlockPos pos1 = PokecubeDimensionManager.getBaseEntrance(owner, 0);
        int dim = PokecubeDimensionManager.getDimensionForPlayer(owner);
        if (pos1 == null)
        {
            pos1 = worldObj.getMinecraftServer().worldServerForDimension(0).getSpawnPoint();
        }
        int current = playerIn.dimension;
        if (current != dim) PokecubeDimensionManager.sendToBase(owner, playerIn, pos);
        else
        {
            Transporter.teleportEntity(playerIn, Vector3.getNewVector().set(pos1), 0, false);
        }
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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }
}

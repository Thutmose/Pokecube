package pokecube.core.moves.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.api.maths.Vector3;

public class PokecubeTeleporter extends Teleporter
{

    public PokecubeTeleporter(WorldServer world)
    {
        super(world);
    }

    /** Place an entity in a nearby portal, creating one if necessary. */
    @Override
    public void placeInPortal(Entity entity, float f0)
    {
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getWorld(entity.dimension);
        Vector3 v = Vector3.getNewVector().set(world.getSpawnPoint()).addTo(0, entity.height, 0);

        if (!v.isClearOfBlocks(world)) v.getTopBlockPos(world).add(1, entity.height);
        Vector3 temp = Vector3.getNewVector();

        if (Vector3.getNextSurfacePoint(world, v, temp.set(EnumFacing.DOWN), v.y) == null)
        {
            // v.set(x,y,z);
            if (Vector3.getNextSurfacePoint(world, v, temp.set(EnumFacing.DOWN), v.y) == null)
            {
                for (int i = -1; i <= 1; i++)
                    for (int j = -1; j <= 1; j++)
                    {
                        Vector3 v1 = temp.set(v).addTo(i, -entity.height, j);
                        v1.setBlock(world, Blocks.OBSIDIAN);
                    }
            }
        }
        v.moveEntity(entity);
    }

}

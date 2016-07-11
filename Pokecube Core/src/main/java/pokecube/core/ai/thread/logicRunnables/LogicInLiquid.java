package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class LogicInLiquid extends LogicBase
{

    public LogicInLiquid(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @Override
    public void doLogic()
    {
        if (!((PokeNavigator) entity.getNavigator()).pathfinder.cacheLock[0])
            world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null) return;
        boolean lava = false;
        boolean water = false;
        if (entity.getParts() != null)
        {
            for (Entity e : entity.getParts())
            {
                Matrix3 box = new Matrix3();
                box.set(e.getEntityBoundingBox());
                if (!lava) lava = lava || box.isInMaterial(world, Vector3.empty, Vector3.empty, Material.LAVA);
                if (!water) water = water || box.isInMaterial(world, Vector3.empty, Vector3.empty, Material.WATER);
            }
        }
        else
        {
            Matrix3 box = new Matrix3();
            box.set(entity.getEntityBoundingBox());
            if (!lava) lava = lava || box.isInMaterial(world, Vector3.empty, Vector3.empty, Material.LAVA);
            if (!water) water = water || box.isInMaterial(world, Vector3.empty, Vector3.empty, Material.WATER);
        }
        setPokemobAIState(INLAVA, lava);
        setPokemobAIState(INWATER, water);
    }

}

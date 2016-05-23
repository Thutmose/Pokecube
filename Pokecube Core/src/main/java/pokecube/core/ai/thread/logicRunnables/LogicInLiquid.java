package pokecube.core.ai.thread.logicRunnables;

import java.util.HashMap;

import net.minecraft.block.material.Material;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.entity.IMultibox;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class LogicInLiquid extends LogicBase
{

    public LogicInLiquid(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doLogic()
    {
        if (!((PokeNavigator) entity.getNavigator()).pathfinder.cacheLock[0])
            world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null) return;
        Vector3 here = Vector3.getNewVector().set(pokemob);
        ((IMultibox) pokemob).setBoxes();
        ((IMultibox) pokemob).setOffsets();
        HashMap<String, Matrix3> boxes = (HashMap<String, Matrix3>) ((IMultibox) pokemob).getBoxes().clone();
        HashMap<String, Vector3> offsets = (HashMap<String, Vector3>) ((IMultibox) pokemob).getOffsets().clone();
        boolean lava = false;
        boolean water = false;
        for (String s : boxes.keySet())
        {
            Matrix3 box = boxes.get(s);
            Vector3 offset = offsets.get(s);
            if (offset == null) offset = Vector3.empty;
            lava = lava || box.isInMaterial(world, here, offset, Material.LAVA);
            water = water || box.isInMaterial(world, here, offset, Material.WATER);
        }
        setPokemobAIState(INLAVA, lava);
        setPokemobAIState(INWATER, water);
    }

}

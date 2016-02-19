package pokecube.core.ai.thread.logicRunnables;

import java.util.Map;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.entity.IMultibox;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class LogicCollision extends LogicBase
{
    public final double[]  diffsArr  = new double[3];
    public final double[]  prevDiffs = new double[3];
    public final int[]     time      = new int[1];
    /** Index 0 = reading/writing from entity, 1 = setting/reading diffs, 2 =
     * ready for pokemob to update */
    public final boolean[] lock      = { true, false, false };
    final IMultibox        e;

    public LogicCollision(IPokemob pokemob_)
    {
        super(pokemob_);
        e = (IMultibox) pokemob;
        prevDiffs[2] = Double.MIN_VALUE;
    }

    @Override
    public void doLogic()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || !lock[1]) // || prevDiffs[2]!=Double.MIN_VALUE)
            return;
        // lock[1] = true;

        Vector3 here = Vector3.getNewVector().set(e);
        double x, y, z;

        x = diffsArr[0];
        y = diffsArr[1];
        z = diffsArr[2];
        prevDiffs[0] = x;
        prevDiffs[1] = y;
        prevDiffs[2] = z;

        // TODO re-write this to do entity collision instead of tile collision.
        Vector3 diffs = Vector3.getNewVector();
        e.setBoxes();
        e.setOffsets();
        Map<String, Matrix3> boxes = e.getBoxes();
        Map<String, Vector3> offsets = e.getOffsets();
        for (String s : boxes.keySet())
        {
            diffs.set(x, y, z);
            Matrix3 box = boxes.get(s);
            Vector3 offset = offsets.get(s);
            if (offset == null) offset = Vector3.empty;
            Vector3 pos = offset.add(here);
            diffs.set(box.doTileCollision(world, (Entity) e, pos, diffs));
            x = diffs.x;
            y = diffs.y;
            z = diffs.z;
        }
        diffsArr[0] = x;
        diffsArr[1] = y;
        diffsArr[2] = z;

        lock[2] = true;
        lock[1] = false;
        lock[0] = false;
    }

}

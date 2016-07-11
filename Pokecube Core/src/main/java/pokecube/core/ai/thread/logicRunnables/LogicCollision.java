package pokecube.core.ai.thread.logicRunnables;

import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;

public class LogicCollision extends LogicBase
{
    final EntityPokemobBase collider;

    public LogicCollision(IPokemob pokemob_)
    {
        super(pokemob_);
        collider = (EntityPokemobBase) pokemob;
    }

    @Override
    public void doLogic()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null) return;
//        float diff = Math.max(Math.max(collider.length, collider.height), collider.width);
//        Vector3 v = Vector3.getNewVector().set(collider);
//        AxisAlignedBB box = v.getAABB().expand(5 + diff, 5 + diff, 5 + diff);
//        final List<AxisAlignedBB> aabbs = new Matrix3().getCollidingBoxes(box, entity.worldObj, world);
//        Matrix3.mergeAABBs(aabbs, 0.0, 0.0, 0.0);
//        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
//        {
//            @Override
//            public void run()
//            {
////                collider.setTileCollsionBoxes(aabbs);
//            }
//        });
    }

}

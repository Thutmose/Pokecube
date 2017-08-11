package pokecube.core.ai.thread.logicRunnables;

import java.util.List;
import java.util.Vector;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

/** This deals with setting the list of colliding boxes for the pokemobs.
 * Attempts were made to make this work on seperate thread, but they did not go
 * too well. For now this runs on the main thread. */
public class LogicCollision extends LogicBase
{
    final EntityPokemob   collider;
    Vector3               lastCheck     = Vector3.getNewVector();
    Vector<AxisAlignedBB> boxes;
    int                   lastTickCheck = 0;

    public LogicCollision(IPokemob pokemob_)
    {
        super(pokemob_);
        if (entity instanceof EntityPokemob) collider = (EntityPokemob) entity;
        else collider = null;
    }

    @Override
    public void doLogic()
    {
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        if (collider == null) return;
        Vector3 vec = Vector3.getNewVector();
        Vector3 vec2 = Vector3.getNewVector();
        List<AxisAlignedBB> aabbs;
        double x = pokemob.getPokedexEntry().width * pokemob.getSize();
        double z = pokemob.getPokedexEntry().length * pokemob.getSize();
        double y = pokemob.getPokedexEntry().height * pokemob.getSize();
        double v = vec.setToVelocity(collider).mag();
        vec.set(collider);
        vec2.set(x + v, y + v, z + v);
        if (vec.distToSq(lastCheck) < vec2.magSq() / 2 && collider.ticksExisted < lastTickCheck) { return; }
        lastTickCheck = collider.ticksExisted + 100;
        lastCheck.set(vec);

        Matrix3 mainBox = new Matrix3();
        Vector3 offset = Vector3.getNewVector();
        mainBox.boxMin().clear();
        mainBox.boxMax().x = x;
        mainBox.boxMax().z = y;
        mainBox.boxMax().y = z;
        offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
        mainBox.set(2, mainBox.rows[2].set(0, 0, (-collider.rotationYaw) * Math.PI / 180));
        mainBox.addOffsetTo(offset).addOffsetTo(vec);
        AxisAlignedBB box = mainBox.getBoundingBox();
        if (box.maxX - box.minX > 3)
        {
            double meanX = box.minX + (box.maxX - box.minX) / 2;
            box = new AxisAlignedBB(meanX - 3, box.minY, box.minZ, meanX + 3, box.maxY, box.maxZ);
        }
        if (box.maxZ - box.minZ > 3)
        {
            double meanZ = box.minZ + (box.maxZ - box.minZ) / 2;
            box = new AxisAlignedBB(box.minX, box.minY, meanZ - 3, box.maxX, box.maxY, meanZ + 3);
        }
        if (box.maxY - box.minY > 3)
        {
            box = box.setMaxY(box.minY + 3);
        }
        AxisAlignedBB box1 = box.grow(2 + x, 2 + y, 2 + z);

        box1 = box1.expand(collider.motionX, collider.motionY, collider.motionZ);
        aabbs = mainBox.getCollidingBoxes(box1, world, world);
        // Matrix3.mergeAABBs(aabbs, x/2, y/2, z/2);
        Matrix3.expandAABBs(aabbs, box);
        if (box1.getAverageEdgeLength() < 3) Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        collider.setTileCollsionBoxes(aabbs);
    }

}

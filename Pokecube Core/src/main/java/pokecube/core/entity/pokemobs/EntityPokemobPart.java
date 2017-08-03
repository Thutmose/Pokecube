package pokecube.core.entity.pokemobs;

import javax.vecmath.Vector3f;

import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;

public class EntityPokemobPart extends MultiPartEntityPart// implements IPokemob
{
    public final IPokemob      parent;
    public final Vector3f      offset;
    public final AxisAlignedBB defaultBox;

    public EntityPokemobPart(World world)
    {
        super(null, null, 0, 0);
        this.parent = null;
        this.offset = null;
        this.defaultBox = null;
    }

    public EntityPokemobPart(IPokemob parent, String partName, Vector3f offset, Vector3f[] dimensions)
    {
        super((IEntityMultiPart) parent.getEntity(), partName, 1, 1);
        this.parent = parent;
        this.offset = offset;
        defaultBox = new AxisAlignedBB(dimensions[0].x, dimensions[0].y, dimensions[0].z, dimensions[1].x,
                dimensions[1].y, dimensions[1].z);
    }
}

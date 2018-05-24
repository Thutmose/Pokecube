package pokecube.core.ai.utils.pathing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.ai.utils.pathing.node.MultiNodeWrapper;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class MultiNodeNavigator extends PathNavigate2
{
    public final NodeProcessor a;
    public final NodeProcessor b;
    private final boolean      canFly;

    private static void setFinal(Field field, Object objTo, Object objFrom) throws Exception
    {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(objTo, field.get(objFrom));
    }

    public MultiNodeNavigator(EntityLiving entityIn, World worldIn, NodeProcessor a, NodeProcessor b, boolean canFly)
    {
        super(entityIn, worldIn);
        this.a = a;
        this.b = b;
        try
        {
            Field points = ReflectionHelper.findField(NodeProcessor.class, "pointMap", "field_176167_b", "c");
            setFinal(points, b, a);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error copying point maps for pathing", e);
        }
        this.canFly = canFly;
    }

    @Override
    protected PathFinder getPathFinder()
    {
        this.nodeProcessor = new MultiNodeWrapper(this);
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }

    @Override
    protected boolean canNavigate()
    {
        return true;
    }

    /** Returns true when an entity of specified size could safely walk in a
     * straight line between the two points. Args: pos1, pos2, entityXSize,
     * entityYSize, entityZSize */
    @Override
    public boolean isDirectPathBetweenPoints(Vec3d start, Vec3d end, int sizeX, int sizeY, int sizeZ)
    {
        Vector3 v1 = Vector3.getNewVector().set(start);
        Vector3 v2 = Vector3.getNewVector().set(end);
        boolean ground = !canFly;
        if (ground && ((int) start.y) != ((int) end.y)) { return false; }
        double dx = sizeX / 2d;
        double dy = sizeY;
        double dz = sizeZ / 2d;

        v1.set(start).addTo(0, 0, 0);
        v2.set(end).addTo(0, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, dy, 0);
        v2.set(end).addTo(0, dy, 0);
        if (!v1.isVisible(world, v2)) return false;

        v1.set(start).addTo(dx, 0, 0);
        v2.set(end).addTo(dx, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(-dx, 0, 0);
        v2.set(end).addTo(-dx, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, 0, dz);
        v2.set(end).addTo(0, 0, dz);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, 0, -dz);
        v2.set(end).addTo(0, 0, -dz);
        return true;
    }

}

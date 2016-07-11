package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class LogicFloatFlySwim extends LogicBase
{
    final private EntityAnimal entity;
    final IPokemob             pokemob;
    PokedexEntry               entry;
    Vector3                    v = Vector3.getNewVector();

    public LogicFloatFlySwim(EntityAnimal entity)
    {
        super((IPokemob) entity);
        this.entity = entity;
        pokemob = (IPokemob) entity;
        entry = pokemob.getPokedexEntry();
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        if (!shouldRun()) return;
        entry = pokemob.getPokedexEntry();
        if (pokemob.getTransformedTo() instanceof IPokemob)
            entry = ((IPokemob) pokemob.getTransformedTo()).getPokedexEntry();
        boolean canFloat = entry.floats();
        Vector3 here = Vector3.getNewVector();
        here.set(pokemob);

        if (canFloat && !pokemob.getPokemonAIState(IMoveConstants.INWATER))
        {
            float floatHeight = (float) entry.preferedHeight;
            Vector3 down = Vector3.getNextSurfacePoint(entity.worldObj, here.set(pokemob), Vector3.secondAxisNeg,
                    floatHeight);
            if (down != null) here.set(down);
            if (!(here.getBlock(entity.worldObj)).isReplaceable(entity.worldObj, here.getPos())
                    && !pokemob.getPokemonAIState(IMoveConstants.SLEEPING)
                    || here.getBlockState(entity.worldObj).getMaterial().isLiquid())
            {
                entity.motionY += 0.01;
            }
            else entity.motionY -= 0.01;
            if (down == null || pokemob.getPokemonAIState(IMoveConstants.SITTING))
            {
                entity.motionY -= 0.02;
            }
            here.set(pokemob);
        }

        if ((entry.floats() || entry.flys()) && !pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            float floatHeight = (float) entry.preferedHeight;
            Path path = entity.getNavigator().getPath();
            if (path != null)
            {
                Vector3 end = Vector3.getNewVector().set(path.getFinalPathPoint());
                double dhs = (here.x - end.x) * (here.x - end.x) + (here.z - end.z) * (here.z - end.z);
                double dvs = (here.y - end.y) * (here.y - end.y);
                double width = Math.max(0.5, pokemob.getSize() * entry.length / 4);
                if (dhs < width * width && dvs <= floatHeight * floatHeight)
                {
                    entity.getNavigator().clearPathEntity();
                }
            }
        }
        canFloat = entry.flys();
        if (canFloat && here.offset(EnumFacing.DOWN).getBlockState(entity.getEntityWorld()).getMaterial().isLiquid())
        {
            if (entity.motionY < -0.1) entity.motionY = 0;
            entity.motionY += 0.05;
        }
    }

    public boolean shouldRun()
    {
        return !entity.isBeingRidden();
    }

    @Override
    public void doLogic()
    {
    }

}

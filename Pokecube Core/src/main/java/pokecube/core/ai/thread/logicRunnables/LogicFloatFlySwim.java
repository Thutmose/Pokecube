package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

/** This is used instead of a Swimming AI task. It manages making mobs "jump" to
 * swim while in water. It also manages making floating mobs float a certain
 * distance above the ground, and manages terminating wandering paths for
 * floating, flying and swimming mobs if they get sufficiently close to their
 * destinations. */
public class LogicFloatFlySwim extends LogicBase
{
    Vector3 here = Vector3.getNewVector();

    public LogicFloatFlySwim(IPokemob entity)
    {
        super(entity);
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        if (!shouldRun()) return;
        here.set(entity);
        doFloatFly(here);
        doSwim(here);
    }

    public boolean shouldRun()
    {
        return !entity.isBeingRidden();
    }

    @Override
    public void doLogic()
    {
    }

    private void doSwim(Vector3 here)
    {
        IPokemob pokemob = this.pokemob;
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null) pokemob = transformed;
        PokedexEntry entry = pokemob.getPokedexEntry();
        boolean isWaterMob = pokemob.getPokedexEntry().swims();
        if (!isWaterMob && (this.entity.isInWater() || this.entity.isInLava()))
        {
            if (this.entity.getRNG().nextFloat() < 0.8F)
            {
                this.entity.getJumpHelper().setJumping();
            }
        }
        else if (isWaterMob)
        {
            if (!pokemob.getPokemonAIState(IMoveConstants.ANGRY))
            {
                float floatHeight = (float) 0.5;
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
        }
    }

    private void doFloatFly(Vector3 here)
    {
        IPokemob pokemob = this.pokemob;
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null) pokemob = transformed;
        PokedexEntry entry = pokemob.getPokedexEntry();
        boolean canFloat = entry.floats();
        if (canFloat && !pokemob.getPokemonAIState(IMoveConstants.INWATER))
        {
            float floatHeight = (float) entry.preferedHeight;
            Vector3 down = Vector3.getNextSurfacePoint(entity.getEntityWorld(), here.set(pokemob.getEntity()),
                    Vector3.secondAxisNeg, floatHeight);
            if (down != null) here.set(down);
            if (!(here.getBlock(entity.getEntityWorld())).isReplaceable(entity.getEntityWorld(), here.getPos())
                    && !pokemob.getPokemonAIState(IMoveConstants.SLEEPING)
                    || here.getBlockState(entity.getEntityWorld()).getMaterial().isLiquid())
            {
                entity.motionY += 0.005;
            }
            else entity.motionY -= 0.005;
            if (down == null || pokemob.getPokemonAIState(IMoveConstants.SITTING))
            {
                entity.motionY -= 0.02;
            }
            here.set(pokemob.getEntity());
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
        canFloat = entry.flys() || entry.floats();
        if (canFloat && here.offset(EnumFacing.DOWN).getBlockState(entity.getEntityWorld()).getMaterial().isLiquid())
        {
            if (entity.motionY < -0.1) entity.motionY = 0;
            entity.motionY += 0.05;
        }
    }
}

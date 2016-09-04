package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;

public class LogicMountedControl extends LogicBase
{
    public boolean leftInputDown    = false;
    public boolean rightInputDown   = false;
    public boolean forwardInputDown = false;
    public boolean backInputDown    = false;
    public boolean upInputDown      = false;
    public boolean downInputDown    = false;

    public LogicMountedControl(IPokemob pokemob_)
    {
        super(pokemob_);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        if (!entity.isBeingRidden()) return;
        boolean move = false;

        boolean shouldControl = entity.onGround;
        if (pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys()) shouldControl = true;

        if (forwardInputDown)
        {
            move = true;
            float f = 0.2f;
            if (shouldControl)
            {
                if (!entity.onGround) f *= 2;
                entity.motionX += (double) (MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f);
                entity.motionZ += (double) (MathHelper.cos(entity.rotationYaw * 0.017453292F) * f);
            }
        }
        if (backInputDown)
        {
            move = true;
            float f = -0.1f;
            if (shouldControl)
            {
                entity.motionX += (double) (MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f);
                entity.motionZ += (double) (MathHelper.cos(entity.rotationYaw * 0.017453292F) * f);
            }
        }
        if (upInputDown)
        {
            if (entity.onGround)
            {
                entity.setJumping(true);
            }
            else if (shouldControl)
            {
                entity.motionY += 0.1;
            }
        }
        if (downInputDown)
        {
            if (shouldControl && !entity.onGround)
            {
                entity.motionY -= 0.1;
            }
        }
        if (!move)
        {
            entity.motionX *= 0.5;
            entity.motionZ *= 0.5;
        }
        if (leftInputDown)
        {
            entity.rotationYaw -= 5;
        }
        if (rightInputDown)
        {
            entity.rotationYaw += 5;
        }
    }

    @Override
    public void doLogic()
    {
        // TODO Auto-generated method stub

    }

}

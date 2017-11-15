package pokecube.core.ai.thread.logicRunnables;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

/** This manages the ridden controls of the pokemob. The booleans are set on the
 * client side, then sent via a packet to the server, and then the mob is moved
 * accordingly. */
public class LogicMountedControl extends LogicBase
{
    public boolean leftInputDown    = false;
    public boolean rightInputDown   = false;
    public boolean forwardInputDown = false;
    public boolean backInputDown    = false;
    public boolean upInputDown      = false;
    public boolean downInputDown    = false;
    public boolean followOwnerLook  = false;
    public double  throttle         = 0.5;

    public LogicMountedControl(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        Entity rider = entity.getControllingPassenger();
        pokemob.setPokemonAIState(IMoveConstants.CONTROLLED, rider != null);
        if (rider == null) return;
        Config config = PokecubeCore.instance.getConfig();
        boolean move = false;
        entity.rotationYaw = pokemob.getHeading();
        boolean shouldControl = entity.onGround || pokemob.floats();
        boolean verticalControl = false;
        boolean waterSpeed = false;
        boolean airSpeed = !entity.onGround;
        if (pokemob.canUseFly())
            shouldControl = verticalControl = PokecubeCore.core.getConfig().flyEnabled || shouldControl;
        if ((pokemob.canUseSurf() || pokemob.canUseDive()) && (waterSpeed = entity.isInWater()))
            shouldControl = verticalControl = PokecubeCore.core.getConfig().surfEnabled || shouldControl;

        if (waterSpeed) airSpeed = false;

        Entity controller = rider;
        if (pokemob.getPokedexEntry().shouldDive)
        {
            PotionEffect vision = new PotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 300, 1, true,
                    false);
            ItemStack stack = new ItemStack(Blocks.BARRIER);
            vision.setCurativeItems(Lists.newArrayList(stack));
            for (Entity e : entity.getRecursivePassengers())
            {
                if (e instanceof EntityLivingBase)
                {
                    if (entity.isInWater())
                    {
                        ((EntityLivingBase) e).addPotionEffect(vision);
                        ((EntityLivingBase) e).setAir(300);
                    }
                    else((EntityLivingBase) e).curePotionEffects(stack);
                }
            }
        }
        float speedFactor = (float) (1 + Math.sqrt(pokemob.getPokedexEntry().getStatVIT()) / (10F));
        float moveSpeed = (float) (0.25f * throttle * speedFactor);
        if (forwardInputDown)
        {
            move = true;
            float f = moveSpeed / 2;

            if (airSpeed) f *= config.flySpeedFactor;
            else if (waterSpeed) f *= config.surfSpeedFactor;
            else f *= config.groundSpeedFactor;

            if (shouldControl)
            {
                if (!entity.onGround) f *= 2;
                entity.motionX += MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f;
                entity.motionZ += MathHelper.cos(entity.rotationYaw * 0.017453292F) * f;
            }
            else if (entity.isInLava() || entity.isInWater())
            {
                f *= 0.1;
                entity.motionX += MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f;
                entity.motionZ += MathHelper.cos(entity.rotationYaw * 0.017453292F) * f;
            }
        }
        if (backInputDown)
        {
            move = true;
            float f = -moveSpeed / 4;
            if (shouldControl)
            {
                entity.motionX += MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f;
                entity.motionZ += MathHelper.cos(entity.rotationYaw * 0.017453292F) * f;
            }
        }
        if (upInputDown)
        {
            if (entity.onGround)
            {
                entity.getJumpHelper().setJumping();
            }
            else if (verticalControl)
            {
                entity.motionY += 0.1 * throttle;
            }
            else if (entity.isInLava() || entity.isInWater())
            {
                entity.motionY += 0.05 * throttle;
            }
        }
        if (downInputDown)
        {
            if (verticalControl && !entity.onGround)
            {
                entity.motionY -= 0.1 * throttle;
            }
        }
        else if (!verticalControl && !entity.onGround)
        {
            entity.motionY -= 0.1;
        }
        if (!followOwnerLook)
        {// TODO some way to make this change based on how long button is held?
            if (leftInputDown)
            {
                pokemob.setHeading(pokemob.getHeading() - 5);
            }
            if (rightInputDown)
            {
                pokemob.setHeading(pokemob.getHeading() + 5);
            }
        }
        else if (!entity.getPassengers().isEmpty())
        {
            pokemob.setHeading(controller.rotationYaw);
            float f = moveSpeed / 2;
            if (leftInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (!entity.onGround) f *= 2;
                    entity.motionX += MathHelper.cos(-entity.rotationYaw * 0.017453292F) * f;
                    entity.motionZ += MathHelper.sin(entity.rotationYaw * 0.017453292F) * f;
                }
                else if (entity.isInLava() || entity.isInWater())
                {
                    f *= 0.1;
                    entity.motionX += MathHelper.cos(-entity.rotationYaw * 0.017453292F) * f;
                    entity.motionZ += MathHelper.sin(entity.rotationYaw * 0.017453292F) * f;
                }
            }
            if (rightInputDown)
            {
                move = true;
                if (shouldControl)
                {
                    if (!entity.onGround) f *= 2;
                    entity.motionX -= MathHelper.cos(-entity.rotationYaw * 0.017453292F) * f;
                    entity.motionZ -= MathHelper.sin(entity.rotationYaw * 0.017453292F) * f;
                }
                else if (entity.isInLava() || entity.isInWater())
                {
                    f *= 0.1;
                    entity.motionX -= MathHelper.cos(-entity.rotationYaw * 0.017453292F) * f;
                    entity.motionZ -= MathHelper.sin(entity.rotationYaw * 0.017453292F) * f;
                }
            }
        }
        if (!move)
        {
            entity.motionX *= 0.5;
            entity.motionZ *= 0.5;
        }
        // Sync the rotations.
        entity.setRenderYawOffset(pokemob.getHeading());
        entity.setRotationYawHead(pokemob.getHeading());
    }

    @Override
    public void doLogic()
    {
    }

}

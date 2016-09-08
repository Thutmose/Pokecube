package pokecube.core.ai.thread.logicRunnables;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
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
        if (pokemob.getPokedexEntry().shouldDive && entity.isInWater()) shouldControl = true;

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
                    if (entity.isInWater()) ((EntityLivingBase) e).addPotionEffect(vision);
                    else((EntityLivingBase) e).curePotionEffects(stack);
                }
            }
        }
        float speedFactor = (float) entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .getAttributeValue();
        if (forwardInputDown)
        {
            move = true;
            float f = speedFactor / 2;
            if (shouldControl)
            {
                if (!entity.onGround) f *= 2;
                entity.motionX += (double) (MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f);
                entity.motionZ += (double) (MathHelper.cos(entity.rotationYaw * 0.017453292F) * f);
            }
            else if (entity.isInLava() || entity.isInWater())
            {
                f *= 0.1;
                entity.motionX += (double) (MathHelper.sin(-entity.rotationYaw * 0.017453292F) * f);
                entity.motionZ += (double) (MathHelper.cos(entity.rotationYaw * 0.017453292F) * f);
            }
        }
        if (backInputDown)
        {
            move = true;
            float f = -speedFactor / 4;
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
            else if (entity.isInLava() || entity.isInWater())
            {
                entity.motionY += 0.05;
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
    }

}

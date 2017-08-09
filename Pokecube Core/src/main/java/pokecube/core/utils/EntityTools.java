package pokecube.core.utils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.interfaces.IPokemob;

public class EntityTools
{
    public static void copyEntityTransforms(EntityLivingBase to, EntityLivingBase from)
    {
        to.setEntityId(from.getEntityId());
        to.posX = from.posX;
        to.posY = from.posY;
        to.posZ = from.posZ;
        to.motionX = from.motionX;
        to.motionY = from.motionY;
        to.motionZ = from.motionZ;
        to.rotationPitch = from.rotationPitch;
        to.ticksExisted = from.ticksExisted;
        to.rotationYaw = from.rotationYaw;
        to.setRotationYawHead(from.getRotationYawHead());
        to.dimension = from.dimension;
        to.prevRotationPitch = from.prevRotationPitch;
        to.prevRotationYaw = from.prevRotationYaw;
        to.prevRotationYawHead = from.prevRotationYawHead;
        to.onGround = from.onGround;
        to.prevLimbSwingAmount = from.prevLimbSwingAmount;
        to.limbSwing = from.limbSwing;
        to.limbSwingAmount = from.limbSwingAmount;
        to.prevDistanceWalkedModified = from.prevDistanceWalkedModified;
        to.lastTickPosX = from.lastTickPosX;
        to.lastTickPosY = from.lastTickPosY;
        to.lastTickPosZ = from.lastTickPosZ;
        to.prevRenderYawOffset = from.prevRenderYawOffset;
        to.renderYawOffset = from.renderYawOffset;
    }

    public static void copyEntityData(EntityLivingBase to, EntityLivingBase from)
    {
        NBTTagCompound tag = new NBTTagCompound();
        from.writeEntityToNBT(tag);
        to.readEntityFromNBT(tag);
    }

    public static void copy(IPokemob from, IPokemob to)
    {
        to.readPokemobData(from.writePokemobData());
    }
}

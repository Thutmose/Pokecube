package pokecube.pokeplayer;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class Proxy
{
    Map<EntityPlayer, IPokemob> playerMap = Maps.newHashMap();

    public void init()
    {

    }

    public IPokemob getPokemob(EntityPlayer player)
    {
        NBTTagCompound nbt = player.getEntityData();
        boolean isPokemob = nbt.getBoolean("isPokemob");
        if (!isPokemob) return null;
        if (playerMap.containsKey(player))
        {
            IPokemob ret = playerMap.get(player);
            copyTransform((EntityLivingBase) ret, player);
            return ret;
        }
        if (nbt.hasKey("Pokemob"))
        {
            NBTTagCompound poketag = nbt.getCompoundTag("Pokemob");
            int number = poketag.getInteger("pokenumber");
            Entity pokemob = PokecubeMod.core.createEntityByPokedexNb(number, player.worldObj);
            pokemob.readFromNBT(poketag);
            copyTransform((EntityLivingBase) pokemob, player);
            return (IPokemob) pokemob;
        }
        return null;
    }

    private void copyTransform(EntityLivingBase to, EntityPlayer from)
    {
        to.rotationPitch = from.rotationPitch;
        to.rotationYaw = from.rotationYaw;
        to.setRotationYawHead(from.getRotationYawHead());
        to.dimension = from.dimension;
        to.prevRotationPitch = from.prevRotationPitch;
        to.prevRotationYaw = from.prevRotationYaw;
        to.onGround = from.onGround;
        to.prevLimbSwingAmount = from.prevLimbSwingAmount;
        to.limbSwing = from.limbSwing;
        to.limbSwingAmount = from.limbSwingAmount;
    }
}

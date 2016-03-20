package pokecube.pokeplayer;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class Proxy
{
    public Map<UUID, PokeInfo> playerMap = Maps.newHashMap();

    private void copyTransform(EntityLivingBase to, EntityPlayer from)
    {
        to.rotationPitch = from.rotationPitch;
        to.ticksExisted = from.ticksExisted;
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

    public void setPokemob(EntityPlayer player, IPokemob pokemob)
    {
        if (pokemob != null) setMapping(player, pokemob);
        else removeMapping(player);

        if (!player.worldObj.isRemote)
        {
            new PokePlayer.SendPacket(player);
        }
    }

    private void setMapping(EntityPlayer player, IPokemob pokemob)
    {
        PokeInfo info = new PokeInfo(pokemob, player);
        info.setPlayer(player);
        playerMap.put(player.getUniqueID(), info);
        player.getEntityData().setBoolean("isPokemob", true);
        NBTTagCompound poketag = new NBTTagCompound();
        poketag.setInteger("pokenum", pokemob.getPokedexNb());
        ((Entity) pokemob).writeToNBT(poketag);
        boolean fly = pokemob.getPokedexEntry().flys() || pokemob.getPokedexEntry().floats();
        if (fly)
        {
            System.out.println("fly " + player.capabilities.allowFlying + " " + player.capabilities.isFlying);
            player.capabilities.allowFlying = true;
            player.capabilities.isFlying = true;
        }
        player.getEntityData().setTag("Pokemob", poketag);
    }

    private void removeMapping(EntityPlayer player)
    {
        PokeInfo info = playerMap.remove(player.getUniqueID());
        if (info != null)
        {
            info.resetPlayer(player);
        }
        player.getEntityData().setBoolean("isPokemob", false);
        player.getEntityData().removeTag("Pokemob");
    }

    public IPokemob getPokemob(EntityPlayer player)
    {
        if (playerMap.containsKey(player.getUniqueID()))
        {
            PokeInfo info = playerMap.get(player.getUniqueID());
            info.setPlayer(player);
            IPokemob ret = info.pokemob;
            copyTransform((EntityLivingBase) ret, player);
            return ret;
        }
        else if (player.getEntityData().getBoolean("isPokemob"))
        {
            NBTTagCompound poketag = player.getEntityData().getCompoundTag("Pokemob");
            int num = poketag.getInteger("pokenum");
            Entity poke = PokecubeMod.core.createEntityByPokedexNb(num, player.worldObj);
            if (poke == null)
            {
                removeMapping(player);
                return null;
            }
            poke.readFromNBT(poketag);
            IPokemob pokemob = (IPokemob) poke;
            setMapping(player, pokemob);
            return pokemob;
        }
        return null;
    }

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static class PokeInfo
    {
        public final IPokemob     pokemob;
        public final PokedexEntry entry;
        public float              originalHeight;
        public float              originalWidth;

        public PokeInfo(IPokemob pokemob, EntityPlayer player)
        {
            this.pokemob = pokemob;
            this.entry = pokemob.getPokedexEntry();
            this.originalHeight = player.height;
            this.originalWidth = player.width;
        }

        public void resetPlayer(EntityPlayer player)
        {
            player.eyeHeight = player.getDefaultEyeHeight();
            float height = originalHeight;
            float width = originalWidth;
            System.out.println("Reset " + originalHeight);
            float f = player.width;
            if (width != player.width || height != player.height)
            {
                player.width = width;
                player.height = height;
                player.setEntityBoundingBox(
                        new AxisAlignedBB(player.getEntityBoundingBox().minX, player.getEntityBoundingBox().minY,
                                player.getEntityBoundingBox().minZ, player.getEntityBoundingBox().minX + player.width,
                                player.getEntityBoundingBox().minY + player.height,
                                player.getEntityBoundingBox().minZ + player.width));
                if (player.width > f && !player.worldObj.isRemote)
                {
                    player.moveEntity(f - player.width, 0.0D, f - player.width);
                }
            }
        }

        public void setPlayer(EntityPlayer player)
        {
            player.eyeHeight = ((EntityLivingBase) pokemob).getEyeHeight();
            float height = pokemob.getSize() * entry.height;
            float width = pokemob.getSize() * entry.width;
            float f = player.width;
            if (width != player.width || height != player.height)
            {
                player.width = width;
                player.height = height;
                player.setEntityBoundingBox(
                        new AxisAlignedBB(player.getEntityBoundingBox().minX, player.getEntityBoundingBox().minY,
                                player.getEntityBoundingBox().minZ, player.getEntityBoundingBox().minX + player.width,
                                player.getEntityBoundingBox().minY + player.height,
                                player.getEntityBoundingBox().minZ + player.width));
                if (player.width > f && !player.worldObj.isRemote)
                {
                    player.moveEntity(f - player.width, 0.0D, f - player.width);
                }
            }
            if (!player.worldObj.isRemote) System.out.println(player.getEntityBoundingBox());
        }
    }
}

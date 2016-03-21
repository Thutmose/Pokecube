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

    void copyTransform(EntityLivingBase to, EntityPlayer from)
    {
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
        to.prevRenderYawOffset = from.prevRenderYawOffset;
        to.prevRotationYawHead = from.prevRotationYawHead;
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
        info.pokemob.setPokemonOwner(player);
        info.pokemob.setPokemonNickname(player.getName());
        info.pokemob.setSize(info.pokemob.getSize());
        info.setPlayer(player);
        copyTransform((EntityLivingBase) info.pokemob, player);
        playerMap.put(player.getUniqueID(), info);
        player.getEntityData().setBoolean("isPokemob", true);
        NBTTagCompound poketag = new NBTTagCompound();
        poketag.setInteger("pokenum", pokemob.getPokedexNb());
        ((Entity) pokemob).writeToNBT(poketag);
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
            IPokemob ret = info.pokemob;
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

    public void postInit()
    {

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
            this.originalHeight = 1.8f;
            this.originalWidth = 0.6f;
        }

        public void resetPlayer(EntityPlayer player)
        {
            player.eyeHeight = player.getDefaultEyeHeight();
            float height = originalHeight;
            float width = originalWidth;
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
            boolean fly = pokemob.getPokedexEntry().flys() || pokemob.getPokedexEntry().floats();
            if (fly && player.capabilities.allowFlying && player.worldObj.isRemote
                    && !player.capabilities.isCreativeMode)
            {
                player.capabilities.allowFlying = false;
                player.sendPlayerAbilities();
            }
        }

        public void setPlayer(EntityPlayer player)
        {
            float height = pokemob.getSize() * entry.height;
            float width = pokemob.getSize() * entry.width;
            float f = player.width;
            if (width != player.width || height != player.height)
            {
                player.eyeHeight = ((EntityLivingBase) pokemob).getEyeHeight();
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
            boolean fly = pokemob.getPokedexEntry().flys() || pokemob.getPokedexEntry().floats();
            if (fly && !player.capabilities.allowFlying && player.worldObj.isRemote)
            {
                player.capabilities.allowFlying = true;
                player.sendPlayerAbilities();
            }
        }
    }
}

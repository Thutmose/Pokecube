package pokecube.pokeplayer;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.pokeplayer.inventory.ContainerPokemob;

public class Proxy implements IGuiHandler
{
    public static final int     POKEMOBGUI      = 0;

    private Map<UUID, PokeInfo> playerMap       = Maps.newHashMap();
    private Map<UUID, PokeInfo> playerMapClient = Maps.newHashMap();

    void copyTransform(EntityLivingBase to, EntityPlayer from)
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
            new EventsHandler.SendPacket(player);
        }
    }

    public Map<UUID, PokeInfo> getMap()
    {
        return FMLCommonHandler.instance().getEffectiveSide().isClient() ? playerMapClient : playerMap;
    }

    public void savePokemob(EntityPlayer player)
    {
        PokeInfo info = getMap().get(player.getUniqueID());
        if (info != null) info.save(player);
    }

    private void setMapping(EntityPlayer player, IPokemob pokemob)
    {
        PokeInfo info = new PokeInfo(pokemob, player);
        info.setPlayer(player);
        copyTransform((EntityLivingBase) info.pokemob, player);
        getMap().put(player.getUniqueID(), info);
        player.getEntityData().setBoolean("isPokemob", true);
        NBTTagCompound poketag = new NBTTagCompound();
        poketag.setInteger("pokenum", pokemob.getPokedexNb());
        ((Entity) pokemob).writeToNBT(poketag);
        player.getEntityData().setTag("Pokemob", poketag);
    }

    private void removeMapping(EntityPlayer player)
    {
        PokeInfo info = getMap().remove(player.getUniqueID());
        if (info != null)
        {
            info.resetPlayer(player);
            System.out.println("Removed");
        }
        player.getEntityData().setBoolean("isPokemob", false);
        player.getEntityData().removeTag("Pokemob");
    }

    public IPokemob getPokemob(EntityPlayer player)
    {
        if (getMap().containsKey(player.getUniqueID()))
        {
            PokeInfo info = getMap().get(player.getUniqueID());
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
        else
        {
            removeMapping(player);
        }
        return null;
    }

    public void updateInfo(EntityPlayer player)
    {
        if (getPokemob(player) != null)
        {
            getMap().get(player.getUniqueID()).onUpdate(player);
        }
    }

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void postInit()
    {

    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == POKEMOBGUI && getPokemob(player) != null) { return new ContainerPokemob(player); }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }
}

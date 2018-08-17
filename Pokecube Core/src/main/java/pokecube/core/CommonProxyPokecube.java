/**
 *
 */
package pokecube.core;

import java.util.UUID;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.ContainerTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTMMachine;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.CommonProxy;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.impl.PokemobGenes;
import pokecube.core.network.packets.PacketParticle;
import thut.api.maths.Vector3;

/** @author Manchou */
public class CommonProxyPokecube extends CommonProxy implements IGuiHandler
{
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        // unused server side. -- see ClientProxyPokecube for implementation
        return null;
    }

    public String getFolderName()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
    }

    public IThreadListener getMainThreadListener()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public ISnooperInfo getMinecraftInstance()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null && getWorld() != null)
        {
            try
            {
                UUID id = UUID.fromString(playerName);
                EntityPlayer ret = getWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(id);
                return ret != null ? ret : getWorld().getPlayerEntityByUUID(UUID.fromString(playerName));
            }
            catch (Exception e)
            {

            }
            return getWorld().getPlayerEntityByName(playerName);
        }
        return null;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (id == Config.GUIPOKECENTER_ID) { return new ContainerHealTable(player.inventory,
                Vector3.getNewVector().set(x + .5, y + .5, z + .5)); }
        if (id == Config.GUIPOKEMOB_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new ContainerPokemob(player.inventory, e.getPokemobInventory(), e);
        }
        if (id == Config.GUIPOKEMOBAI_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new ContainerPokemob(player.inventory, e.getPokemobInventory(), e, false);
        }
        if (id == Config.GUIPOKEMOBSTORE_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new ContainerPokemob(player.inventory, e.getPokemobInventory(), e, false);
        }
        if (id == Config.GUIPOKEMOBROUTE_ID)
        {
            IPokemob e = CapabilityPokemob
                    .getPokemobFor(PokecubeMod.core.getEntityProvider().getEntity(world, x, true));
            return new ContainerPokemob(player.inventory, e.getPokemobInventory(), e, false);
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (id == Config.GUITMTABLE_ID)
        {
            TileEntity tile_entity = world.getTileEntity(pos);
            if (tile_entity instanceof TileEntityTMMachine) { return new ContainerTMCreator(
                    (TileEntityTMMachine) tile_entity, player.inventory); }
        }
        if (id == Config.GUITRADINGTABLE_ID)
        {
            TileEntity tile_entity = world.getTileEntity(pos);
            if (tile_entity instanceof TileEntityTradingTable) { return new ContainerTradingTable(
                    (TileEntityTradingTable) tile_entity, player.inventory); }
        }
        if (id == Config.GUIPC_ID)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityPC)
            {
                ContainerPC cont = new ContainerPC(player.inventory, (TileEntityPC) te);

                return cont;
            }
        }
        return null;

    }

    public World getWorld()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance().worlds.length >= 1)
            return FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
        return null;
    }

    public boolean isOnClientSide()
    {
        return false;
    }

    public void initItemModels()
    {

    }

    public void initBlockModels()
    {

    }

    public void preInit(FMLPreInitializationEvent evt)
    {

    }

    public void registerKeyBindings()
    {
    }

    public void registerClass(Class<? extends EntityLiving> clazz, PokedexEntry entry)
    {
        // Register this pokemob has having genes.
        PokemobGenes.registerClass(clazz, entry);
    }

    @Override
    public void registerPokemobModel(String name, ModelBase model, Object mod)
    {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerPokemobRenderer(String name, IRenderFactory renderer, Object mod)
    {
    }

    /** Client side only register stuff... */
    public void registerRenderInformation()
    {
        // unused server side. -- see ClientProxyPokecube for implementation
    }

    public void spawnParticle(World world, String par1Str, Vector3 location, Vector3 velocity, int... args)
    {
        PacketParticle.sendMessage(world, location, velocity, par1Str, args);
    }

    public void handshake(boolean revert)
    {

    }

}

/**
 *
 */
package pokecube.core;

import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.blocks.healtable.TileHealTable;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.ContainerTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.CommonProxy;
import pokecube.core.interfaces.IPokemob;

/** @author Manchou */
public class CommonProxyPokecube extends CommonProxy implements IGuiHandler
{
    /** Client side only register stuff... */
    public void registerRenderInformation()
    {
        // unused server side. -- see ClientProxyPokecube for implementation
    }

    public void registerKeyBindings()
    {
    }

    public void preInit(FMLPreInitializationEvent evt)
    {

    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (id == Mod_Pokecube_Helper.GUIPOKECENTER_ID)
        {
            TileEntity tile_entity = world.getTileEntity(new BlockPos(x, y, z));

            if (tile_entity instanceof TileHealTable) { return new ContainerHealTable((TileHealTable) tile_entity,
                    player.inventory); }
        }
        if (id == Mod_Pokecube_Helper.GUIPOKEMOB_ID)
        {
            IPokemob e = (IPokemob) world.getEntityByID(x);
            return new ContainerPokemob(player.inventory, e.getPokemobInventory(), e);
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (id == Mod_Pokecube_Helper.GUITRADINGTABLE_ID)
        {
            TileEntity tile_entity = world.getTileEntity(pos);
            IBlockState state = world.getBlockState(pos);
            if (tile_entity instanceof TileEntityTradingTable)
            {
                boolean tmc = (Boolean) state.getValue(BlockTradingTable.TMC);

                if (!tmc) return new ContainerTradingTable((TileEntityTradingTable) tile_entity, player.inventory);
                else return new ContainerTMCreator((TileEntityTradingTable) tile_entity, player.inventory);
            }
        }
        if (id == Mod_Pokecube_Helper.GUIPC_ID)
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

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        // unused server side. -- see ClientProxyPokecube for implementation
        return null;
    }

    public boolean isOnClientSide()
    {
        return false;
    }

    public String getFolderName()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
    }

    public IPlayerUsage getMinecraftInstance()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null)
        {
            try
            {
                UUID.fromString(playerName);
                return getWorld().getPlayerEntityByUUID(UUID.fromString(playerName));
            }
            catch (Exception e)
            {

            }
            return getWorld().getPlayerEntityByName(playerName);
        }
        else
        {
            return null;
        }
    }

    public World getWorld()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance().worldServers.length > 1)
            return FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
        return null;
    }

    @Deprecated
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10,
            double par12)
    {
    }

    @Override
    public void registerPokemobModel(int nb, ModelBase model, Object mod)
    {
    }

    @Override
    public void registerPokemobModel(String name, ModelBase model, Object mod)
    {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerPokemobRenderer(int nb, Render renderer, Object mod)
    {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerPokemobRenderer(String name, Render renderer, Object mod)
    {
    }

}

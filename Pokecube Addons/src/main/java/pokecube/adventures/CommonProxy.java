package pokecube.adventures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.items.bags.ContainerBag;

public class CommonProxy implements IGuiHandler
{

    public void initClient()
    {
    }

    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null)
        {
            return getWorld().getPlayerEntityByName(playerName);
        }
        else
        {
            return null;
        }
    }

    public EntityPlayer getPlayer()
    {
        return null;
    }

    public boolean isOnClientSide()
    {
        return false;
    }

    public World getWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
    }

    public World getWorld(int dim)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim);
    }

    public void registerEntities()
    {

    }

    @Override
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (guiID == PokecubeAdv.GUIBAG_ID)
        {
            ContainerBag cont = new ContainerBag(player.inventory);
            return cont;
        }
        if (guiID == PokecubeAdv.GUICLONER_ID)
        {
            BlockPos pos = new BlockPos(x,y,z);
            TileEntityCloner tile = (TileEntityCloner) world.getTileEntity(pos);
            ContainerCloner cont = new ContainerCloner(player.inventory, tile);
            return cont;
        }
        if (guiID == PokecubeAdv.GUIAFA_ID)
        {
            BlockPos pos = new BlockPos(x,y,z);
            TileEntityAFA tile = (TileEntityAFA) world.getTileEntity(pos);
            ContainerAFA cont = new ContainerAFA(tile, player.inventory);
            return cont;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public void preinit()
    {

    }
}
package pokecube.adventures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.ContainerDaycare;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.container.ContainerCloner;
import pokecube.adventures.blocks.cloner.container.ContainerGeneExtractor;
import pokecube.adventures.blocks.cloner.container.ContainerSplicer;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.adventures.items.bags.ContainerBag;

public class CommonProxy implements IGuiHandler
{

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public EntityPlayer getPlayer()
    {
        return null;
    }

    public EntityPlayer getPlayer(String playerName)
    {
        if (playerName != null) { return getWorld().getPlayerEntityByName(playerName); }
        return null;
    }

    @Override
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (guiID == PokecubeAdv.GUIBAG_ID)
        {
            ContainerBag cont = new ContainerBag(player.inventory);
            cont.gotoInventoryPage(x);
            return cont;
        }
        if (guiID == PokecubeAdv.GUICLONER_ID)
        {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntityCloner tile = (TileEntityCloner) world.getTileEntity(pos);
            ContainerCloner cont = new ContainerCloner(player.inventory, tile);
            return cont;
        }
        if (guiID == PokecubeAdv.GUISPLICER_ID)
        {
            BlockPos pos = new BlockPos(x, y, z);
            IInventory tile = (IInventory) world.getTileEntity(pos);
            ContainerSplicer cont = new ContainerSplicer(player.inventory, tile);
            return cont;
        }
        if (guiID == PokecubeAdv.GUIEXTRACTOR_ID)
        {
            BlockPos pos = new BlockPos(x, y, z);
            IInventory tile = (IInventory) world.getTileEntity(pos);
            ContainerGeneExtractor cont = new ContainerGeneExtractor(player.inventory, tile);
            return cont;
        }
        if (guiID == PokecubeAdv.GUIAFA_ID)
        {
            BlockPos pos = new BlockPos(x, y, z);
            Container cont = null;
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityAFA)
            {
                TileEntityAFA tile = (TileEntityAFA) tileEntity;
                cont = new ContainerAFA(tile, player.inventory);
            }
            else if (tileEntity instanceof TileEntityDaycare)
            {
                TileEntityDaycare tile = (TileEntityDaycare) tileEntity;
                cont = new ContainerDaycare(tile, player.inventory);
            }
            return cont;
        }
        return null;
    }

    public World getWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
    }

    public World getWorld(int dim)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
    }

    public void initClient() {}

    public boolean isOnClientSide()
    {
        return false;
    }
    
    public void initItemModels() {}
    
    public void initBlockModels() {}

    public void preinit() {}

    public void postinit() {}

    public void registerEntities()  {}
}
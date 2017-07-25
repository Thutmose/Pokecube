package pokecube.pokeplayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.EntityTools;
import pokecube.pokeplayer.inventory.ContainerPokemob;

public class Proxy implements IGuiHandler
{
    public static final int POKEMOBGUI = 0;

    public void setPokemob(EntityPlayer player, IPokemob pokemob)
    {
        setMapping(player, pokemob);
    }

    public void savePokemob(EntityPlayer player)
    {
        PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        if (info != null) info.save(player);
    }

    private void setMapping(EntityPlayer player, IPokemob pokemob)
    {
        PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        info.set(pokemob, player);
        if (pokemob != null)
        {
            info.setPlayer(player);
            EntityTools.copyEntityTransforms(info.getPokemob(player.getEntityWorld()).getEntity(), player);
            info.save(player);
        }
    }

    public IPokemob getPokemob(EntityPlayer player)
    {
        if (player == null || player.getUniqueID() == null) return null;
        PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        return info.getPokemob(player.getEntityWorld());
    }

    public void updateInfo(EntityPlayer player)
    {
        PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        info.onUpdate(player);
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

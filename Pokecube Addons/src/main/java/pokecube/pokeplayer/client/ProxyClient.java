package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.Proxy;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;
import pokecube.pokeplayer.client.gui.GuiPokemob;

public class ProxyClient extends Proxy
{
    @Override
    public IPokemob getPokemob(EntityPlayer player)
    {
        IPokemob ret = super.getPokemob(player);
        if (ret != null && player.getEntityWorld().isRemote)
        {
            PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            info.setPlayer(player);
        }
        return ret;
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public void postInit()
    {
        super.postInit();
        MinecraftForge.EVENT_BUS.register(new GuiAsPokemob());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        IPokemob pokemob;
        if (event.side == Side.SERVER || event.player != PokecubeCore.proxy.getPlayer((String) null)
                || (pokemob = getPokemob(event.player)) == null)
            return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiPokedex)
        {
            ((GuiPokedex) Minecraft.getMinecraft().currentScreen).pokemob = pokemob;
            GuiPokedex.pokedexEntry = pokemob.getPokedexEntry();
        }
    }

    @SubscribeEvent
    public void pRender(RenderPlayerEvent.Pre event)
    {
        IPokemob pokemob = getPokemob(event.getEntityPlayer());
        if (pokemob == null) return;
        event.setCanceled(true);
        boolean shadow = Minecraft.getMinecraft().getRenderManager().isRenderShadow();
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(pokemob.getEntity(), event.getX(), event.getY(),
                event.getZ(), event.getEntityPlayer().rotationYaw, event.getPartialRenderTick(), false);
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(shadow);
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event)
    {
        IPokemob pokemob = getPokemob(Minecraft.getMinecraft().thePlayer);
        if (pokemob == null) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void mouseClickEvent(MouseEvent event)
    {
        IPokemob pokemob = getPokemob(Minecraft.getMinecraft().thePlayer);
        if (pokemob != null && event.getButton() == 0 && event.isButtonstate())
        {
            if (GuiScreen.isAltKeyDown())
            {
                GuiAsPokemob.useMove = true;
                GuiDisplayPokecubeInfo.instance().pokemobAttack();
                event.setCanceled(true);
            }
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == POKEMOBGUI && getPokemob(player) != null) { return new GuiPokemob(player); }
        return null;
    }
}
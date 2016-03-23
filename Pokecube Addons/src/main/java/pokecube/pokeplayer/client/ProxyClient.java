package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.Proxy;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;
import pokecube.pokeplayer.client.gui.GuiPokemob;

public class ProxyClient extends Proxy
{
    static long time = Long.MIN_VALUE;

    @Override
    public IPokemob getPokemob(EntityPlayer player)
    {
        IPokemob ret = super.getPokemob(player);
        if (ret != null)
        {
            PokeInfo info = playerMap.get(player.getUniqueID());
            info.setPlayer(player);
        }
        return ret;
    }

    @Override
    public void init()
    {
        super.init();
    }

    public void postInit()
    {
        super.postInit();
        MinecraftForge.EVENT_BUS.register(new GuiAsPokemob());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        IPokemob pokemob;
        if (event.side == Side.SERVER || (pokemob = getPokemob(event.player)) == null) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiPokedex)
        {
            ((GuiPokedex) Minecraft.getMinecraft().currentScreen).pokemob = pokemob;
            GuiPokedex.pokedexEntry = pokemob.getPokedexEntry();
        }
    }

    @SubscribeEvent
    public void pRender(RenderPlayerEvent.Pre event)
    {
        IPokemob pokemob = getPokemob(event.entityPlayer);
        if (pokemob == null) return;
        event.setCanceled(true);
        boolean shadow = Minecraft.getMinecraft().getRenderManager().isRenderShadow();
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw((EntityLivingBase) pokemob, event.x, event.y,
                event.z, event.entityPlayer.rotationYaw, event.partialRenderTick);
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
        if (pokemob != null && time < System.nanoTime() - 100000000 && event.button == 0 && event.buttonstate)
        {
            if (Minecraft.getMinecraft().thePlayer.getHeldItem() == null)
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
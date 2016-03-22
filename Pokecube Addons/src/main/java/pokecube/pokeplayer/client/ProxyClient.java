package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.Proxy;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;

public class ProxyClient extends Proxy
{
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
}

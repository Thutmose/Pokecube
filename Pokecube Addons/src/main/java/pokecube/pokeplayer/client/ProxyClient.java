package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.Proxy;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;

public class ProxyClient extends Proxy
{
    @Override
    public IPokemob getPokemob(EntityPlayer player)
    {
        return super.getPokemob(player);
    }

    @Override
    public void init()
    {
        super.init();
        MinecraftForge.EVENT_BUS.register(new GuiAsPokemob());
    }

    @SubscribeEvent
    public void pRender(RenderPlayerEvent.Pre event)
    {
        IPokemob pokemob = getPokemob(event.entityPlayer);
        if (pokemob == null) return;
        event.setCanceled(true);
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw((EntityLivingBase) pokemob, event.x, event.y,
                event.z, event.entityPlayer.rotationYaw, event.partialRenderTick);
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event)
    {
        IPokemob pokemob = getPokemob(Minecraft.getMinecraft().thePlayer);
        if (pokemob == null) return;
        event.setCanceled(true);
    }
}

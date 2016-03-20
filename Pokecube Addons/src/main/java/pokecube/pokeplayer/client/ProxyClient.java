package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.Proxy;

public class ProxyClient extends Proxy
{
    @SubscribeEvent
    public void pRender(RenderPlayerEvent.Pre event)
    {
        IPokemob pokemob = getPokemob(event.entityPlayer);
        if (pokemob == null) return;
        event.setCanceled(true);
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw((EntityLivingBase) pokemob, event.x, event.y,
                event.z, event.entityPlayer.rotationYaw, event.partialRenderTick);
    }

    public void init()
    {
        super.init();
    }

    @Override
    public IPokemob getPokemob(EntityPlayer player)
    {
        return null;
    }
}

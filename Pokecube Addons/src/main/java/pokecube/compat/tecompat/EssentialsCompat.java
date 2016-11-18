package pokecube.compat.tecompat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.interfaces.IPokecube;
import thut.essentials.events.DenyItemUseEvent;
import thut.essentials.events.DenyItemUseEvent.UseType;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class EssentialsCompat
{

    @Optional.Method(modid = "thutessentials")
    @CompatClass(phase = Phase.POST)
    public static void thutEssentialsCompat()
    {
        new pokecube.compat.tecompat.EssentialsCompat();
    }

    public EssentialsCompat()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemUse(DenyItemUseEvent evt)
    {
        if (evt.getType() == UseType.RIGHTCLICKITEM || evt.getType() == UseType.RIGHTCLICKBLOCK)
        {
            if (evt.getItem() == null || evt.getItem().getItem() instanceof IPokecube)
            {
                evt.setCanceled(true);
            }
        }
    }
}

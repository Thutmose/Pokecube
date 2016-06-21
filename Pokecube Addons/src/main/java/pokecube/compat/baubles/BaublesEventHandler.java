package pokecube.compat.baubles;

import java.util.Set;

import com.google.common.collect.Sets;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.client.render.item.BagRenderer;
import pokecube.core.events.handlers.EventsHandlerClient.RingChecker;
import pokecube.core.items.megastuff.ItemMegaring;

public class BaublesEventHandler
{
    private Set<RenderPlayer> addedBaubles = Sets.newHashSet();

    public BaublesEventHandler()
    {
        pokecube.core.events.handlers.EventsHandlerClient.checker = new RingChecker()
        {
            @Override
            public boolean hasRing(EntityPlayer player)
            {
                InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
                for (int i = 0; i < inv.getSizeInventory(); i++)
                {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (stack != null)
                    {
                        Item item = stack.getItem();
                        if (item instanceof ItemMegaring) { return true; }
                    }
                }
                return false;
            }
        };
    }

    @SubscribeEvent
    public void addBaubleRender(RenderPlayerEvent.Post event)
    {
        if (addedBaubles.contains(event.getRenderer())) { return; }
        event.getRenderer().addLayer(new RingRenderer(event.getRenderer()));
        event.getRenderer().addLayer(new BagRenderer(event.getRenderer()));
        addedBaubles.add(event.getRenderer());
    }
}

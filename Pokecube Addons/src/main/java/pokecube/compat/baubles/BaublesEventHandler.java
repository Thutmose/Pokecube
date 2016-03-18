package pokecube.compat.baubles;

import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Sets;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.client.render.item.BagRenderer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.core.client.render.entity.RingRenderer;
import pokecube.core.events.handlers.EventsHandlerClient.RingChecker;
import pokecube.core.items.megastuff.ItemMegaring;
import thut.api.maths.Vector3;

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
        if (addedBaubles.contains(event.renderer)) { return; }
        event.renderer.addLayer(new RingRenderer(event.renderer));
        event.renderer.addLayer(new BagRenderer(event.renderer));
        addedBaubles.add(event.renderer);
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
        boolean bag = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                Item item = stack.getItem();
                if (item instanceof ItemBag)
                {
                    bag = true;
                    break;
                }
            }
        }
        if (bag && Keyboard.getEventKey() == ClientProxy.bag.getKeyCode())
        {
            PacketPokeAdv.sendBagOpenPacket(false, Vector3.empty);
        }
    }
}

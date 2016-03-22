package pokecube.pokeplayer;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokedex;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageClient;

public class EventsHandler
{
    private static Proxy proxy;

    public EventsHandler(Proxy proxy)
    {
        EventsHandler.proxy = proxy;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent event)
    {
        IPokemob pokemob = proxy.getPokemob(event.entityPlayer);
        if (pokemob == null || !(pokemob instanceof EntityPokemob)) return;
        EntityPlayer player = event.entityPlayer;
        if (event.entityPlayer.getHeldItem() != null && event.entityPlayer.isSneaking()
                && event.entityPlayer.getHeldItem().getItem() instanceof ItemPokedex)
        {
            pokemob.getPokemobInventory().func_110134_a((IInvBasic) pokemob);
            if (!event.world.isRemote)
            {
                event.setCanceled(true);
                event.entityPlayer.openGui(PokePlayer.INSTANCE, Proxy.POKEMOBGUI, event.entityPlayer.worldObj, 0, 0, 0);
            }
        }
        else if (event.action == Action.RIGHT_CLICK_AIR && event.entityPlayer.getHeldItem() != null)
        {
            ((Entity) pokemob).interactFirst(player);
            proxy.playerMap.get(event.entityPlayer.getUniqueID()).save(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            proxy.updateInfo(event.player);
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        if (!evt.player.worldObj.isRemote)
        {
        }
    }

    @SubscribeEvent
    public void PlayerLogout(PlayerLoggedOutEvent evt)
    {
        PokeInfo info = proxy.playerMap.remove(evt.player.getUniqueID());
        if (info != null) info.save(evt.player);
    }

    @SubscribeEvent
    public void PlayerJoinWorld(EntityJoinWorldEvent evt)
    {
        if (!(evt.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) evt.entity;
        if (!player.worldObj.isRemote)
        {
            new SendPacket(player);
            new SendExsistingPacket(player);
        }
    }

    public static class SendPacket
    {
        final EntityPlayer player;

        public SendPacket(EntityPlayer player)
        {
            this.player = player;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event)
        {
            if (event.player == player)
            {
                proxy.getPokemob(player);
                boolean pokemob = player.getEntityData().getBoolean("isPokemob");
                PokeInfo info = proxy.playerMap.get(player.getUniqueID());
                if (info == null) pokemob = false;
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                MessageClient message = new MessageClient(buffer);
                buffer.writeByte(MessageClient.SETPOKE);
                buffer.writeInt(player.getEntityId());
                buffer.writeBoolean(pokemob);
                if (pokemob)
                {
                    buffer.writeFloat(info.originalHeight);
                    buffer.writeFloat(info.originalWidth);
                    buffer.writeNBTTagCompoundToBuffer(player.getEntityData().getCompoundTag("Pokemob"));
                }
                PokecubeMod.packetPipeline.sendToDimension(message, player.dimension);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static class SendExsistingPacket
    {
        final EntityPlayer player;

        public SendExsistingPacket(EntityPlayer player)
        {
            this.player = player;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event)
        {
            if (event.player == player)
            {
                for (EntityPlayer player1 : player.worldObj.playerEntities)
                {
                    proxy.getPokemob(player1);
                    boolean pokemob = player1.getEntityData().getBoolean("isPokemob");
                    PokeInfo info = proxy.playerMap.get(player1.getUniqueID());
                    if (info == null) pokemob = false;
                    PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                    MessageClient message = new MessageClient(buffer);
                    buffer.writeByte(MessageClient.SETPOKE);
                    buffer.writeInt(player1.getEntityId());
                    buffer.writeBoolean(pokemob);
                    if (pokemob)
                    {
                        buffer.writeFloat(info.originalHeight);
                        buffer.writeFloat(info.originalWidth);
                        buffer.writeNBTTagCompoundToBuffer(player1.getEntityData().getCompoundTag("Pokemob"));
                    }
                    PokecubeMod.packetPipeline.sendTo(message, (EntityPlayerMP) player);
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        }
    }
}

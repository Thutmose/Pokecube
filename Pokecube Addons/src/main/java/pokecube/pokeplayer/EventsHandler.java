package pokecube.pokeplayer;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import pokecube.core.events.AttackEvent;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokedex;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageClient;
import thut.api.entity.IHungrymob;

public class EventsHandler
{
    private static Proxy proxy;

    public EventsHandler(Proxy proxy)
    {
        EventsHandler.proxy = proxy;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.RightClickItem event)
    {
        IPokemob pokemob = proxy.getPokemob(event.getEntityPlayer());
        if (pokemob == null) return;
        if (event.getItemStack() != null && event.getEntityPlayer().isSneaking()
                && event.getItemStack().getItem() instanceof ItemPokedex)
        {
            pokemob.getPokemobInventory().addInventoryChangeListener((IInventoryChangedListener) pokemob);
            if (!event.getWorld().isRemote)
            {
                event.setCanceled(true);
                event.getEntityPlayer().openGui(PokePlayer.INSTANCE, Proxy.POKEMOBGUI, event.getEntityPlayer().worldObj,
                        0, 0, 0);
            }
        }
        else if (event.getItemStack() != null)
        {
            ((Entity) pokemob).processInitialInteract(event.getEntityPlayer(), event.getItemStack(), event.getHand());
            proxy.getMap().get(event.getEntityPlayer().getUniqueID()).save(event.getEntityPlayer());
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
    public void pokemobAttack(AttackEvent evt)
    {
        if (evt.moveInfo.attacked instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.moveInfo.attacked;
            IPokemob pokemob = proxy.getPokemob(player);
            if (pokemob != null)
            {
                evt.moveInfo.attacked = (Entity) pokemob;
            }
        }
    }

    @SubscribeEvent
    public void pokemobMoveMessage(MoveMessageEvent evt)
    {
        Entity user = (Entity) evt.sender;
        if (user.getEntityData().getBoolean("isPlayer") && !user.worldObj.isRemote)
        {
            Entity e = user.worldObj.getEntityByID(user.getEntityId());
            if (e instanceof EntityPlayer)
            {
                EntityPlayer owner = (EntityPlayer) e;
                PacketPokemobMessage.sendMessage(owner, owner.getEntityId(), evt.message);
            }
        }
    }

    @SubscribeEvent
    public void PlayerDeath(LivingDeathEvent evt)
    {
        if (evt.getEntityLiving() instanceof EntityPlayer)
        {
            IPokemob pokemob = proxy.getPokemob((EntityPlayer) evt.getEntityLiving());
            if (pokemob != null)
            {
                ((EntityLivingBase) pokemob).setHealth(10);
                ((IHungrymob) pokemob).setHungerTime(0);
            }
        }
    }

    @SubscribeEvent
    public void PlayerLogout(PlayerLoggedOutEvent evt)
    {
        PokeInfo info = proxy.getMap().remove(evt.player.getUniqueID());
        if (info != null) info.save(evt.player);
    }

    @SubscribeEvent
    public void PlayerJoinWorld(EntityJoinWorldEvent evt)
    {
        if (evt.getEntity() instanceof IPokemob)
        {
            if (evt.getEntity().getEntityData().getBoolean("isPlayer"))
            {
                UUID uuid = UUID.fromString(evt.getEntity().getEntityData().getString("playerID"));
                EntityPlayer player = evt.getWorld().getPlayerEntityByUUID(uuid);
                IPokemob evo = (IPokemob) evt.getEntity();
                proxy.setPokemob(player, evo);
                evt.setCanceled(true);
            }
        }

        if (!(evt.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) evt.getEntity();
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
                boolean pokemob;
                PokeInfo info = proxy.getMap().get(player.getUniqueID());
                if (info == null)
                {
                    proxy.getPokemob(player);
                    info = proxy.getMap().get(player.getUniqueID());
                    pokemob = info != null;
                    pokemob = pokemob && player.getEntityData().getBoolean("isPokemob");
                }
                else
                {
                    pokemob = player.getEntityData().getBoolean("isPokemob");
                }
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
                    boolean pokemob;
                    PokeInfo info = proxy.getMap().get(player.getUniqueID());
                    if (info == null)
                    {
                        proxy.getPokemob(player);
                        info = proxy.getMap().get(player.getUniqueID());
                        pokemob = info != null;
                        pokemob = pokemob && player.getEntityData().getBoolean("isPokemob");
                    }
                    else
                    {
                        pokemob = player.getEntityData().getBoolean("isPokemob");
                    }
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

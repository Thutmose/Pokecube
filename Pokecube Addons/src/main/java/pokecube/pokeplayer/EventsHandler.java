package pokecube.pokeplayer;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import pokecube.core.PokecubeCore;
import pokecube.core.events.AttackEvent;
import pokecube.core.events.EvolveEvent;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.pokeplayer.network.PacketTransform;

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
        if (event.getEntityPlayer().isSneaking())
        {
            EntityInteractSpecific evt = new EntityInteractSpecific(event.getEntityPlayer(), event.getHand(),
                    event.getItemStack(), pokemob.getEntity(), new Vec3d(0, 0, 0));
            PokecubeCore.instance.events.interactEvent(evt);
            PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(event.getEntityPlayer())
                    .getData(PokeInfo.class);
            info.save(event.getEntityPlayer());
            if (evt.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            if (event.player.getHealth() <= 0) { return; }
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
                evt.moveInfo.attacked = pokemob.getEntity();
            }
        }
    }

    @SubscribeEvent
    public void pokemobMoveMessage(MoveMessageEvent evt)
    {
    }

    @SubscribeEvent
    public void doRespawn(PlayerRespawnEvent event)
    {
        if (event.player != null && !event.player.getEntityWorld().isRemote)
        {
            IPokemob pokemob = proxy.getPokemob(event.player);
            if (pokemob != null)
            {
                ItemStack stack = PokecubeManager.pokemobToItem(pokemob);
                PokecubeManager.heal(stack);
                pokemob = PokecubeManager.itemToPokemob(stack, event.player.getEntityWorld());
                proxy.setPokemob(event.player, pokemob);
                sendUpdate(event.player);
            }
        }
    }

    @SubscribeEvent
    public void recall(RecallEvent.Pre evt)
    {
        if (evt.recalled.getEntity().getEntityData().getBoolean("isPlayer")) evt.setCanceled(true);
    }

    @SubscribeEvent
    public void PlayerDeath(LivingDeathEvent evt)
    {
        if (evt.getEntityLiving().getEntityWorld().isRemote) return;
        if (!(evt.getEntityLiving() instanceof EntityPlayer))
        {
            if (evt.getEntityLiving().getEntityData().getBoolean("isPlayer")
                    && CapabilityPokemob.getPokemobFor(evt.getEntityLiving()) != null)
            {
                Entity real = evt.getEntityLiving().getEntityWorld().getEntityByID(evt.getEntity().getEntityId());
                if (real != evt.getEntity() && real instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP) real;
                    player.attackEntityFrom(evt.getSource(), Float.MAX_VALUE);
                }
            }
            return;
        }
        EntityPlayer player = (EntityPlayer) evt.getEntityLiving();
        if (player != null)
        {
            IPokemob pokemob = proxy.getPokemob(player);
            if (pokemob != null)
            {
                ItemStack stack = PokecubeManager.pokemobToItem(pokemob);
                PokecubeManager.heal(stack);
                pokemob = PokecubeManager.itemToPokemob(stack, player.getEntityWorld());
                proxy.setPokemob(player, pokemob);
                sendUpdate(player);
            }
        }
    }

    @SubscribeEvent
    public void evolve(EvolveEvent.Post evt)
    {
        Entity entity = evt.mob.getEntity();
        if (entity.getEntityData().getBoolean("isPlayer"))
        {
            UUID uuid = UUID.fromString(entity.getEntityData().getString("playerID"));
            EntityPlayer player = entity.getEntityWorld().getPlayerEntityByUUID(uuid);
            IPokemob evo = evt.mob;
            proxy.setPokemob(player, evo);
            evt.setCanceled(true);
            if (!player.getEntityWorld().isRemote)
            {
                sendUpdate(player);
            }
            return;
        }
    }

    @SubscribeEvent
    public void PlayerJoinWorld(EntityJoinWorldEvent evt)
    {
        if (evt.getEntity().getEntityData().getBoolean("isPlayer"))
        {
            IPokemob evo = CapabilityPokemob.getPokemobFor(evt.getEntity());
            if (evo != null)
            {
                UUID uuid = UUID.fromString(evt.getEntity().getEntityData().getString("playerID"));
                EntityPlayer player = evt.getWorld().getPlayerEntityByUUID(uuid);
                proxy.setPokemob(player, evo);
                evt.setCanceled(true);
                if (!player.getEntityWorld().isRemote)
                {
                    sendUpdate(player);
                }
                return;
            }
        }
        if (!(evt.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) evt.getEntity();
        if (!player.getEntityWorld().isRemote)
        {
            sendUpdate(player);
            sendExisting(player);
        }
    }

    public static void sendUpdate(EntityPlayer player)
    {
        new SendPacket(player);
    }

    public static void sendExisting(EntityPlayer player)
    {
        new SendExsistingPacket(player);
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
                PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                PacketTransform message = new PacketTransform();
                info.writeToNBT(message.data);
                message.id = player.getEntityId();
                PokecubeMod.packetPipeline.sendToAll(message);
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
                    if (player1 == player) continue;
                    PokeInfo info = PokecubePlayerDataHandler.getInstance().getPlayerData(player1)
                            .getData(PokeInfo.class);
                    PacketTransform message = new PacketTransform();
                    info.writeToNBT(message.data);
                    message.id = player1.getEntityId();
                    PokecubeMod.packetPipeline.sendTo(message, (EntityPlayerMP) player);
                }
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}

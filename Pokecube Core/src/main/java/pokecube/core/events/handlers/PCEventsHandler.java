package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.CaptureEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketPC;
import pokecube.core.utils.PCSaveHandler;

public class PCEventsHandler
{
    public static final UUID THUTMOSE = UUID.fromString("f1dacdfd-42d6-4af0-8234-b2f180ecd6a8");

    /** Gets a list of all pokemobs out of their cube belonging to the player in
     * the player's current world.
     * 
     * @param player
     * @return */
    public static List<IPokemob> getOutMobs(EntityLivingBase player)
    {
        List<Entity> pokemobs = new ArrayList<Entity>(player.getEntityWorld().loadedEntityList);
        List<IPokemob> ret = new ArrayList<IPokemob>();
        for (Entity o : pokemobs)
        {
            // Check to see if the mob has recenlty unloaded, or isn't added to
            // chunk for some reason. This is to hopefully prevent dupes when
            // the player has died far from the loaded area.
            if (player.getEntityWorld().unloadedEntityList.contains(o)) continue;
            if (!o.addedToChunk) continue;

            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                if (mob.getPokemonOwner() != null && mob.getPokemonOwner() == player)
                {
                    ret.add(mob);
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube cube = (EntityPokecube) o;
                if (cube.getItem() != null)
                {
                    IPokemob poke = PokecubeManager.itemToPokemob(cube.getItem(), cube.getEntityWorld());
                    if (poke != null && poke.getPokemonOwner() != null && poke.getPokemonOwner() == player)
                    {
                        ret.add(poke);
                    }
                }
            }
        }

        return ret;
    }

    /** Recalls all pokemobs belonging to the player in the player's current
     * world.
     * 
     * @param player */
    public static void recallAllPokemobs(Entity player)
    {
        List<Entity> pokemobs = new ArrayList<Entity>(player.getEntityWorld().loadedEntityList);
        for (Entity o : pokemobs)
        {
            // Check to see if the mob has recenlty unloaded, or isn't added to
            // chunk for some reason. This is to hopefully prevent dupes when
            // the player has died far from the loaded area.
            if (player.getEntityWorld().unloadedEntityList.contains(o)) continue;
            if (!o.addedToChunk) continue;

            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                if (mob.getPokemonOwner() != null && mob.getPokemonOwner() == player)
                {
                    mob.returnToPokecube();
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube cube = (EntityPokecube) o;
                if (cube.getItem() != null)
                {
                    String name = PokecubeManager.getOwner(cube.getItem());
                    if (name != null && (name.equalsIgnoreCase(player.getName())
                            || name.equals(player.getCachedUniqueIdString())))
                    {
                        InventoryPC.addStackToPC(player.getUniqueID(), cube.getItem());
                        cube.setDead();
                    }
                }
            }
        }
    }

    /** Used for changing name from "Someone's PC" to "Thutmose's PC". This is
     * done as all of the PC systems are named after whoever made them. See
     * Bill's PC for an example.
     * 
     * @param evt */
    @SubscribeEvent
    public void PCLoggin(EntityJoinWorldEvent evt)
    {
        // I will deal with some pokemob related bugs here, as to not have
        // multiple event handlers for this.
        if (evt.getWorld() instanceof WorldServer && evt.getEntity() instanceof IPokemob)
        {
            UUID id = evt.getEntity().getUniqueID();
            WorldServer serer = (WorldServer) evt.getWorld();
            if (serer.getEntityFromUuid(id) != null)
            {
                evt.setCanceled(true);
                PokecubeMod.log(Level.WARNING, "Tried to load duplicate " + evt.getEntity(),
                        new IllegalArgumentException());
            }
            return;
        }

        if (!(evt.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer entityPlayer = (EntityPlayer) evt.getEntity();
        if (entityPlayer.getUniqueID().equals(THUTMOSE))
        {
            for (Object o : evt.getWorld().playerEntities)
            {
                if (o instanceof EntityPlayer)
                {
                    EntityPlayer p = (EntityPlayer) o;
                    if (InventoryPC.map.containsKey(p.getCachedUniqueIdString()))
                    {
                        InventoryPC pc = InventoryPC.getPC(p);
                        pc.seenOwner = true;
                        if (evt.getWorld().isRemote) continue;
                        PacketPC packet = new PacketPC(PacketPC.ONOPEN);
                        packet.data.setBoolean("O", pc.seenOwner);
                        packet.data.setBoolean("A", pc.autoToPC);
                        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) p);
                    }
                }
            }
        }
    }

    /** Sends the packet with the player's PC data to that player.
     * 
     * @param evt */
    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;

        if (entityPlayer.getName().toLowerCase(java.util.Locale.ENGLISH).trim().equals("thutmose"))
        {
            PCSaveHandler.getInstance().seenPCCreator = true;
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        PacketPC.sendInitialSyncMessage(evt.player);

    }

    /** This sends pokecube to PC if the player has a full inventory and tries
     * to pick up a pokecube.
     * 
     * @param evt */
    @SubscribeEvent
    public void playerPickupItem(EntityItemPickupEvent evt)
    {
        if (evt.getItem().getEntityWorld().isRemote) return;
        InventoryPlayer inv = evt.getEntityPlayer().inventory;
        int num = inv.getFirstEmptyStack();
        if (!PokecubeManager.isFilled(evt.getItem().getItem())) { return; }
        String owner = PokecubeManager.getOwner(evt.getItem().getItem());
        if (evt.getEntityPlayer().getCachedUniqueIdString().equals(owner))
        {
            if (num == -1)
            {
                InventoryPC.addPokecubeToPC(evt.getItem().getItem(), evt.getEntityPlayer().getEntityWorld());
                evt.getItem().setDead();
            }
        }
        else
        {
            InventoryPC.addPokecubeToPC(evt.getItem().getItem(), evt.getEntityPlayer().getEntityWorld());
            evt.getItem().setDead();
            evt.setCanceled(true);
        }
    }

    /** If player tosses a pokecube item, it will be send to PC instead.
     * 
     * @param evt */
    @SubscribeEvent
    public void playerTossPokecubeToPC(ItemTossEvent evt)
    {
        if (evt.getEntityItem().getEntityWorld().isRemote) return;
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            InventoryPC.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntityItem().getEntityWorld());
            evt.getEntityItem().setDead();
            evt.setCanceled(true);
        }
    }

    /** If player tosses a pokecube item, it will be send to PC instead.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void PCEvent(pokecube.core.events.PCEvent evt)
    {
        if (evt.owner == null || evt.owner.getEntityWorld().isRemote) return;
        if (PokecubeManager.isFilled(evt.toPC))
        {
            InventoryPC.addPokecubeToPC(evt.toPC, evt.owner.getEntityWorld());
            evt.setCanceled(true);
        }
    }

    /** Attempts to send the pokecube to the PC whenever the entityitem it is in
     * expires. This prevents losing pokemobs if the cube is somehow left in the
     * world.
     * 
     * @param evt */
    @SubscribeEvent
    public void sendPokemobToPCOnItemExpiration(ItemExpireEvent evt)
    {
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            if (evt.getEntityItem().getEntityWorld().isRemote) return;
            InventoryPC.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntityItem().getEntityWorld());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void sendPokemobToPCPlayerDeath(LivingDeathEvent evt)
    {
        if (evt.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.getEntity();
            EventsHandler.recallAllPokemobsExcluding(player, null);
        }
    }

    /** Tries to send pokecubes to PC when player dies.
     * 
     * @param evt */
    @SubscribeEvent
    public void sendPokemobToPCPlayerDrops(PlayerDropsEvent evt)
    {
        if (!(evt.getEntity() instanceof EntityPlayer) || !PokecubeMod.core.getConfig().pcOnDrop) return;
        if (evt.getEntity().getEntityWorld().isRemote) return;
        List<EntityItem> toRemove = Lists.newArrayList();
        for (EntityItem item : evt.getDrops())
        {
            if (item != null && item.getItem() != null && ContainerPC.isItemValid(item.getItem()))
            {
                InventoryPC.addStackToPC(evt.getEntity().getUniqueID(), item.getItem().copy());
                toRemove.add(item);
            }
        }
        evt.getDrops().removeAll(toRemove);
    }

    /** Tries to send pokecube to PC if player has no room in inventory for it.
     * Otherwise, will add pokecube to player's inventory.
     * 
     * @param evt */
    @SubscribeEvent
    public void sendPokemobToPCPlayerFull(CaptureEvent.Post evt)
    {
        Entity catcher = evt.caught.getPokemonOwner();
        if (evt.caught.isShadow()) return;
        if (catcher instanceof EntityPlayer && PokecubeManager.isFilled(evt.filledCube))
        {
            if (catcher.getEntityWorld().isRemote) return;

            EntityPlayer player = (EntityPlayer) catcher;
            if (player instanceof FakePlayer) return;

            InventoryPlayer inv = player.inventory;
            UUID id = UUID.fromString(PokecubeManager.getOwner(evt.filledCube));
            InventoryPC pc = InventoryPC.getPC(id);
            int num = inv.getFirstEmptyStack();
            if (evt.filledCube == null || pc == null)
            {
                System.err.println("Cube is null");
            }
            else if (num == -1 || pc.autoToPC || player.isDead)
            {
                evt.setCanceled(true);
                InventoryPC.addPokecubeToPC(evt.filledCube, catcher.getEntityWorld());
            }
            else
            {
                player.inventory.addItemStackToInventory(evt.filledCube);
                if (player instanceof EntityPlayerMP)
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        }
        else
        {
            evt.pokecube.entityDropItem(evt.filledCube, 0.5f);
        }
    }
}

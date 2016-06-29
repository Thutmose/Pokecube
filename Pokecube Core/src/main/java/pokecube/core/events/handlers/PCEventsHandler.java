package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.CaptureEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PCPacketHandler.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PCSaveHandler;

public class PCEventsHandler
{
    /** Gets a list of all pokemobs out of their cube belonging to the player in
     * the player's current world.
     * 
     * @param player
     * @return */
    public static List<IPokemob> getOutMobs(EntityLivingBase player)
    {
        List<?> pokemobs = new ArrayList<Object>(player.worldObj.loadedEntityList);
        List<IPokemob> ret = new ArrayList<IPokemob>();
        for (Object o : pokemobs)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob.getPokemonOwner() != null && mob.getPokemonOwner() == player)
                {
                    ret.add(mob);
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube mob = (EntityPokecube) o;
                if (mob.getEntityItem() != null)
                {
                    IPokemob poke = PokecubeManager.itemToPokemob(mob.getEntityItem(), mob.worldObj);
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
        List<Object> pokemobs = new ArrayList<Object>(player.worldObj.loadedEntityList);
        boolean sentToPC = false;
        for (Object o : pokemobs)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob.getPokemonOwner() != null && mob.getPokemonOwner() == player)
                {
                    mob.returnToPokecube();
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube mob = (EntityPokecube) o;
                if (mob.getEntityItem() != null)
                {
                    String name = PokecubeManager.getOwner(mob.getEntityItem());
                    if (name != null && (name.equalsIgnoreCase(player.getName())
                            || name.equals(player.getUniqueID().toString())))
                    {
                        InventoryPC.addStackToPC(name, mob.getEntityItem());
                        mob.setDead();
                        sentToPC = true;
                    }
                }
            }
        }
        if (sentToPC && player instanceof EntityPlayer)
        {
            String uuid = player.getUniqueID().toString();
            if (!PokecubeCore.isOnClientSide())
            {
                PCSaveHandler.getInstance().savePC(uuid);
                NBTTagCompound nbt = new NBTTagCompound();
                NBTTagList tags = InventoryPC.saveToNBT(player.getUniqueID().toString());

                nbt.setTag("pc", tags);

                MessageClient packet = new MessageClient(MessageClient.PERSONALPC, nbt);
                PokecubePacketHandler.sendToClient(packet, (EntityPlayer) player);
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
        if (!(evt.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer entityPlayer = (EntityPlayer) evt.getEntity();

        if (entityPlayer.getName().toLowerCase().trim().equals("thutmose"))
        {
            for (Object o : evt.getWorld().playerEntities)
            {
                if (o instanceof EntityPlayer)
                {
                    EntityPlayer p = (EntityPlayer) o;
                    if (InventoryPC.map.containsKey(p.getUniqueID().toString()))
                    {
                        InventoryPC.getPC(p.getUniqueID().toString()).seenOwner = true;

                        if (evt.getWorld().isRemote) continue;

                        NBTTagCompound nbt = new NBTTagCompound();
                        NBTTagList tags = InventoryPC.saveToNBT(p.getUniqueID().toString());

                        nbt.setTag("pc", tags);
                        nbt.setBoolean("pcCreator", true);
                        MessageClient packet = new MessageClient(MessageClient.PERSONALPC, nbt);
                        PokecubePacketHandler.sendToClient(packet, p);
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

        if (entityPlayer.getName().toLowerCase().trim().equals("thutmose"))
        {
            PCSaveHandler.getInstance().seenPCCreator = true;
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList tags = InventoryPC.saveToNBT(entityPlayer.getUniqueID().toString());

        nbt.setTag("pc", tags);
        nbt.setBoolean("pcCreator", PCSaveHandler.getInstance().seenPCCreator);
        MessageClient packet = new MessageClient(MessageClient.PERSONALPC, nbt);
        PokecubePacketHandler.sendToClient(packet, entityPlayer);

    }

    /** This sends pokecube to PC if the player has a full inventory and tries
     * to pick up a pokecube.
     * 
     * @param evt */
    @SubscribeEvent
    public void playerPickupItem(EntityItemPickupEvent evt)
    {
        if (evt.getItem().worldObj.isRemote) return;
        InventoryPlayer inv = evt.getEntityPlayer().inventory;
        int num = inv.getFirstEmptyStack();
        if (!PokecubeManager.isFilled(evt.getItem().getEntityItem())) { return; }
        String owner = PokecubeManager.getOwner(evt.getItem().getEntityItem());
        if (evt.getEntityPlayer().getUniqueID().toString().equals(owner))
        {
            if (num == -1)
            {
                InventoryPC.addPokecubeToPC(evt.getItem().getEntityItem(), evt.getEntityPlayer().worldObj);
                evt.getItem().setDead();
            }
        }
        else
        {
            InventoryPC.addPokecubeToPC(evt.getItem().getEntityItem(), evt.getEntityPlayer().worldObj);
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
        if (evt.getEntityItem().worldObj.isRemote) return;
        if (PokecubeManager.isFilled(evt.getEntityItem().getEntityItem()))
        {
            InventoryPC.addPokecubeToPC(evt.getEntityItem().getEntityItem(), evt.getEntityItem().worldObj);
            evt.getEntityItem().setDead();
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
        if (PokecubeManager.isFilled(evt.getEntityItem().getEntityItem()))
        {
            if (evt.getEntityItem().worldObj.isRemote) return;
            InventoryPC.addPokecubeToPC(evt.getEntityItem().getEntityItem(), evt.getEntityItem().worldObj);
        }
    }

    /** Tries to send pokecubes to PC when player dies.
     * 
     * @param evt */
    @SubscribeEvent
    public void sendPokemobToPCPlayerDeath(PlayerDropsEvent evt)
    {
        if (!(evt.getEntity() instanceof EntityPlayer) || !PokecubeCore.core.getConfig().pcOnDrop) return;

        if (evt.getEntity().worldObj.isRemote) return;

        EntityPlayer player = (EntityPlayer) evt.getEntity();
        InventoryPlayer inv = player.inventory;

        recallAllPokemobs(player);
        for (ItemStack stack : inv.mainInventory)
        {
            if (stack != null && ContainerPC.isItemValid(stack))
            {
                InventoryPC.addStackToPC(player.getUniqueID().toString(), stack.copy());
            }
        }
        for (int i = 0; i < inv.armorInventory.length; i++)
        {
            ItemStack stack = inv.armorInventory[i];
            if (stack != null && ContainerPC.isItemValid(stack))
            {
                InventoryPC.addStackToPC(player.getUniqueID().toString(), stack.copy());
                inv.armorInventory[i] = null;
            }
        }

        for (int i = 0; i < inv.mainInventory.length; i++)
        {
            ItemStack item = inv.mainInventory[i];
            if (ContainerPC.isItemValid(item)) inv.mainInventory[i] = null;
        }

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        String uuid = player.getUniqueID().toString();
        if (!PokecubeCore.isOnClientSide())
        {
            PCSaveHandler.getInstance().savePC(uuid);
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList tags = InventoryPC.saveToNBT(player.getUniqueID().toString());

            nbt.setTag("pc", tags);

            MessageClient packet = new MessageClient(MessageClient.PERSONALPC, nbt);
            PokecubePacketHandler.sendToClient(packet, player);
        }

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
            if (catcher.worldObj.isRemote) return;

            EntityPlayer player = (EntityPlayer) catcher;
            if (player instanceof FakePlayer) return;

            InventoryPlayer inv = player.inventory;
            InventoryPC pc = InventoryPC.getPC(PokecubeManager.getOwner(evt.filledCube));
            int num = inv.getFirstEmptyStack();

            if (evt.filledCube == null || pc == null)
            {
                System.err.println("Cube is null");
            }
            else if (num == -1 || pc.autoToPC || player.isDead)
            {
                evt.setCanceled(true);
                InventoryPC.addPokecubeToPC(evt.filledCube, catcher.worldObj);
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

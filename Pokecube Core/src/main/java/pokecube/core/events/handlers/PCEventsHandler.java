package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.CaptureEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PCPacketHandler.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PCSaveHandler;
import pokecube.core.utils.Tools;

public class PCEventsHandler
{
    @SubscribeEvent
    public void sendPokemobToPCOnItemExpiration(ItemExpireEvent evt)
    {
        if (PokecubeManager.isFilled(evt.entityItem.getEntityItem()))
        {
            if (evt.entityItem.worldObj.isRemote) return;
            InventoryPC.addPokecubeToPC(evt.entityItem.getEntityItem(), evt.entityItem.worldObj);
        }
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.world.provider.getDimensionId() == 0 && !evt.world.isRemote)
        {
            PCSaveHandler.getInstance().loadPC();
        }
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.world.provider.getDimensionId() == 0 && !evt.world.isRemote)
        {
            InventoryPC.clearPC();
        }
    }

    @SubscribeEvent
    public void sendPokemobToPCPlayerDeath(LivingDeathEvent evt)
    {
        if (!(evt.entity instanceof EntityPlayer)) return;

        if (evt.entity.worldObj.isRemote) return;

        EntityPlayer player = (EntityPlayer) evt.entity;
        InventoryPlayer inv = player.inventory;

        recallAllPokemobs(player);
        int n = 0;
        for (ItemStack stack : inv.mainInventory)
        {
            if (stack != null && ContainerPC.isItemValid(stack))
            {
                InventoryPC.addStackToPC(player.getUniqueID().toString(), stack.copy());
                n++;
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
        if (!mod_Pokecube.isOnClientSide())
        {
            PCSaveHandler.getInstance().savePC(uuid);
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList tags = InventoryPC.saveToNBT(player.getUniqueID().toString());

            nbt.setTag("pc", tags);

            MessageClient packet = new MessageClient((byte) 2, nbt);
            PokecubePacketHandler.sendToClient(packet, player);
        }

    }

    @SubscribeEvent
    public void sendPokemobToPCPlayerFull(CaptureEvent.Post evt)
    {
        Entity catcher = evt.caught.getPokemonOwner();
        if (evt.caught.isShadow()) return;
        System.out.println(catcher + " " + evt.caught.getPokemonAIState(IPokemob.TAMED) + " " + evt.pokecube);
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

    @SubscribeEvent
    public void playerTossPokecubeToPC(ItemTossEvent evt)
    {
        if (evt.entityItem.worldObj.isRemote) return;
        if (PokecubeManager.isFilled(evt.entityItem.getEntityItem()) && evt.entityItem.getEntityItem().hasTagCompound())
        {
            InventoryPC.addPokecubeToPC(evt.entityItem.getEntityItem(), evt.entityItem.worldObj);
            evt.entityItem.setDead();
            evt.setCanceled(true);

        }
    }

    @SubscribeEvent
    public void PokecubeExpireToPC(ItemExpireEvent evt)
    {
        if (evt.entityItem.worldObj.isRemote) return;
        if (PokecubeManager.isFilled(evt.entityItem.getEntityItem()) && evt.entityItem.getEntityItem().hasTagCompound())
        {
            InventoryPC.addPokecubeToPC(evt.entityItem.getEntityItem(), evt.entityItem.worldObj);
        }
    }

    @SubscribeEvent
    public void playerPickupItem(EntityItemPickupEvent evt)
    {
        if (evt.item.worldObj.isRemote) return;
        InventoryPlayer inv = evt.entityPlayer.inventory;
        int num = inv.getFirstEmptyStack();

        if (num == -1)
        {
            if (PokecubeManager.isFilled(evt.item.getEntityItem()))
            {
                InventoryPC.addPokecubeToPC(evt.item.getEntityItem(), evt.entityPlayer.worldObj);
                evt.item.setDead();

            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;

        if (entityPlayer.getCommandSenderName().toLowerCase().trim().equals("thutmose"))
        {
            System.out.println(FMLCommonHandler.instance().getEffectiveSide() + "PCLoggedIn");

            PCSaveHandler.getInstance().seenPCCreator = true;
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList tags = InventoryPC.saveToNBT(entityPlayer.getUniqueID().toString());

        nbt.setTag("pc", tags);

        MessageClient packet = new MessageClient((byte) 2, nbt);
        PokecubePacketHandler.sendToClient(packet, entityPlayer);

    }

    @SubscribeEvent
    public void PCLoggin(EntityJoinWorldEvent evt)
    {
        if (!(evt.entity instanceof EntityPlayer)) return;

        EntityPlayer entityPlayer = (EntityPlayer) evt.entity;

        if (entityPlayer.getCommandSenderName().toLowerCase().trim().equals("thutmose"))
        {
            // System.out.println("Thutmose logged in");
            for (Object o : evt.world.playerEntities)
            {
                if (o instanceof EntityPlayer)
                {
                    EntityPlayer p = (EntityPlayer) o;
                    // System.out.println(p);
                    if (InventoryPC.map.containsKey(p.getUniqueID().toString()))
                    {
                        InventoryPC.getPC(p.getUniqueID().toString()).seenOwner = true;

                        if (evt.world.isRemote) continue;

                        NBTTagCompound nbt = new NBTTagCompound();
                        NBTTagList tags = InventoryPC.saveToNBT(p.getUniqueID().toString());

                        nbt.setTag("pc", tags);
                        MessageClient packet = new MessageClient((byte) 2, nbt);
                        PokecubePacketHandler.sendToClient(packet, p);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggout(PlayerLoggedOutEvent evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            InventoryPC.clearPC();
        }
    }

    @SubscribeEvent
    public void KillEvent(pokecube.core.events.KillEvent evt)
    {
        IPokemob killer = evt.killer;
        IPokemob killed = evt.killed;

        if (killer != null)
        {
            EntityLivingBase owner = killer.getPokemonOwner();

            ItemStack stack = ((EntityLivingBase) killer).getHeldItem();
            if (stack != null && PokecubeItems.getStack("luckyegg").isItemEqual(stack))
            {
                int exp = killer.getExp() + Tools.getExp(1, killed.getBaseXP(), killed.getLevel());

                killer.setExp(exp, true, false);
            }

            if (owner != null)
            {

                List<IPokemob> pokemobs = getOutMobs(owner);
                for (IPokemob mob : pokemobs)
                {
                    if (mob instanceof IPokemob)
                    {
                        IPokemob poke = (IPokemob) mob;
                        if (((EntityLiving) poke).getHeldItem() != null)
                            if (((EntityLiving) poke).getHeldItem().isItemEqual(PokecubeItems.getStack("exp_share")))
                        {
                            int exp = poke.getExp() + Tools.getExp(1, killed.getBaseXP(), killed.getLevel());

                            poke.setExp(exp, true, false);
                        }
                    }
                }
            }
        }
    }

    public static void recallAllPokemobs(Entity player)
    {
        List pokemobs = new ArrayList(player.worldObj.loadedEntityList);
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
                    if (name != null && (name.equalsIgnoreCase(player.getCommandSenderName())
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
            if (!mod_Pokecube.isOnClientSide())
            {
                PCSaveHandler.getInstance().savePC(uuid);
                NBTTagCompound nbt = new NBTTagCompound();
                NBTTagList tags = InventoryPC.saveToNBT(player.getUniqueID().toString());

                nbt.setTag("pc", tags);

                MessageClient packet = new MessageClient((byte) 2, nbt);
                PokecubePacketHandler.sendToClient(packet, (EntityPlayer) player);
            }
        }
    }

    public static List<IPokemob> getOutMobs(EntityLivingBase player)
    {
        List pokemobs = new ArrayList(player.worldObj.loadedEntityList);
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
}

package pokecube.adventures.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.handlers.ConfigHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.StarterEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.utils.PCSaveHandler;

public class PAEventsHandler
{

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        NBTTagCompound nbt;
        MessageClient packet;

        nbt = new NBTTagCompound();
        TeamManager.getInstance().saveToNBT(nbt, false);
        packet = new MessageClient((byte) 7, nbt);
        PokecubePacketHandler.sendToClient(packet, entityPlayer);

    }

    @SubscribeEvent
    public void PlayerStarter(StarterEvent evt)
    {
        String playerName = evt.player.getName().toLowerCase();
        if (evt.starterPack == null)
        {
            if (PokecubePacketHandler.specialStarters.containsKey(playerName))
            {
                StarterInfo[] info = PokecubePacketHandler.specialStarters.get(playerName);
                for (StarterInfo i : info)
                {
                    if (i == null || i.name == null)
                    {
                        evt.setCanceled(true);
                    }
                }
            }
            return;
        }

        if (!ConfigHandler.overrides.contains(playerName))
        {
            ItemStack[] temp = evt.starterPack.clone();
            evt.starterPack = new ItemStack[temp.length + 1];
            for (int i = 0; i < temp.length; i++)
                evt.starterPack[i] = temp[i];
            evt.starterPack[temp.length] = PokecubeItems.getStack("pokecubebag");
            return;
        }

        ItemStack[] temp = evt.starterPack.clone();
        evt.starterPack = new ItemStack[temp.length + 1];
        for (int i = 0; i < temp.length; i++)
            evt.starterPack[i] = temp[i];
        evt.starterPack[temp.length] = PokecubeItems.getStack("pokecubebag");

        if (!evt.player.worldObj.isRemote)
        {
            List<ItemStack> toPC = new ArrayList<ItemStack>();
            ItemStack starter = null;
            boolean replaced = false;

            if (PokecubePacketHandler.specialStarters.containsKey(playerName))
            {
                StarterInfo[] info = PokecubePacketHandler.specialStarters.get(playerName);
                for (StarterInfo i : info)
                {
                    if (i != null && i.name != null)
                    {
                        replaced = true;
                    }
                }
            }

            if (!replaced) for (int i = 0; i < evt.starterPack.length; i++)
            {
                ItemStack e = evt.starterPack[i];
                int num;
                if ((num = PokecubeManager.getPokedexNb(e)) > 0 && num != evt.pick)
                {
                    if (e != starter) toPC.add(e);
                    evt.starterPack[i] = null;
                    if (!replaced)
                    {
                        replaced = true;
                        evt.starterPack[i] = starter;
                    }
                }
            }

            for (ItemStack e : toPC)
            {
                InventoryPC.addStackToPC(evt.player.getUniqueID().toString(), e);
            }
            PCSaveHandler.getInstance().savePC();
        }
    }

    @SubscribeEvent
    public void TrainerRecallEvent(pokecube.core.events.RecallEvent evt)
    {
        IPokemob recalled = evt.recalled;
        EntityLivingBase owner = recalled.getPokemonOwner();
        if (owner instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) owner;
            t.outID = null;
            t.outMob = null;
            System.out.println("Recalling " + recalled);
            t.addPokemob(PokecubeManager.pokemobToItem(recalled));
        }
    }

    @SubscribeEvent
    public void TrainerSendOutEvent(SendOut evt)
    {
        IPokemob sent = evt.pokemob;
        EntityLivingBase owner = sent.getPokemonOwner();
        if (owner instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) owner;
            t.setAIState(EntityTrainer.THROWING, false);
            if (t.outMob != null)
            {
                t.outMob.returnToPokecube();
            }
            System.out.println("Sent out " + evt.pokemob);
            t.outID = evt.entity.getUniqueID();
            t.outMob = evt.pokemob;
        }
    }
}

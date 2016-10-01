package pokecube.adventures.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import pokecube.adventures.entity.helper.EntityHasAIStates;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StarterEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;

public class PAEventsHandler
{

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {

    }

    @SubscribeEvent
    public void PlayerStarter(StarterEvent.Pick evt)
    {
        evt.starterPack.add(PokecubeItems.getStack("pokecubebag"));
    }

    @SubscribeEvent
    public void TrainerPokemobPC(PCEvent evt)
    {
        if (evt.owner instanceof EntityTrainer)
        {
            evt.setCanceled(true);
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
            if (recalled == t.outMob)
            {
                t.outMob = null;
            }
            t.addPokemob(PokecubeManager.pokemobToItem(recalled));
        }
    }

    @SubscribeEvent
    public void TrainerSendOutEvent(SendOut.Post evt)
    {
        IPokemob sent = evt.pokemob;
        EntityLivingBase owner = sent.getPokemonOwner();
        if (owner instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) owner;
            if (t.outMob != null && t.outMob != evt.pokemob)
            {
                t.outMob.returnToPokecube();
                t.outMob = evt.pokemob;
                t.setAIState(EntityHasAIStates.THROWING, false);
            }
            else
            {
                t.outMob = evt.pokemob;
                t.setAIState(EntityHasAIStates.THROWING, false);
            }
        }
    }

    @SubscribeEvent
    public void TrainerWatchEvent(StartTracking event)
    {
        if (event.getTarget() instanceof EntityTrainer && event.getEntityPlayer() instanceof EntityPlayerMP)
        {
            EntityTrainer trainer = (EntityTrainer) event.getTarget();
            if (trainer.notifyDefeat)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                packet.data.setInteger("I", trainer.getEntityId());
                packet.data.setBoolean("V", trainer.hasDefeated(event.getEntityPlayer()));
                PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }
}
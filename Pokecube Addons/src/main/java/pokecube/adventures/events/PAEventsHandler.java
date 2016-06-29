package pokecube.adventures.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StarterEvent;
import pokecube.core.interfaces.IPokemob;
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
            t.outID = evt.entity.getUniqueID();
            t.outMob = evt.pokemob;
        }
    }
}

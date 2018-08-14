package pokecube.adventures.ai.tasks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.core.ai.thread.aiRunnables.AIBase;

public class AITrainerBase extends AIBase
{
    World                  world;
    // The trainer Entity
    final EntityLivingBase entity;
    final IHasPokemobs     trainer;
    final IHasNPCAIStates  aiTracker;
    final IHasMessages     messages;
    final boolean          valid;
    int                    noSeeTicks = 0;

    public AITrainerBase(EntityLivingBase trainer)
    {
        this.entity = trainer;
        this.world = trainer.getEntityWorld();
        this.aiTracker = CapabilityNPCAIStates.getNPCAIStates(trainer);
        this.trainer = CapabilityHasPokemobs.getHasPokemobs(trainer);
        this.messages = CapabilityNPCMessages.getMessages(trainer);
        valid = trainer != null && aiTracker != null && messages != null;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
    }

    @Override
    public boolean shouldRun()
    {
        return false;
    }
}

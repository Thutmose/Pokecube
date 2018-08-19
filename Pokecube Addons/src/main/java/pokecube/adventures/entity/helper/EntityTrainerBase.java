package pokecube.adventures.entity.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public abstract class EntityTrainerBase extends EntityHasTrades
{
    public List<IPokemob>  currentPokemobs = new ArrayList<IPokemob>();
    public DefaultPokemobs pokemobsCap;
    public IHasMessages    messages;
    public IHasRewards     rewardsCap;
    public IHasNPCAIStates aiStates;
    int                    despawncounter  = 0;

    public EntityTrainerBase(World worldIn)
    {
        super(worldIn);
        pokemobsCap = (DefaultPokemobs) this.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        rewardsCap = this.getCapability(CapabilityHasRewards.REWARDS_CAP, null);
        this.messages = getCapability(CapabilityNPCMessages.MESSAGES_CAP, null);
        this.aiStates = getCapability(CapabilityNPCAIStates.AISTATES_CAP, null);
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (!isServerWorld()) return;
        if (pokemobsCap.getOutID() != null && pokemobsCap.getOutMob() == null)
        {
            Entity mob = getServer().getWorld(dimension).getEntityFromUuid(pokemobsCap.getOutID());
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            pokemobsCap.setOutMob(pokemob);
            if (pokemobsCap.getOutMob() == null) pokemobsCap.setOutID(null);
        }
        if (pokemobsCap.countPokemon() == 0 && !aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                && !aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
        {
            despawncounter++;
            if (despawncounter > 200)
            {
                this.setDead();
            }
            return;
        }
        if (this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0)
        {
            this.setHealth(Math.min(this.getHealth() + 1, this.getMaxHealth()));
        }
        despawncounter = 0;
    }
}
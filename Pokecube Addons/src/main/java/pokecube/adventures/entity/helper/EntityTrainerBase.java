package pokecube.adventures.entity.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.interfaces.IPokemob;
import thut.lib.CompatWrapper;

public abstract class EntityTrainerBase extends EntityHasTrades
{
    public List<IPokemob>  currentPokemobs = new ArrayList<IPokemob>();
    public DefaultPokemobs pokemobsCap;
    protected IHasMessages messages;
    protected IHasRewards  rewardsCap;
    public IHasNPCAIStates    aiStates;
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
            pokemobsCap.setOutMob((IPokemob) getServer().worldServerForDimension(dimension)
                    .getEntityFromUuid(pokemobsCap.getOutID()));
            if (pokemobsCap.getOutMob() == null) pokemobsCap.setOutID(null);
        }
        if (pokemobsCap.countPokemon() == 0 && !aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                && !aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
        {
            despawncounter++;
            if (despawncounter > 50)
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

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        // Below here is legacy support for loading from nbt.
        if (nbt.hasKey("reward", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("reward", 10);
            rewardsCap.getRewards().clear();
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                rewardsCap.getRewards().add(CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
            }
        }
        if (nbt.hasKey("pokemobs", 9))
        {
            pokemobsCap.clear();
            NBTTagList nbttaglist = nbt.getTagList("pokemobs", 10);
            if (nbttaglist.tagCount() != 0) for (int i = 0; i < Math.min(nbttaglist.tagCount(), 6); ++i)
            {
                pokemobsCap.setPokemob(i, CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
            }
            pokemobsCap.setType(TypeTrainer.getTrainer(nbt.getString("type")));
            pokemobsCap.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.hasKey("outPokemob"))
            {
                pokemobsCap.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            }
            pokemobsCap.setNextSlot(nbt.getInteger("nextSlot"));
            if (pokemobsCap.getNextSlot() >= 6) pokemobsCap.setNextSlot(0);

            if (nbt.hasKey("battleCD")) pokemobsCap.battleCooldown = nbt.getInteger("battleCD");
            if (pokemobsCap.battleCooldown < 0) pokemobsCap.battleCooldown = Config.instance.trainerCooldown;
            pokemobsCap.friendlyCooldown = nbt.getInteger("friendly");

        }
        if (nbt.hasKey("messages"))
        {
            NBTTagCompound messTag = nbt.getCompoundTag("messages");
            for (MessageState state : MessageState.values())
            {
                if (messTag.hasKey(state.name())) messages.setMessage(state, messTag.getString(state.name()));
            }
            NBTTagCompound actionTag = nbt.getCompoundTag("actions");
            for (MessageState state : MessageState.values())
            {
                if (actionTag.hasKey(state.name()))
                    messages.setAction(state, new Action(actionTag.getString(state.name())));
            }
        }
        if (nbt.hasKey("aiState")) aiStates.setTotalState(nbt.getInteger("aiState"));
        // End of legacy support for loading from NBT.
    }
}
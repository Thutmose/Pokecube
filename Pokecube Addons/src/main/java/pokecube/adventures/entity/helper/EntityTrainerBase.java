package pokecube.adventures.entity.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates.IHasAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityMessages.IHasMessages;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.interfaces.IPokemob;
import thut.lib.CompatWrapper;

public abstract class EntityTrainerBase extends EntityHasTrades implements IHasPokemobs, IHasRewards
{
    public List<IPokemob>  currentPokemobs  = new ArrayList<IPokemob>();
    protected IHasPokemobs pokemobsCap;
    protected IHasMessages messages;
    protected IHasRewards  rewardsCap;
    public IHasAIStates    aiStates;
    protected int          friendlyCooldown = 0;
    int                    despawncounter   = 0;

    public EntityTrainerBase(World worldIn)
    {
        super(worldIn);
        pokemobsCap = this.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        rewardsCap = this.getCapability(CapabilityHasRewards.REWARDS_CAP, null);
        this.messages = getCapability(CapabilityMessages.MESSAGES_CAP, null);
        this.aiStates = getCapability(CapabilityAIStates.AISTATES_CAP, null);
    }

    @Override
    public void onAddMob()
    {
        pokemobsCap.onAddMob();
    }

    @Override
    public EntityLivingBase getTarget()
    {
        return pokemobsCap.getTarget();
    }

    @Override
    public TypeTrainer getType()
    {
        return pokemobsCap.getType();
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (!isServerWorld()) return;
        if (getOutID() != null && getOutMob() == null)
        {
            setOutMob((IPokemob) getServer().getWorld(dimension).getEntityFromUuid(getOutID()));
            if (getOutMob() == null) setOutID(null);
        }
        if (this.countPokemon() == 0 && !aiStates.getAIState(IHasAIStates.STATIONARY)
                && !aiStates.getAIState(IHasAIStates.PERMFRIENDLY))
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

        friendlyCooldown = nbt.getInteger("friendly");

        // Below here is legacy support for loading from nbt.
        if (nbt.hasKey("reward", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("reward", 10);
            getRewards().clear();
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                getRewards().add(CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
            }
        }
        if (nbt.hasKey("pokemobs", 9))
        {
            getPokecubes().clear();
            NBTTagList nbttaglist = nbt.getTagList("pokemobs", 10);
            if (nbttaglist.tagCount() != 0) for (int i = 0; i < Math.min(nbttaglist.tagCount(), 6); ++i)
            {
                getPokecubes().add(CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
            }
            setType(TypeTrainer.getTrainer(nbt.getString("type")));
            setCooldown(nbt.getLong("nextBattle"));
            if (nbt.hasKey("outPokemob"))
            {
                setOutID(UUID.fromString(nbt.getString("outPokemob")));
            }
            setNextSlot(nbt.getInteger("nextSlot"));
            if (getNextSlot() >= 6) setNextSlot(0);
            if (pokemobsCap instanceof DefaultPokemobs)
            {
                DefaultPokemobs cap = (DefaultPokemobs) pokemobsCap;
                if (nbt.hasKey("battleCD")) cap.battleCooldown = nbt.getInteger("battleCD");
                if (cap.battleCooldown < 0) cap.battleCooldown = Config.instance.trainerCooldown;
            }
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

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("friendly", friendlyCooldown);
    }

    @Override
    public void lowerCooldowns()
    {
        if (aiStates.getAIState(IHasAIStates.PERMFRIENDLY)) { return; }
        if (friendlyCooldown-- >= 0) return;
        pokemobsCap.lowerCooldowns();
    }

    @Override
    public void throwCubeAt(Entity target)
    {
        pokemobsCap.throwCubeAt(target);
    }

    @Override
    public void setTarget(EntityLivingBase target)
    {
        pokemobsCap.setTarget(target);
    }

    @Override
    public void resetPokemob()
    {
        pokemobsCap.resetPokemob();
    }

    @Override
    public List<ItemStack> getPokecubes()
    {
        return pokemobsCap.getPokecubes();
    }

    @Override
    public int getNextSlot()
    {
        return pokemobsCap.getNextSlot();
    }

    @Override
    public List<ItemStack> getRewards()
    {
        return rewardsCap.getRewards();
    }

    @Override
    public int getAttackCooldown()
    {
        return pokemobsCap.getAttackCooldown();
    }

    @Override
    public void setAttackCooldown(int value)
    {
        pokemobsCap.setAttackCooldown(value);
    }

    @Override
    public void setNextSlot(int value)
    {
        pokemobsCap.setNextSlot(value);
    }

    @Override
    public void setOutMob(IPokemob mob)
    {
        pokemobsCap.setOutMob(mob);
    }

    @Override
    public IPokemob getOutMob()
    {
        return pokemobsCap.getOutMob();
    }

    @Override
    public void setOutID(UUID mob)
    {
        pokemobsCap.setOutID(mob);
    }

    @Override
    public UUID getOutID()
    {
        return pokemobsCap.getOutID();
    }

    @Override
    public long getCooldown()
    {
        return pokemobsCap.getCooldown();
    }

    @Override
    public void setCooldown(long value)
    {
        pokemobsCap.setCooldown(value);
    }

    @Override
    public void setType(TypeTrainer type)
    {
        pokemobsCap.setType(type);
    }

    @Override
    public boolean isAgressive()
    {
        return friendlyCooldown < 0;
    }
}
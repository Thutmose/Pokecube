package pokecube.adventures.entity.helper.capabilities;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import thut.lib.CompatWrapper;

public class CapabilityHasRewards
{
    @CapabilityInject(IHasRewards.class)
    public static final Capability<IHasRewards> REWARDS_CAP = null;
    public static Storage                       storage;

    public static IHasRewards getHasRewards(ICapabilityProvider entityIn)
    {
        IHasRewards pokemobHolder = null;
        if (entityIn == null) return null;
        if (entityIn.hasCapability(REWARDS_CAP, null)) pokemobHolder = entityIn.getCapability(REWARDS_CAP, null);
        else if (entityIn instanceof IHasRewards) return (IHasRewards) entityIn;
        return pokemobHolder;
    }

    public static class Reward
    {
        public final ItemStack stack;
        public final float     chance;

        public Reward(ItemStack stack, float chance)
        {
            this.stack = stack;
            this.chance = chance;
        }

        public Reward(ItemStack stack)
        {
            this(stack, 1);
        }
    }

    public static interface IHasRewards
    {
        List<Reward> getRewards();

        default void giveReward(EntityPlayer player, EntityLivingBase rewarder)
        {
            for (Reward reward : getRewards())
            {
                ItemStack i = reward.stack;
                if (!CompatWrapper.isValid(i)) continue;
                if (new Random().nextFloat() > reward.chance) continue;
                if (!player.inventory.addItemStackToInventory(i.copy()))
                {
                    EntityItem item = player.entityDropItem(i.copy(), 0.5f);
                    if (item == null)
                    {
                        continue;
                    }
                    item.setPickupDelay(0);
                }
                IHasMessages messageSender = CapabilityNPCMessages.getMessages(rewarder);
                if (messageSender != null)
                {
                    messageSender.sendMessage(MessageState.GIVEITEM, player, rewarder.getDisplayName(),
                            i.getDisplayName(), player.getDisplayName());
                    messageSender.doAction(MessageState.GIVEITEM, player);
                }
            }
        }
    }

    public static class Storage implements Capability.IStorage<IHasRewards>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasRewards> capability, IHasRewards instance, EnumFacing side)
        {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < instance.getRewards().size(); ++i)
            {
                ItemStack stack = instance.getRewards().get(i).stack;

                if (CompatWrapper.isValid(stack))
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    stack.writeToNBT(nbttagcompound);
                    nbttagcompound.setFloat("chance", instance.getRewards().get(i).chance);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }
            return nbttaglist;
        }

        @Override
        public void readNBT(Capability<IHasRewards> capability, IHasRewards instance, EnumFacing side, NBTBase base)
        {
            if (!(base instanceof NBTTagList)) return;
            NBTTagList nbttaglist = (NBTTagList) base;
            instance.getRewards().clear();
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
                ItemStack stack = new ItemStack(tag);
                float chance = tag.hasKey("chance") ? tag.getFloat("chance") : 1;
                instance.getRewards().add(new Reward(stack, chance));
            }
        }

    }

    public static class DefaultRewards implements IHasRewards, ICapabilitySerializable<NBTTagList>
    {
        private final List<Reward> rewards = Lists.newArrayList();

        @Override
        public List<Reward> getRewards()
        {
            return rewards;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == REWARDS_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? REWARDS_CAP.cast(this) : null;
        }

        @Override
        public NBTTagList serializeNBT()
        {
            return (NBTTagList) storage.writeNBT(REWARDS_CAP, this, null);
        }

        @Override
        public void deserializeNBT(NBTTagList nbt)
        {
            storage.readNBT(REWARDS_CAP, this, null, nbt);
        }

    }
}

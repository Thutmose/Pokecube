package pokecube.adventures.entity.helper.capabilities;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.adventures.entity.helper.Action;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.core.interfaces.PokecubeMod;

public class CapabilityNPCMessages
{
    @CapabilityInject(IHasMessages.class)
    public static final Capability<IHasMessages> MESSAGES_CAP = null;
    public static Storage                        storage;

    public static IHasMessages getMessages(ICapabilityProvider entityIn)
    {
        IHasMessages pokemobHolder = null;
        if (entityIn == null) return null;
        if (entityIn.hasCapability(MESSAGES_CAP, null)) pokemobHolder = entityIn.getCapability(MESSAGES_CAP, null);
        else if (entityIn instanceof IHasMessages) return (IHasMessages) entityIn;
        return pokemobHolder;
    }

    public static interface IHasMessages
    {
        void sendMessage(MessageState state, Entity target, Object... args);

        void doAction(MessageState state, EntityLivingBase target);

        void setMessage(MessageState state, String message);

        void setAction(MessageState state, Action action);

        String getMessage(MessageState state);

        Action getAction(MessageState state);
    }

    public static class Storage implements Capability.IStorage<IHasMessages>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasMessages> capability, IHasMessages instance, EnumFacing side)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagCompound messTag = new NBTTagCompound();
            NBTTagCompound actionTag = new NBTTagCompound();
            for (MessageState state : MessageState.values())
            {
                String message = instance.getMessage(state);
                if (message != null && !message.isEmpty()) messTag.setString(state.name(), message);
                Action action = instance.getAction(state);
                if (action != null && !action.getCommand().isEmpty())
                    actionTag.setString(state.name(), action.getCommand());
            }
            nbt.setTag("messages", messTag);
            nbt.setTag("actions", actionTag);
            return nbt;
        }

        @Override
        public void readNBT(Capability<IHasMessages> capability, IHasMessages instance, EnumFacing side, NBTBase base)
        {
            if (!(base instanceof NBTTagCompound)) return;
            NBTTagCompound nbt = (NBTTagCompound) base;
            if (!nbt.hasKey("messages")) return;
            NBTTagCompound messTag = nbt.getCompoundTag("messages");
            for (MessageState state : MessageState.values())
            {
                if (messTag.hasKey(state.name())) instance.setMessage(state, messTag.getString(state.name()));
            }
            NBTTagCompound actionTag = nbt.getCompoundTag("actions");
            for (MessageState state : MessageState.values())
            {
                if (actionTag.hasKey(state.name()))
                    instance.setAction(state, new Action(actionTag.getString(state.name())));
            }
        }
    }

    public static class DefaultMessager implements IHasMessages, ICapabilitySerializable<NBTTagCompound>
    {
        Map<MessageState, String> messages = Maps.newHashMap();
        Map<MessageState, Action> actions  = Maps.newHashMap();

        public DefaultMessager()
        {
            messages.put(MessageState.AGRESS, "pokecube.trainer.agress");
            messages.put(MessageState.ABOUTSEND, "pokecube.trainer.next");
            messages.put(MessageState.SENDOUT, "pokecube.trainer.toss");
            messages.put(MessageState.DEFEAT, "pokecube.trainer.defeat");
            messages.put(MessageState.DEAGRESS, "pokecube.trainer.forget");
            messages.put(MessageState.GIVEITEM, "pokecube.trainer.drop");
        }

        @Override
        public void sendMessage(MessageState state, Entity target, Object... args)
        {
            if (target instanceof FakePlayer || messages.get(state) == null || messages.get(state).trim().isEmpty())
                return;
            target.sendMessage(new TextComponentTranslation(messages.get(state), args));
            if (PokecubeMod.debug) PokecubeMod.log(state + ": " + messages.get(state));
        }

        @Override
        public void doAction(MessageState state, EntityLivingBase target)
        {
            if (target instanceof FakePlayer) return;
            Action action = actions.get(state);
            if (action != null && target instanceof EntityPlayer) action.doAction((EntityPlayer) target);
        }

        @Override
        public void setMessage(MessageState state, String message)
        {
            messages.put(state, message);
        }

        @Override
        public void setAction(MessageState state, Action action)
        {
            actions.put(state, action);
        }

        @Override
        public String getMessage(MessageState state)
        {
            return messages.get(state);
        }

        @Override
        public Action getAction(MessageState state)
        {
            return actions.get(state);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == MESSAGES_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? MESSAGES_CAP.cast(this) : null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            return (NBTTagCompound) storage.writeNBT(MESSAGES_CAP, this, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            storage.readNBT(MESSAGES_CAP, this, null, nbt);
        }

    }
}

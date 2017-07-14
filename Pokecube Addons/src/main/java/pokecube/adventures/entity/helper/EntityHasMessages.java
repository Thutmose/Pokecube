package pokecube.adventures.entity.helper;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public abstract class EntityHasMessages extends EntityHasAIStates
{
    Map<MessageState, String> messages = Maps.newHashMap();
    Map<MessageState, Action> actions  = Maps.newHashMap();

    public EntityHasMessages(World worldIn)
    {
        super(worldIn);
        messages.put(MessageState.AGRESS, "pokecube.trainer.agress");
        messages.put(MessageState.ABOUTSEND, "pokecube.trainer.next");
        messages.put(MessageState.SENDOUT, "pokecube.trainer.toss");
        messages.put(MessageState.DEFEAT, "pokecube.trainer.defeat");
        messages.put(MessageState.DEAGRESS, "pokecube.trainer.forget");
        messages.put(MessageState.GIVEITEM, "pokecube.trainer.drop");
    }

    public ITextComponent getMessage(MessageState state, Object... args)
    {
        return new TextComponentTranslation(messages.get(state), args);
    }

    public void doAction(MessageState state, EntityLivingBase target)
    {
        Action action = actions.get(state);
        if (action != null && target instanceof EntityPlayer) action.doAction((EntityPlayer) target);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (!nbt.hasKey("messages")) return;
        NBTTagCompound messTag = nbt.getCompoundTag("messages");
        for (MessageState state : MessageState.values())
        {
            if (messTag.hasKey(state.name())) messages.put(state, messTag.getString(state.name()));
        }
        NBTTagCompound actionTag = nbt.getCompoundTag("actions");
        for (MessageState state : MessageState.values())
        {
            if (actionTag.hasKey(state.name())) actions.put(state, new Action(actionTag.getString(state.name())));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        NBTTagCompound messTag = new NBTTagCompound();
        for (MessageState state : MessageState.values())
            if (messages.containsKey(state)) messTag.setString(state.name(), messages.get(state));
        nbt.setTag("messages", messTag);
        NBTTagCompound actionTag = new NBTTagCompound();
        for (MessageState state : MessageState.values())
            if (actions.containsKey(state)) actionTag.setString(state.name(), actions.get(state).getCommand());
        nbt.setTag("actions", actionTag);
    }
}

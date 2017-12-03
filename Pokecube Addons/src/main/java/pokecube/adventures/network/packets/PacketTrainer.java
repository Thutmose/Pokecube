package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.utils.TimePeriod;
import thut.api.network.PacketHandler;

public class PacketTrainer implements IMessage, IMessageHandler<PacketTrainer, IMessage>
{
    public static final byte MESSAGEUPDATETRAINER = 0;
    public static final byte MESSAGENOTIFYDEFEAT  = 1;
    public static final byte MESSAGEKILLTRAINER   = 2;

    byte                     message;
    public NBTTagCompound    data                 = new NBTTagCompound();

    public PacketTrainer()
    {
    }

    public PacketTrainer(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketTrainer message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        message = buf.readByte();
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketTrainer message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        if (message.message == MESSAGEUPDATETRAINER)
        {
            NBTBase tag = message.data.getTag("T");
            int id = message.data.getInteger("I");
            Entity mob = player.getEntityWorld().getEntityByID(id);
            IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(mob);
            if (cap != null)
            {
                IHasMessages messages = CapabilityNPCMessages.getMessages(mob);
                IHasRewards rewards = CapabilityHasRewards.getHasRewards(mob);
                IHasNPCAIStates ai = CapabilityNPCAIStates.getNPCAIStates(mob);
                IGuardAICapability guard = mob.getCapability(EventsHandler.GUARDAI_CAP, null);
                ITextComponent mess = null;
                if (message.data.hasKey("S") && ai != null)
                {
                    ai.setAIState(IHasNPCAIStates.STATIONARY, message.data.getBoolean("S"));
                    mess = new TextComponentTranslation("traineredit.set.stationary." + message.data.getBoolean("S"));
                    player.sendStatusMessage(mess, true);
                }
                if (message.data.hasKey("X"))
                {
                    cap.setGender(message.data.getByte("X"));
                    mess = new TextComponentTranslation("traineredit.set.gender." + message.data.getByte("X"));
                    player.sendStatusMessage(mess, true);
                }
                if (message.data.hasKey("K"))
                {
                    TypeTrainer type = TypeTrainer.getTrainer(message.data.getString("K"));
                    if (type != cap.getType())
                    {
                        TypeTrainer old = cap.getType();
                        String prefix = old.name + " ";
                        if (mob.getName().startsWith(prefix))
                        {
                            mob.setCustomNameTag(mob.getName().replaceFirst(old.name, type.name));
                        }
                        mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, type.held.copy());
                        mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, type.bag.copy());
                        mess = new TextComponentTranslation("traineredit.set.type", type.name);
                        cap.setType(type);
                    }
                }
                if (message.data.hasKey("N"))
                {
                    mob.setCustomNameTag(message.data.getString("N"));
                    if (mob instanceof EntityTrainer)
                    {
                        ((EntityTrainer) mob).name = mob.getCustomNameTag().replaceFirst(cap.getType() + " ", "");
                    }
                    mess = new TextComponentTranslation("traineredit.set.name", message.data.getString("N"));
                }
                if (mob instanceof EntityTrainer)
                {
                    EntityTrainer trainer = (EntityTrainer) mob;
                    if (message.data.hasKey("U"))
                    {
                        trainer.urlSkin = message.data.getString("U");
                    }
                    if (message.data.hasKey("P"))
                    {
                        trainer.playerName = message.data.getString("P");
                    }
                }
                boolean stationaryBefore = ai.getAIState(IHasNPCAIStates.STATIONARY);
                if (tag != null && !tag.hasNoTags())
                {
                    byte type = message.data.getByte("V");
                    if (type == 0)
                        CapabilityHasPokemobs.storage.readNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, cap, null, tag);
                    else if (type == 1)
                        CapabilityHasRewards.storage.readNBT(CapabilityHasRewards.REWARDS_CAP, rewards, null, tag);
                    else if (type == 2)
                        CapabilityNPCMessages.storage.readNBT(CapabilityNPCMessages.MESSAGES_CAP, messages, null, tag);
                    else if (type == 3)
                        CapabilityNPCAIStates.storage.readNBT(CapabilityNPCAIStates.AISTATES_CAP, ai, null, tag);
                    else if (type == 4) EventsHandler.storage.readNBT(EventsHandler.GUARDAI_CAP, guard, null, tag);
                }
                if (mess != null) player.sendStatusMessage(mess, true);
                if (ai.getAIState(IHasNPCAIStates.STATIONARY) != stationaryBefore)
                {
                    if (guard != null)
                    {
                        guard.setPos(mob.getPosition());
                        guard.setActiveTime(stationaryBefore ? new TimePeriod(0, 0) : TimePeriod.fullDay);
                        guard.setRoamDistance(stationaryBefore ? 16 : 0);
                    }
                }

                PacketHandler.sendEntityUpdate(mob);
            }
            return;
        }
        if (message.message == MESSAGENOTIFYDEFEAT)
        {
            int id = message.data.getInteger("I");
            EntityLivingBase mob = (EntityLivingBase) player.getEntityWorld().getEntityByID(id);
            if (mob instanceof EntityTrainer) ((EntityTrainer) mob).visibleTime = message.data.getLong("L");
            return;
        }
        if (message.message == MESSAGEKILLTRAINER)
        {
            int id = message.data.getInteger("I");
            EntityTrainer trainer = (EntityTrainer) player.getEntityWorld().getEntityByID(id);
            trainer.setDead();
        }
    }

}

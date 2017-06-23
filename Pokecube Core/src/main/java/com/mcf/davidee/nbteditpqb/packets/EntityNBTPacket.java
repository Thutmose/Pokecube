package com.mcf.davidee.nbteditpqb.packets;

import org.apache.logging.log4j.Level;

import com.mcf.davidee.nbteditpqb.NBTEdit;
import com.mcf.davidee.nbteditpqb.NBTHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class EntityNBTPacket implements IMessage {
	/** The id of the entity being edited. */
	protected int entityID;
	/** The nbt data of the entity. */
	protected NBTTagCompound tag;

	/** Required default constructor. */
	public EntityNBTPacket() {}

	public EntityNBTPacket(int entityID, NBTTagCompound tag) {
		this.entityID = entityID;
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityID = buf.readInt();
		this.tag = NBTHelper.readNbtFromBuffer(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityID);
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public static class Handler implements IMessageHandler<EntityNBTPacket, IMessage> {

		@Override
		public IMessage onMessage(final EntityNBTPacket packet, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				final EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						Entity entity = player.world.getEntityByID(packet.entityID);
						if (entity != null && NBTEdit.proxy.checkPermission(player)) {
							try {
								GameType preGameType = player.interactionManager.getGameType();
								entity.readFromNBT(packet.tag);
								NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Entity ID #" + packet.entityID);
								NBTEdit.logTag(packet.tag);
								if (entity == player) { //Update player info
									// This is fairly hacky. Consider swapping to an event driven system, where classes can register to
									// receive entity edit events and provide feedback/send packets as necessary.

									player.sendContainerToPlayer(player.inventoryContainer);
									GameType type = player.interactionManager.getGameType();
									if (preGameType != type)
										player.setGameType(type);
									player.connection.sendPacket(new SPacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
									player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
									player.sendPlayerAbilities();
								}
								thut.api.network.PacketHandler.sendEntityUpdate(entity);
								NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
							} catch (Throwable t) {
								NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", TextFormatting.RED);
								NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
								NBTEdit.logTag(packet.tag);
								NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
							}
						} else {
							NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", TextFormatting.RED);
						}
					}
				});
			} else {
				NBTEdit.proxy.openEditGUI(packet.entityID, packet.tag);
			}
			return null;
		}
	}
}

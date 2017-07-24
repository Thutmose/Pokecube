package com.mcf.davidee.nbteditpqb.packets;

import org.apache.logging.log4j.Level;

import com.mcf.davidee.nbteditpqb.NBTEdit;
import com.mcf.davidee.nbteditpqb.NBTHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TileNBTPacket implements IMessage {
	/** The block of the tileEntity. */
	protected BlockPos pos;
	/** The nbt data of the tileEntity. */
	protected NBTTagCompound tag;

	/** Required default constructor. */
	public TileNBTPacket() {}

	public TileNBTPacket(BlockPos pos, NBTTagCompound tag) {
		this.pos = pos;
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = BlockPos.fromLong(buf.readLong());
		this.tag = NBTHelper.readNbtFromBuffer(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(this.pos.toLong());
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public static class Handler implements IMessageHandler<TileNBTPacket, IMessage> {

		@Override
		public IMessage onMessage(final TileNBTPacket packet, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				final EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						TileEntity te = player.world.getTileEntity(packet.pos);
						if (te != null && NBTEdit.proxy.checkPermission(player)) {
							try {
								te.readFromNBT(packet.tag);
								te.markDirty();// Ensures changes gets saved to disk later on.
								if (te.hasWorld() && te.getWorld() instanceof WorldServer) {
									((WorldServer) te.getWorld()).getPlayerChunkMap().markBlockForUpdate(packet.pos);// Broadcast changes.
								}
								NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Tile Entity at " + packet.pos.getX() + ", " + packet.pos.getY() + ", " + packet.pos.getZ());
								NBTEdit.logTag(packet.tag);
								NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
							} catch (Throwable t) {
								NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Tile Entity", TextFormatting.RED);
								NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
								NBTEdit.logTag(packet.tag);
								NBTEdit.throwing("TileNBTPacket", "Handler.onMessage", t);
							}
						} else {
							NBTEdit.log(Level.WARN, player.getName() + " tried to edit a non-existent TileEntity at " + packet.pos.getX() + ", " + packet.pos.getY() + ", " + packet.pos.getZ());
							NBTEdit.proxy.sendMessage(player, "cSave Failed - There is no TileEntity at " + packet.pos.getX() + ", " + packet.pos.getY() + ", " + packet.pos.getZ(), TextFormatting.RED);
						}
					}
				});
			} else {
				NBTEdit.proxy.openEditGUI(packet.pos, packet.tag);
			}
			return null;
		}
	}
}

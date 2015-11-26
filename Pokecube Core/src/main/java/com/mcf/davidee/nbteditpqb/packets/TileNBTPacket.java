package com.mcf.davidee.nbteditpqb.packets;

import static com.mcf.davidee.nbteditpqb.NBTEdit.SECTION_SIGN;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import com.mcf.davidee.nbteditpqb.NBTEdit;
import com.mcf.davidee.nbteditpqb.NBTHelper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

public class TileNBTPacket extends AbstractPacket {
	
	protected BlockPos pos;
	protected NBTTagCompound tag;
	
	public TileNBTPacket() {
		
	}
	
	public TileNBTPacket(BlockPos pos, NBTTagCompound tag) {
		this.pos = pos;
		this.tag = tag;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
		bos.writeInt(pos.getX());
		bos.writeInt(pos.getY());
		bos.writeInt(pos.getZ());
		NBTHelper.nbtWrite(tag, bos);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufInputStream bis = new ByteBufInputStream(buffer);
		pos = new BlockPos(bis.readInt(), bis.readInt(), bis.readInt());
		DataInputStream dis = new DataInputStream(bis);
		tag = NBTHelper.nbtRead(dis);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		NBTEdit.proxy.openEditGUI(pos, tag);
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		TileEntity te = player.worldObj.getTileEntity(pos);
		if (te != null) {
			try {
				te.readFromNBT(tag);
				NBTEdit.DISPATCHER.sendToDimension(new TileNBTUpdatePacket(pos, tag), player.dimension); //Broadcast changes
				NBTEdit.log(Level.FINE, player.getCommandSenderName() + " edited a tag -- Tile Entity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
				NBTEdit.logTag(tag);
				sendMessageToPlayer(player, "Your changes have been saved");
			}
			catch(Throwable t) {
				sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - Invalid NBT format for Tile Entity");
				NBTEdit.log(Level.WARNING, player.getCommandSenderName() + " edited a tag and caused an exception");
				NBTEdit.logTag(tag);
				NBTEdit.throwing("TileNBTPacket", "handleServerSide", t);
			}
		}
		else {
			NBTEdit.log(Level.WARNING, player.getCommandSenderName() + " tried to edit a non-existant TileEntity at "+pos.getX()+","+pos.getY()+","+pos.getZ());
			sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - There is no TileEntity at "+pos.getX()+","+pos.getY()+","+pos.getZ());
		}
	}

}

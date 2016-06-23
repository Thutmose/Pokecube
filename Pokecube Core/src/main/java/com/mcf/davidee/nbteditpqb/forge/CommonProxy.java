package com.mcf.davidee.nbteditpqb.forge;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class CommonProxy {
	public void registerInformation(){
		
	}

	public File getMinecraftDirectory(){
		return new File(".");
	}
	
	public void openEditGUI(int entityID, NBTTagCompound tag) {
		
	}
	
	public void openEditGUI(BlockPos pos, NBTTagCompound tag) {
		
	}
}

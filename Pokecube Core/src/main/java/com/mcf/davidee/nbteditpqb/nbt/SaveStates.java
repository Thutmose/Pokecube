package com.mcf.davidee.nbteditpqb.nbt;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import com.mcf.davidee.nbteditpqb.NBTEdit;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

// This save format can definitely be improved. Also, this can be extended to provide infinite save slots - just
// need to add some scrollbar (use GuiLib!).
public class SaveStates {

	private File file;
	private SaveState[] tags;

	public SaveStates(File file){
		this.file = file;
		tags = new SaveState[7];
		for (int i =0; i < 7; ++i)
			tags[i] = new SaveState("Slot " + (i+1));
	}
	
	public void read() throws IOException{
		if (file.exists() && file.canRead()){
			NBTTagCompound root = CompressedStreamTools.read(file);
			for (int i =0; i < 7; ++i){
				String name = "slot" + (i+1);
				if (root.hasKey(name))
					tags[i].tag = root.getCompoundTag(name);
				if (root.hasKey(name+"Name"))
					tags[i].name = root.getString(name+"Name");
			}
		}
	}
	
	public void write() throws IOException{
		NBTTagCompound root = new NBTTagCompound();
		for (int i = 0; i <7; ++i){
			root.setTag("slot" + (i+1), tags[i].tag);
			root.setString("slot" + (i+1)+"Name", tags[i].name);
		}
		CompressedStreamTools.write(root, file);
	}
	
	public void save(){
		try {
			write();
			NBTEdit.log(Level.TRACE,"NBTEdit saved successfully.");
		}
		catch(IOException e){
			NBTEdit.log(Level.WARN, "Unable to write NBTEdit save.");
			NBTEdit.throwing("SaveStates", "save", e);
		}
	}
	
	public void load(){
		try {
			read();
			NBTEdit.log(Level.TRACE,"NBTEdit save loaded successfully.");
		}
		catch(IOException e){
			NBTEdit.log(Level.WARN, "Unable to read NBTEdit save.");
			NBTEdit.throwing("SaveStates", "load", e);
		}
	}
	
	public SaveState getSaveState(int index){
		return tags[index];
	}

	public static final class SaveState{
		public String name;
		public NBTTagCompound tag;

		public SaveState(String name){
			this.name = name;
			this.tag = new NBTTagCompound();
		}
	}
}

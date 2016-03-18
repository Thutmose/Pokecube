package com.mcf.davidee.nbteditpqb.nbt;

import net.minecraft.nbt.NBTBase;

public class NamedNBT {
	
	protected String name;
	protected NBTBase nbt;
	
	public NamedNBT(NBTBase nbt) {
		this("", nbt);
	}
	
	public NamedNBT(String name, NBTBase nbt) {
		this.name = name;
		this.nbt = nbt;
	}
	
	public NamedNBT copy() {
		return new NamedNBT(name, nbt.copy());
	}
	
	public String getName() {
		return name;
	}
	
	public NBTBase getNBT() {
		return nbt;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNBT(NBTBase nbt) {
		this.nbt = nbt;
	}
	
}

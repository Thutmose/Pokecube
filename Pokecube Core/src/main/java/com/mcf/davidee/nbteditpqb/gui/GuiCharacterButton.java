package com.mcf.davidee.nbteditpqb.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class GuiCharacterButton extends Gui{


	public static final int WIDTH = 14, HEIGHT = 14;

	private Minecraft mc = Minecraft.getMinecraft();
	private byte id;
	private int x, y;
	private boolean enabled;


	public GuiCharacterButton(byte id, int x, int y){
		this.id = id;
		this.x = x; 
		this.y = y;
	}
	public void draw(int mx, int my){
		mc.renderEngine.bindTexture(GuiNBTNode.WIDGET_TEXTURE);
		if(inBounds(mx,my))
			Gui.drawRect(x, y, x+WIDTH, y+HEIGHT, 0x80ffffff);

		if (enabled) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		} else GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

		drawTexturedModalRect(x, y, id * WIDTH, 27, WIDTH, HEIGHT);
	}
	
	public void setEnabled(boolean aFlag){
		enabled = aFlag;
	}
	
	public boolean inBounds(int mx, int my){
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}
	
	public byte getId(){
		return id;
	}
}
 
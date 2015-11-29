package pokecube.adventures.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.network.PokecubePacketHandler;

public class GUIBiomeSetter extends GuiScreen {
    ItemStack setter;
    GuiTextField textField0;
	
    public GUIBiomeSetter(ItemStack item) {
    	setter = item;
	}
    
	@Override
    public void initGui()
    {
    	super.initGui();
        buttonList.clear();
        
        int yOffset = -20;
        String biome = "";
        if(setter!=null && setter.hasTagCompound())
        {
        	biome = setter.getTagCompound().getString("biome");
        }
        
        textField0 = new GuiTextField(0, fontRendererObj, width / 2 - 50, height / 4 + 20 + yOffset, 100, 10);
        textField0.setText(biome);
    }
	
    
    @Override
    public void drawScreen(int i, int j, float f)
    {
    	super.drawScreen(i, j, f);
    	textField0.drawTextBox();
    	fontRendererObj.drawString("Sub-Biome to set",  width / 2 - fontRendererObj.getStringWidth("Sub-Biome to set") / 3 - 20, height / 4 - 15, 4210752);
    }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        textField0.mouseClicked(par1, par2, par3);
    }
    
    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        if (par1 == 13)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }

        textField0.textboxKeyTyped(par1, par2);
    }
    
    @Override
    public void onGuiClosed() {
    	sendChooseToServer();
    	
    	super.onGuiClosed();
    }
    
    private void sendChooseToServer()
    {
    	NBTTagCompound tag = new NBTTagCompound();
    	tag.setString("biome", textField0.getText());
    	MessageServer mess = new MessageServer((byte) 9, tag);
    	PokecubePacketHandler.sendToServer(mess);
    }
}

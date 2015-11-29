package pokecube.adventures.client.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.maths.Vector3;

public class GuiBag extends GuiContainer {

	String page;
	ContainerBag cont;
	GuiTextField textFieldSelectedBox;
	GuiTextField textFieldBoxName;
	GuiTextField textFieldSearch;

	private String boxName = "1";
	private boolean toRename = false;
	public boolean pc = false;
	Vector3 loc;
	
    public GuiBag (ContainerBag cont, Vector3 pcLoc) {
            super(cont);
            this.cont = cont;
            this.xSize = 175;
            this.ySize = 229;
            page = cont.getPageNb();
            boxName = cont.getPage();
            pc = !pcLoc.isEmpty();
            loc = pcLoc;
            //release = cont.release;
    }
    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = 0;
        int yOffset = -11;
        String next = StatCollector.translateToLocal("tile.pc.next");
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 15, height / 2 - yOffset, 50, 20, next));
        String prev = StatCollector.translateToLocal("tile.pc.previous");
        buttonList.add(new GuiButton(2, width / 2 - xOffset-65, height / 2 - yOffset, 50, 20, prev));

        String rename = StatCollector.translateToLocal("tile.pc.rename");
        buttonList.add(new GuiButton(3, width / 2 - xOffset-137, height / 2 - yOffset - 125, 50, 20, rename));
        if(pc)
        {
        	String bag = "PC";//StatCollector.translateToLocal("tile.pc.rename");
        	buttonList.add(new GuiButton(4, width / 2 - xOffset-137, height / 2 - yOffset - 105, 50, 20, bag));
        }
        
        textFieldSelectedBox  = new GuiTextField(0, fontRendererObj, width / 2 - xOffset - 13, height / 2 - yOffset + 5, 25, 10);
        textFieldSelectedBox.setText(page);

        textFieldBoxName  = new GuiTextField(0, fontRendererObj, width / 2 - xOffset - 190, height / 2 - yOffset - 80, 100, 10);
        textFieldBoxName.setText(boxName);
        
        textFieldSearch  = new GuiTextField(0, fontRendererObj, width / 2 - xOffset - 10, height / 2 - yOffset - 121, 90, 10);
        textFieldSearch.setText("");
    }
	
    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if(mc.thePlayer.worldObj.isRemote)
        {
        	byte[] message= {(byte)guibutton.id};
        	
    		MessageServer packet = PacketPokeAdv.makeServerPacket((byte) 6, message);
            PokecubePacketHandler.sendToServer(packet);

        	if(guibutton.id == 3)
        	{
        		if(toRename)
        		{
                	String box = textFieldBoxName.getText();
        			if(box!=boxName)
        				cont.changeName(box);
        		}
        		toRename = !toRename;
        	} 
        	if(guibutton.id == 4)
        	{
				PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
				buf.writeByte(7);
				buf.writeBoolean(false);
				loc.writeToBuff(buf);
				packet = new MessageServer(buf);
				PokecubePacketHandler.sendToServer(packet);
        	} 
        	else
        	{
        		cont.updateInventoryPages((byte) (guibutton.id==2?-1:guibutton.id==1?1:0),mc.thePlayer.inventory);

        		textFieldSelectedBox.setText(cont.getPageNb());
        	}
        }
        }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        textFieldSelectedBox.mouseClicked(par1, par2, par3);
        textFieldSearch.mouseClicked(par1, par2, par3);
        if(toRename)
        	textFieldBoxName.mouseClicked(par1, par2, par3);
        
    }
    
    @Override
    protected void keyTyped(char par1, int par2)
    {
        keyTyped2(par1, par2);
    	if(par2==1)
    	{
    		mc.thePlayer.closeScreen();
    		return;
    	}
    	
    	textFieldSearch.textboxKeyTyped(par1, par2);
    	
    	if(toRename)
    		textFieldBoxName.textboxKeyTyped(par1, par2);
        if (par1 <=57)
        {
        	textFieldSelectedBox.textboxKeyTyped(par1, par2);
        }
        if(par2 == 28)
        {
        	String entry = textFieldSelectedBox.getText();
        	String box = textFieldBoxName.getText();
        	int number = 1;

			try {
				number = Integer.parseInt(entry);
			}
			catch(Exception e){e.printStackTrace();}
			
			number = Math.max(1, Math.min(number, InventoryBag.PAGECOUNT));
			cont.gotoInventoryPage(number);
			
			if(toRename&&box!=boxName)
			{
				
        		if(toRename)
        		{
                	box = textFieldBoxName.getText();
        			if(box!=boxName)
        				cont.changeName(box);
        		}
        		toRename = !toRename;
			}
        }
        
    }
    
    @Override
    public void drawScreen(int i1, int j, float f)
    {
		super.drawScreen(i1, j, f);
		textFieldSelectedBox.drawTextBox();
		
    	textFieldSearch.drawTextBox();
		
		if(toRename)
			textFieldBoxName.drawTextBox();
		for(int i = 0; i<54; i++)
		if(!textFieldSearch.getText().isEmpty())
		{
			ItemStack stack = cont.inv.getStackInSlot(i);
			//if(stack!=null)
			int x = (i % 9) * 18 + width/2 - 80;
			int y = (i / 9) * 18 + height/2 - 96;
			{
				String name = stack==null?"":stack.getDisplayName();
				if(name.isEmpty()||!name.toLowerCase().contains(textFieldSearch.getText()))
				{
			    	GL11.glPushMatrix();
			        GL11.glEnable(GL11.GL_BLEND);
			        GL11.glColor4f(0, 0, 0, 1);
					mc.renderEngine.bindTexture(new ResourceLocation(PokecubeAdv.ID, "textures/hologram.png"));
					drawTexturedModalRect(x, y, 0, 0, 16, 16);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glPopMatrix();
				}
				else
				{
			    	GL11.glPushMatrix();
			        GL11.glEnable(GL11.GL_BLEND);
			        GL11.glColor4f(0, 1, 0, 1);
					mc.renderEngine.bindTexture(new ResourceLocation(PokecubeAdv.ID, "textures/hologram.png"));
					drawTexturedModalRect(x, y, 0, 0, 16, 16);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glPopMatrix();
				}
			}
		}
		
    }
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) 
	{
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		mc.renderEngine.bindTexture(new ResourceLocation(PokecubeAdv.ID, "textures/gui/pcGui.png"));
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		GL11.glPushMatrix();
		GL11.glScaled(0.8, 0.8, 0.8);
		String pcTitle = cont.invPlayer.player.getCommandSenderName()+"'s Bag";
    	fontRendererObj.drawString(cont.getPage(), xSize / 2 - fontRendererObj.getStringWidth(cont.getPage()) / 3 - 60, 13, 4210752);
    	fontRendererObj.drawString(pcTitle,  xSize / 2 - fontRendererObj.getStringWidth(pcTitle) / 3 - 60, 4, 4210752);
    	GL11.glPopMatrix();
	}

	//public boolean getReleaseState(){
		
		///return release;
	//}
	
	private Slot theSlot;
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped2(char par1, int par2)
    {
        this.checkHotbarKeys(par2);

        if (this.theSlot != null && this.theSlot.getHasStack())
        {
            if (par2 == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, 3);
            }
            else if (par2 == this.mc.gameSettings.keyBindDrop.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
	public void onGuiClosed()
    {
        if (this.mc.thePlayer != null)
        {
            this.inventorySlots.onContainerClosed(this.mc.thePlayer);
        }
    }
	

}

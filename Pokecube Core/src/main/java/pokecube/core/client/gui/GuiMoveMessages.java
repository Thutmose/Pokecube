package pokecube.core.client.gui;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiMoveMessages
{
	private static GuiMoveMessages instance;

	private ArrayList<String> messages = new ArrayList<String>();
	private ArrayList<String> recent = new ArrayList<String>();
	int time = 0;
	int offset = 0;
	public GuiMoveMessages()
	{
		MinecraftForge.EVENT_BUS.register(this);
		instance = this;
	}
	
	public static void clear()
	{
		if(instance!=null)
		{
			instance.messages.clear();
			instance.recent.clear();
		}
	}
	
    @SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) 
    {
		draw(event);
		time--;
    }
    
    @SubscribeEvent
    public void PlayerLoggout(PlayerLoggedOutEvent evt)
    {
    	if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
    	{
    		messages.clear();
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void draw(RenderWorldLastEvent event)
    {
    	Minecraft minecraft = Minecraft.getMinecraft();
    	int i = Mouse.getDWheel();
        
        
        int n = (minecraft.displayWidth);
        int m = (minecraft.displayHeight);

        if(n>1300&&m>900)
        {
        	m = m/4;
        	n = n/4;
        }
        if(n>1280&&m>960)
        {
        	m = m/2;
        	n = n/3;
        }
        if(n>960&&m>720)
        {
        	m = (int) (m/1.5);
        	n = n/3;
        }
        else if(n>600&&m>480)
        {
        	m = m/1;
        	n = n/2;
        }
        else
        {
        	m = (int) (m/0.5);
        }
        m = m/2;
        
        int x = n, y=m / 2;
        GL11.glPushMatrix();
     	GL11.glDisable(GL11.GL_DEPTH_TEST);
     	GL11.glDepthMask(false);
     	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     	GL11.glEnable(GL11.GL_BLEND);
     	RenderHelper.disableStandardItemLighting();
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
     	minecraft.entityRenderer.setupOverlayRendering();
     	
     	int num = -1;
     	if (Minecraft.getMinecraft().currentScreen instanceof GuiChat)
     	{
     		num = 7;
         	offset += (int) (i!=0?Math.signum(i):0);
     	}
     	else if(time > 0)
     	{
     		num = 6;
     		offset = 0;
     	}
     	else
     	{
     		offset = 0;
     		if(recent.size()>0)
     		{
     			recent.remove(0);
     		}
     	}

     	int size = (num==7?messages.size():recent.size()) - 1;
     	
     	for(int l = 0; l<num; l++)
     	{
     		if(size-l<0)
     			break;
     		int index = (size-l + offset);
     		if(index<0)
     			index = size - l;
     		if(index>size)
     			index = l;
     		String mess = num==7?messages.get(index):recent.get(index);
        	minecraft.fontRendererObj.drawString(mess, x - minecraft.fontRendererObj.getStringWidth(mess), y - minecraft.fontRendererObj.FONT_HEIGHT * l, 255*256*256 + 255 + 255*256, true);
     	}

     	GL11.glEnable(GL11.GL_DEPTH_TEST);
     	GL11.glDepthMask(true);
     	GL11.glDisable(GL11.GL_BLEND);
     	RenderHelper.enableStandardItemLighting();
     	GL11.glPopMatrix();
    }
    
    public static void addMessage(String message)
    {
    	instance.messages.add(message);
    	instance.time = 100;
    	instance.recent.add(message);
    	if(instance.messages.size()>100)
    	{
    		instance.messages.remove(0);
    	}
    }
}

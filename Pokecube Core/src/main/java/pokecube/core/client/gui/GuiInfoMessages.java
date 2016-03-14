package pokecube.core.client.gui;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;

public class GuiInfoMessages
{
    private static GuiInfoMessages instance;

    public static void addMessage(String message)
    {
        instance.messages.add(message);
        instance.time = Minecraft.getMinecraft().thePlayer.ticksExisted;
        instance.recent.add(message);
        if (instance.messages.size() > 100)
        {
            instance.messages.remove(0);
        }
    }

    public static void clear()
    {
        if (instance != null)
        {
            instance.messages.clear();
            instance.recent.clear();
        }
    }

    private ArrayList<String> messages = new ArrayList<String>();
    private ArrayList<String> recent   = new ArrayList<String>();
    long                      time     = 0;

    int                       offset   = 0;

    public GuiInfoMessages()
    {
        MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    @SideOnly(Side.CLIENT)
    public void draw(RenderGameOverlayEvent.Post event)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.type == ElementType.CHAT && !(minecraft.currentScreen instanceof GuiChat)) return;

        int i = Mouse.getDWheel();
        int w = (event.resolution.getScaledWidth() - 1);
        int h = (event.resolution.getScaledHeight());

        int n = w;
        int m = h;
        int x = n, y = m / 2;
        GL11.glPushMatrix();
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.entityRenderer.setupOverlayRendering();

        int num = -1;
        if (event.type == ElementType.CHAT)
        {
            num = 7;
            offset += (int) (i != 0 ? Math.signum(i) : 0);
        }
        else if (time > minecraft.thePlayer.ticksExisted - 30)
        {
            num = 6;
            offset = 0;
        }
        else
        {
            offset = 0;
            time = minecraft.thePlayer.ticksExisted;
            if (recent.size() > 0)
            {
                recent.remove(0);
            }
        }

        int size = (num == 7 ? messages.size() : recent.size()) - 1;

        for (int l = 0; l < num; l++)
        {
            if (size - l < 0) break;
            int index = (size - l + offset);
            if (index < 0) index = size - l;
            if (index > size) index = l;
            String mess = num == 7 ? messages.get(index) : recent.get(index);
            minecraft.fontRendererObj.drawString(mess, x - minecraft.fontRendererObj.getStringWidth(mess),
                    y - minecraft.fontRendererObj.FONT_HEIGHT * l, 255 * 256 * 256 + 255 + 255 * 256, true);
        }
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if (!((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                    && (event.type == ElementType.HOTBAR || event.type == ElementType.CHAT))
            {
                draw(event);
            }
        }
        catch (Exception e)
        {

        }
    }
}

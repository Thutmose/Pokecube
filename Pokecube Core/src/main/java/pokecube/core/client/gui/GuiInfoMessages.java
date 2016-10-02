package pokecube.core.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class GuiInfoMessages
{
    private static GuiInfoMessages instance;

    public static void addMessage(ITextComponent message)
    {
        instance.messages.add(message.getFormattedText());
        instance.time = Minecraft.getMinecraft().thePlayer.ticksExisted;
        instance.recent.add(message.getFormattedText());
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
        if (event.getType() == ElementType.CHAT && !(minecraft.currentScreen instanceof GuiChat)) return;
        if (event.getType() != ElementType.CHAT && (minecraft.currentScreen instanceof GuiChat)) return;
        int i = Mouse.getDWheel();
        int texH = minecraft.fontRendererObj.FONT_HEIGHT;
        int trim = PokecubeCore.core.getConfig().messageWidth;
        GL11.glPushMatrix();
        minecraft.entityRenderer.setupOverlayRendering();
        int w = PokecubeMod.core.getConfig().messageOffset[0];
        int h = PokecubeMod.core.getConfig().messageOffset[1];
        int scaledWidth = Minecraft.getMinecraft().displayWidth;
        int scaledHeight = Minecraft.getMinecraft().displayHeight;
        float scaleFactor = GuiDisplayPokecubeInfo.scale(PokecubeMod.core.getConfig().messageSize, true);
        scaledWidth /= scaleFactor;
        scaledHeight /= scaleFactor;
        w = Math.min(scaledWidth, w);
        h = Math.min(scaledHeight - texH, h);
        w = Math.max(trim, w);
        h = Math.max(texH * 7, h);
        int x = w, y = h;
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int num = -1;
        if (event.getType() == ElementType.CHAT)
        {
            num = 7;
            offset += (int) (i != 0 ? Math.signum(i) : 0);
            if (offset < 0) offset = 0;
            if (offset > messages.size() - 7) offset = messages.size() - 7;
        }
        else if (time > minecraft.thePlayer.ticksExisted - 30)
        {
            num = 6;
            offset = 0;
        }
        else
        {
            offset = 0;
            num = 6;
            time = minecraft.thePlayer.ticksExisted;
            if (recent.size() > 0)
            {
                recent.remove(0);
            }
        }
        while (recent.size() > 8)
            recent.remove(0);
        List<String> toUse = num == 7 ? messages : recent;
        int size = toUse.size() - 1;
        num = Math.min(num, size + 1);
        for (int l = 0; l < num; l++)
        {
            int index = (l + offset);
            if (index < 0) index = 0;
            if (index > size) break;
            String mess = toUse.get(index);
            List<String> mess1 = minecraft.fontRendererObj.listFormattedStringToWidth(mess, trim);
            for (int j = mess1.size() - 1; j >= 0; j--)
            {
                h = y - texH * l;
                w = x - trim;
                GuiScreen.drawRect(w, h, w + trim, h + texH, 0x66000000);
                minecraft.fontRendererObj.drawString(mess1.get(j), x - trim, h, 0xffffff, true);
                if (j > 0) l++;
            }
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
                    && (event.getType() == ElementType.HOTBAR || event.getType() == ElementType.CHAT))
            {
                draw(event);
            }
        }
        catch (Exception e)
        {

        }
    }
}

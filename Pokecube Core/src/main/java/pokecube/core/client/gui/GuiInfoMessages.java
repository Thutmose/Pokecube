package pokecube.core.client.gui;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
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
        instance.messages.push(message.getFormattedText());
        instance.time = Minecraft.getMinecraft().thePlayer.ticksExisted;
        instance.recent.addFirst(message.getFormattedText());
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

    private LinkedList<String> messages = Lists.newLinkedList();
    private LinkedList<String> recent   = Lists.newLinkedList();
    long                       time     = 0;

    int                        offset   = 0;

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

        int texH = minecraft.fontRendererObj.FONT_HEIGHT;
        int trim = PokecubeCore.core.getConfig().messageWidth;
        int paddingXPos = PokecubeCore.core.getConfig().messagePadding[0];
        int paddingXNeg = PokecubeCore.core.getConfig().messagePadding[1];
        GL11.glPushMatrix();
        int[] messSize = new int[] { trim + paddingXPos + paddingXNeg, 7 * texH };
        minecraft.entityRenderer.setupOverlayRendering();
        int[] messArr = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().messageRef,
                PokecubeMod.core.getConfig().messagePos, messSize, PokecubeMod.core.getConfig().messageSize);
        int x = 0, y = 0;
        float s = PokecubeMod.core.getConfig().messageSize;
        x += messArr[2];
        y += messArr[3];
        Rectangle messRect = new Rectangle((int) (x * s), (int) (y * s), (int) (messArr[0] * s),
                (int) (messArr[1] * s));

        int i1 = ((Mouse.getX()));
        int j1 = ((minecraft.displayHeight - Mouse.getY())) - 1;
        i1 = i1 - messArr[0];
        j1 = j1 - messArr[1];
        int i = Mouse.getDWheel();
        if (!messRect.contains(i1, j1))
        {
            i = 0;
        }

        int w = 0;
        int h = 0;
        x = w;
        y = h;
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0, -texH * 7, 0);
        GlStateManager.translate(0, 0, 0);
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
            if (!recent.isEmpty())
            {
                recent.removeLast();
            }
        }
        while (recent.size() > 8)
            recent.removeLast();
        List<String> toUse = num == 7 ? messages : recent;
        int size = toUse.size() - 1;
        num = Math.min(num, size + 1);
        int shift = 0;
        for (int l = 0; l < num && shift < num; l++)
        {
            int index = (l + offset);
            if (index < 0) index = 0;
            if (index > size) break;
            String mess = toUse.get(index);
            List<String> mess1 = minecraft.fontRendererObj.listFormattedStringToWidth(mess, trim);
            for (int j = 0; j < mess1.size(); j++)
            {
                h = y + texH * (shift + j);
                w = x - trim;
                GuiScreen.drawRect(w - paddingXNeg, h, w + trim + paddingXPos, h + texH, 0x66000000);
                minecraft.fontRendererObj.drawString(mess1.get(j), x - trim, h, 0xffffff, true);
                if (j != 0) shift++;
            }
            shift++;
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

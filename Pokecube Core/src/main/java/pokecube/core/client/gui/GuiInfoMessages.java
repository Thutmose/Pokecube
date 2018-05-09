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
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent.RenderMoveMessages;
import pokecube.core.interfaces.PokecubeMod;

public class GuiInfoMessages
{
    public static GuiInfoMessages instance;

    public static void addMessage(ITextComponent message)
    {
        if (PokecubeCore.core.getConfig().battleLogInChat)
        {
            if (PokecubeCore.getPlayer(null) != null) PokecubeCore.getPlayer(null).sendMessage(message);
            return;
        }
        instance.messages.push(message.getFormattedText());
        instance.time = Minecraft.getMinecraft().player.ticksExisted;
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
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void draw(RenderMoveMessages event)
    {
        if (PokecubeCore.core.getConfig().battleLogInChat) { return; }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getType() == ElementType.CHAT && !(minecraft.currentScreen instanceof GuiChat)) return;
        if (event.getType() != ElementType.CHAT && (minecraft.currentScreen instanceof GuiChat)) return;

        int texH = minecraft.fontRenderer.FONT_HEIGHT;
        int trim = PokecubeCore.core.getConfig().messageWidth;
        int paddingXPos = PokecubeCore.core.getConfig().messagePadding[0];
        int paddingXNeg = PokecubeCore.core.getConfig().messagePadding[1];
        GL11.glPushMatrix();

        minecraft.entityRenderer.setupOverlayRendering();
        int[] mess = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().messageRef,
                PokecubeMod.core.getConfig().messagePos,
                new int[] { PokecubeMod.core.getConfig().messageWidth, 7 * minecraft.fontRenderer.FONT_HEIGHT },
                PokecubeMod.core.getConfig().messageSize);
        int x = 0, y = 0;
        float s = PokecubeMod.core.getConfig().messageSize;
        x = x - 150;
        Rectangle messRect = new Rectangle(x, y - 7 * texH, 150, 8 * texH);
        x += mess[2];
        y += mess[3];
        messRect.setBounds((int) (x * s), (int) ((y - 7 * texH) * s), (int) (150 * s), (int) (8 * texH * s));

        int i1 = -10;
        int j1 = -10;

        if (minecraft.currentScreen != null)
        {
            i1 = ((Mouse.getX() * minecraft.currentScreen.width / minecraft.displayWidth));
            j1 = ((minecraft.currentScreen.height
                    - Mouse.getY() * minecraft.currentScreen.height / minecraft.displayHeight)) - 1;
        }
        i1 = i1 - mess[0];
        j1 = j1 - mess[1];

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
        else if (time > minecraft.player.ticksExisted - 30)
        {
            num = 6;
            offset = 0;
        }
        else
        {
            offset = 0;
            num = 6;
            time = minecraft.player.ticksExisted;
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
            String mess2 = toUse.get(index);
            List<String> mess1 = minecraft.fontRenderer.listFormattedStringToWidth(mess2, trim);
            for (int j = mess1.size() - 1; j >= 0; j--)
            {
                h = y + texH * (shift);
                w = x - trim;
                int ph = 6 * texH - h;
                GuiScreen.drawRect(w - paddingXNeg, ph, w + trim + paddingXPos, ph + texH, 0x66000000);
                minecraft.fontRenderer.drawString(mess1.get(j), x - trim, ph, 0xffffff, true);
                if (j != 0) shift++;
            }
            shift++;
        }
        GL11.glPopMatrix();
    }
}

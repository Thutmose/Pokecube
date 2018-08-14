package pokecube.core.client.gui;

import java.awt.Rectangle;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class GuiArranger
{
    public static boolean toggle = false;
    boolean[]             held   = new boolean[4];

    public GuiArranger()
    {
    }

    private void drawBox(Rectangle rect, float s, int startColor, int endColor)
    {
        int top = (int) rect.getMinY();
        int bottom = (int) rect.getMaxY();
        double right = rect.getMaxX();
        double left = rect.getMinX();
        double zLevel = 0;
        float f = (startColor >> 24 & 255) / 255.0F;
        float f1 = (startColor >> 16 & 255) / 255.0F;
        float f2 = (startColor >> 8 & 255) / 255.0F;
        float f3 = (startColor & 255) / 255.0F;
        float f4 = (endColor >> 24 & 255) / 255.0F;
        float f5 = (endColor >> 16 & 255) / 255.0F;
        float f6 = (endColor >> 8 & 255) / 255.0F;
        float f7 = (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiEvent(GuiScreenEvent event)
    {
        if (toggle && event.getGui() instanceof GuiChat)
        {
            GuiScreen gui = event.getGui();
            if (event instanceof DrawScreenEvent.Post)
            {
                // Draw the box for the messages.
                GL11.glPushMatrix();
                Minecraft minecraft = Minecraft.getMinecraft();
                int texH = minecraft.fontRenderer.FONT_HEIGHT;
                minecraft.entityRenderer.setupOverlayRendering();
                int[] mess = GuiDisplayPokecubeInfo.applyTransform(
                        PokecubeCore.core.getConfig().messageRef, PokecubeMod.core.getConfig().messagePos, new int[] {
                                PokecubeMod.core.getConfig().messageWidth, 7 * minecraft.fontRenderer.FONT_HEIGHT },
                        PokecubeMod.core.getConfig().messageSize);
                int x = 0, y = 0;
                x = x - 150;
                Rectangle messRect = new Rectangle(x, y - 7 * texH, 150, 8 * texH);
                int startColor = 0x22FFFF00;
                int endColor = 0x22FFFF00;
                float s = PokecubeMod.core.getConfig().messageSize;
                drawBox(messRect, s, startColor, endColor);
                x += mess[2];
                y += mess[3];
                messRect.setBounds((int) (x * s), (int) ((y - 7 * texH) * s), (int) (150 * s), (int) (8 * texH * s));
                GL11.glPopMatrix();

                // Draw a box for the Info
                GL11.glPushMatrix();
                int[] guiS = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().guiRef,
                        PokecubeMod.core.getConfig().guiPos, GuiDisplayPokecubeInfo.guiDims,
                        PokecubeMod.core.getConfig().guiSize);
                x = 0;
                y = 0;
                startColor = 0x2200ff00;
                endColor = 0x2200ff00;
                Rectangle guiRect = new Rectangle(x, y, GuiDisplayPokecubeInfo.guiDims[0],
                        GuiDisplayPokecubeInfo.guiDims[1]);
                s = PokecubeMod.core.getConfig().guiSize;
                drawBox(guiRect, s, startColor, endColor);
                x += guiS[2];
                y += guiS[3];
                guiRect.setBounds((int) (x * s), (int) (y * s), (int) (GuiDisplayPokecubeInfo.guiDims[0] * s),
                        (int) (GuiDisplayPokecubeInfo.guiDims[1] * s));
                GL11.glPopMatrix();

                // Draw a box for the Target
                GL11.glPushMatrix();
                int[] targ = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().targetRef,
                        PokecubeMod.core.getConfig().targetPos, GuiDisplayPokecubeInfo.targetDims,
                        PokecubeMod.core.getConfig().targetSize);
                x = 0;
                y = 0;
                startColor = 0x22FF0000;
                endColor = 0x22FF0000;
                Rectangle targRect = new Rectangle(x, y, GuiDisplayPokecubeInfo.targetDims[0],
                        GuiDisplayPokecubeInfo.targetDims[1]);
                s = PokecubeMod.core.getConfig().targetSize;
                drawBox(targRect, s, startColor, endColor);
                x += targ[2];
                y += targ[3];
                targRect.setBounds((int) (x * s), (int) (y * s), (int) (GuiDisplayPokecubeInfo.targetDims[0] * s),
                        (int) (GuiDisplayPokecubeInfo.targetDims[1] * s));
                GL11.glPopMatrix();

                // Draw a box for Teleports
                GL11.glPushMatrix();
                int[] teles = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().teleRef,
                        PokecubeMod.core.getConfig().telePos, GuiDisplayPokecubeInfo.teleDims,
                        PokecubeMod.core.getConfig().teleSize);
                x = 0;
                y = 0;
                startColor = 0x22FFFF00;
                endColor = 0x22FFFF00;
                Rectangle teleRect = new Rectangle(x, y, GuiDisplayPokecubeInfo.teleDims[0],
                        GuiDisplayPokecubeInfo.teleDims[1]);
                s = PokecubeMod.core.getConfig().teleSize;
                drawBox(teleRect, s, startColor, endColor);
                x += teles[2];
                y += teles[3];
                teleRect.setBounds((int) (x * s), (int) (y * s), (int) (GuiDisplayPokecubeInfo.teleDims[0] * s),
                        (int) (GuiDisplayPokecubeInfo.teleDims[1] * s));
                GL11.glPopMatrix();

                int i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                int j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                int k = 0;
                boolean isHeld = false;
                int index = -1;
                for (int n = 0; n < held.length; n++)
                {
                    if (held[n])
                    {
                        isHeld = true;
                        index = n;
                        break;
                    }
                }
                if (isHeld)
                {
                    held[index] = false;
                    switch (index)
                    {
                    case 0:
                        if (Mouse.isButtonDown(k))
                        {
                            i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                            j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                            i = i - mess[0];
                            j = j - mess[1];
                        }
                        if (messRect.contains(i, j))
                        {
                            held[index] = Mouse.isButtonDown(k);
                        }
                        if (held[index])
                        {
                            int mx = (int) messRect.getCenterX();
                            int my = (int) messRect.getCenterY();
                            int dx = mx - i;
                            int dy = my - j;
                            PokecubeMod.core.getConfig().messagePos[0] -= dx * mess[4];
                            PokecubeMod.core.getConfig().messagePos[1] -= dy * mess[5];
                        }
                        break;
                    case 1:
                        if (Mouse.isButtonDown(k))
                        {
                            i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                            j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                            i = i - guiS[0];
                            j = j - guiS[1];
                        }
                        if (guiRect.contains(i, j))
                        {
                            held[index] = Mouse.isButtonDown(k);
                        }
                        if (held[index])
                        {
                            int mx = (int) guiRect.getCenterX();
                            int my = (int) guiRect.getCenterY();
                            int dx = mx - i;
                            int dy = my - j;
                            PokecubeMod.core.getConfig().guiPos[0] -= dx * guiS[4];
                            PokecubeMod.core.getConfig().guiPos[1] -= dy * guiS[5];
                        }
                        break;
                    case 2:
                        if (Mouse.isButtonDown(k))
                        {
                            i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                            j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                            i = i - targ[0];
                            j = j - targ[1];
                        }
                        if (targRect.contains(i, j))
                        {
                            held[index] = Mouse.isButtonDown(k);
                        }
                        if (held[index])
                        {
                            int mx = (int) targRect.getCenterX();
                            int my = (int) targRect.getCenterY();
                            int dx = mx - i;
                            int dy = my - j;
                            PokecubeMod.core.getConfig().targetPos[0] -= dx * targ[4];
                            PokecubeMod.core.getConfig().targetPos[1] -= dy * targ[5];
                        }
                        break;
                    case 3:
                        if (Mouse.isButtonDown(k))
                        {
                            i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                            j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                            i = i - teles[0];
                            j = j - teles[1];
                        }
                        if (teleRect.contains(i, j))
                        {
                            held[index] = Mouse.isButtonDown(k);
                        }
                        if (held[index])
                        {
                            int mx = (int) teleRect.getCenterX();
                            int my = (int) teleRect.getCenterY();
                            int dx = mx - i;
                            int dy = my - j;
                            PokecubeMod.core.getConfig().telePos[0] -= dx * teles[4];
                            PokecubeMod.core.getConfig().telePos[1] -= dy * teles[5];
                        }
                        break;
                    }
                }
                else
                {
                    for (index = 0; index < held.length; index++)
                    {
                        switch (index)
                        {
                        case 0:
                            if (Mouse.isButtonDown(k))
                            {
                                i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                                j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                                i = i - mess[0];
                                j = j - mess[1];
                            }
                            if (messRect.contains(i, j))
                            {
                                held[index] = Mouse.isButtonDown(k);
                            }
                            break;
                        case 1:
                            if (Mouse.isButtonDown(k))
                            {
                                i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                                j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                                i = i - guiS[0];
                                j = j - guiS[1];
                            }
                            if (guiRect.contains(i, j))
                            {
                                held[index] = Mouse.isButtonDown(k);
                            }
                            break;
                        case 2:
                            if (Mouse.isButtonDown(k))
                            {
                                i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                                j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                                i = i - targ[0];
                                j = j - targ[1];
                            }
                            if (targRect.contains(i, j))
                            {
                                held[index] = Mouse.isButtonDown(k);
                            }
                            break;
                        case 3:
                            if (Mouse.isButtonDown(k))
                            {
                                i = ((Mouse.getX() * gui.width / gui.mc.displayWidth));
                                j = ((gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight)) - 1;
                                i = i - teles[0];
                                j = j - teles[1];
                            }
                            if (teleRect.contains(i, j))
                            {
                                held[index] = Mouse.isButtonDown(k);
                            }
                            break;
                        }
                    }
                }
            }

            if (event instanceof MouseInputEvent.Post)
            {
                try
                {

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

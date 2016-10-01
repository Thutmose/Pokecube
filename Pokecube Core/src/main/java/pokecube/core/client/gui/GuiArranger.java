package pokecube.core.client.gui;

import java.awt.Rectangle;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
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
    public static boolean toggle       = false;

    boolean               guiheld      = false;
    boolean               messagesheld = false;

    public GuiArranger()
    {
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
                int texH = minecraft.fontRendererObj.FONT_HEIGHT;
                int trim = PokecubeCore.core.getConfig().messageWidth;
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
                x = x - 150;
                Rectangle messRect = new Rectangle(x, y - 7 * texH, 150, 8 * texH);
                int startColor = 0x22FFFF00;
                int endColor = 0x22FFFF00;
                int top = (int) messRect.getMinY();// y - 6 * texH;
                int bottom = (int) messRect.getMaxY();// y + texH;
                double right = messRect.getMaxX();// x + 150;
                double left = messRect.getMinX();// x;
                float s = PokecubeMod.core.getConfig().messageSize;
                messRect.setBounds((int) (x * s), (int) ((y - 7 * texH) * s), (int) (150 * s), (int) (8 * texH * s));
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
                VertexBuffer vertexbuffer = tessellator.getBuffer();
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
                GL11.glPopMatrix();

                // Draw a box for the Info
                w = PokecubeMod.core.getConfig().guiOffset[0];
                h = PokecubeMod.core.getConfig().guiOffset[1];
                scaledWidth = Minecraft.getMinecraft().displayWidth;
                scaledHeight = Minecraft.getMinecraft().displayHeight;
                scaleFactor = GuiDisplayPokecubeInfo.scale(PokecubeMod.core.getConfig().guiSize, true);
                scaledWidth /= scaleFactor;
                scaledHeight /= scaleFactor;
                w = Math.min(scaledWidth - 147, w);
                h = Math.min(scaledHeight - 42, h);
                w = Math.max(0, w);
                h = Math.max(0, h);
                x = w;
                y = h;
                // System.out.println(x+" "+y);
                startColor = 0x22FF0000;
                endColor = 0x22FF0000;
                Rectangle guiRect = new Rectangle(x, y, 147, 42);
                top = (int) guiRect.getMinY();
                bottom = (int) guiRect.getMaxY();
                right = guiRect.getMaxX();
                left = guiRect.getMinX();
                zLevel = 0;
                s = PokecubeMod.core.getConfig().guiSize;
                guiRect.setBounds((int) (x * s), (int) (y * s), (int) (147 * s), (int) (42 * s));
                f = (startColor >> 24 & 255) / 255.0F;
                f1 = (startColor >> 16 & 255) / 255.0F;
                f2 = (startColor >> 8 & 255) / 255.0F;
                f3 = (startColor & 255) / 255.0F;
                f4 = (endColor >> 24 & 255) / 255.0F;
                f5 = (endColor >> 16 & 255) / 255.0F;
                f6 = (endColor >> 8 & 255) / 255.0F;
                f7 = (endColor & 255) / 255.0F;
                GL11.glPushMatrix();
                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                GlStateManager.shadeModel(7425);
                tessellator = Tessellator.getInstance();
                vertexbuffer = tessellator.getBuffer();
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
                GL11.glPopMatrix();

                int i = Mouse.getX() * gui.width / gui.mc.displayWidth;
                int j = gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight - 1;
                int k = 0;
                if (messRect.contains(i, j))
                {
                    messagesheld = Mouse.isButtonDown(k);
                }
                if (messagesheld)
                {
                    int mx = (int) messRect.getCenterX();
                    int my = (int) messRect.getCenterY();
                    int dx = mx - i;
                    int dy = my - j;
                    PokecubeMod.core.getConfig().messageOffset[0] -= dx;
                    PokecubeMod.core.getConfig().messageOffset[1] -= dy;
                    if (dx != 0 || dy != 0) PokecubeMod.core.getConfig().setSettings();
                }
                else
                {
                    if (guiRect.contains(i, j))
                    {
                        guiheld = Mouse.isButtonDown(k);
                    }
                    if (guiheld)
                    {
                        int mx = (int) guiRect.getCenterX();
                        int my = (int) guiRect.getCenterY();
                        int dx = mx - i;
                        int dy = my - j;
                        if (dx != 0 || dy != 0)
                        {
                            GuiDisplayPokecubeInfo.instance().moveGui(-dx, -dy);
                            PokecubeMod.core.getConfig().setSettings();
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

package pokecube.pokeplayer.client.gui;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.pokeplayer.PokePlayer;

public class GuiAsPokemob extends GuiDisplayPokecubeInfo
{
    public GuiAsPokemob()
    {
        minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        fontRenderer = minecraft.fontRendererObj;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if (minecraft.currentScreen == null
                    && !((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                    && event.type == ElementType.HOTBAR && PokePlayer.proxy.getPokemob(minecraft.thePlayer) != null)
                draw(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void draw(RenderGameOverlayEvent.Post event)
    {
        int w = PokecubeMod.core.getConfig().guiOffset[0];
        int h = PokecubeMod.core.getConfig().guiOffset[1];
        w = Math.min(event.resolution.getScaledWidth() - 105, w);
        h = Math.min(event.resolution.getScaledHeight() - 13, h);

        if (fontRenderer == null) fontRenderer = minecraft.fontRendererObj;
        GL11.glPushMatrix();

        minecraft.entityRenderer.setupOverlayRendering();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);

        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            // pokemob.setMoveIndex(pokemob.getMoveIndex());

            if (pokemob.getMoveIndex() == 5)
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            else
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            // bind texture
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(0 + w, 0 + h, 0, 0, 91, 13);
            String displayName = pokemob.getPokemonDisplayName();
            if (fontRenderer.getStringWidth(displayName) > 70)
            {

            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            fontRenderer.drawString(displayName, 2 + w, 2 + h, lightGrey);
            int moveIndex = 0;
            // Draw number of pokemon
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            int num = fontRenderer.getStringWidth("" + 1);
            this.drawTexturedModalRect(90 + w, 0 + h, 0, 0, num, 13);
            this.drawTexturedModalRect(90 + num + w, 0 + h, 81, 0, 10, 13);
            fontRenderer.drawString("" + 1, 95 + w, 3 + h, lightGrey);

            int moveCount = 0;

            for (moveCount = 0; moveCount < 4; moveCount++)
            {
                if (pokemob.getMove(moveCount) == null) break;
            }

            int dir = PokecubeMod.core.getConfig().guiDown ? 1 : -1;
            int h1 = 1;
            if (dir == -1)
            {
                h -= 25 + 12 * (moveCount - 1);
                // h1 = 0;
            }
            // pokemob.setMoveIndex(pokemob.getMoveIndex());

            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                int index = moveIndex;
                // if(dir==-1) index = 3-index;

                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(index));

                if (move != null)
                {
                    if (pokemob.getMoveIndex() == index) GL11.glColor4f(0F, 0.1F, 1.0F, 1.0F);
                    else GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    // bind texture

                    minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                    this.drawTexturedModalRect(0 + w, 13 + 12 * index + h, 0, 13 + h1, 91, 12);
                    GL11.glPushMatrix();// TODO find out why both needed
                    Color moveColor = new Color(move.getType(pokemob).colour);
                    GL11.glColor4f(moveColor.getRed() / 255f, moveColor.getGreen() / 255f, moveColor.getBlue() / 255f,
                            1.0F);
                    fontRenderer.drawString(MovesUtils.getTranslatedMove(move.getName()), 5 + 0 + w,
                            index * 12 + 14 + h, // white.getRGB());
                            move.getType(pokemob).colour);
                    GL11.glPopMatrix();
                }
            }

        }
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        GL11.glPopMatrix();
    }

    @Override
    public IPokemob getCurrentPokemob()
    {
        return PokePlayer.proxy.getPokemob(minecraft.thePlayer);
    }
}

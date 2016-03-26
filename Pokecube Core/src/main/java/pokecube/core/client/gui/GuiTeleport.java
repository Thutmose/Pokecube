/**
 *
 */
package pokecube.core.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class GuiTeleport extends Gui
{
    protected static int       lightGrey = 0xDDDDDD;
    private static GuiTeleport instance;
    public static GuiTeleport instance()
    {
        if (instance == null) new GuiTeleport();

        if (instance.locations == null) instance.locations = PokecubeSerializer.getInstance()
                .getTeleports(instance.minecraft.thePlayer.getUniqueID().toString());

        return instance;
    }
    protected FontRenderer     fontRenderer;

    protected Minecraft        minecraft;

    public List<TeleDest> locations;

    public int indexLocation = 0;

    boolean state = false;

    /**
     *
     */
    public GuiTeleport()
    {
        minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        fontRenderer = minecraft.fontRendererObj;
        instance = this;
    }

    private void draw(RenderGameOverlayEvent.Post event)
    {
        int w = PokecubeMod.core.getConfig().guiOffset[0];
        int h = PokecubeMod.core.getConfig().guiOffset[1];
        w = Math.min(event.getResolution().getScaledWidth() - 105, w);
        h = Math.min(event.getResolution().getScaledHeight() - 13, h);
        GlStateManager.pushMatrix();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        minecraft.entityRenderer.setupOverlayRendering();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        locations = PokecubeSerializer.getInstance().getTeleports(minecraft.thePlayer.getUniqueID().toString());

        int i = 0;
        int xOffset = 0;
        int yOffset = 60;
        int dir = 1;

        if (!PokecubeMod.core.getConfig().guiDown)
        {
            IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            int moveCount = 0;
            dir = -1;
            for (moveCount = 0; moveCount < 4; moveCount++)
            {
                if (pokemob.getMove(moveCount) == null) break;
            }
            yOffset = -(25 + 12 * (moveCount - 1));
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // bind texture
        minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
        this.drawTexturedModalRect(90 * xOffset + w, yOffset + h, 0, 0, 100, 13);
        fontRenderer.drawString(I18n.translateToLocal("gui.pokemob.teleport"), 2 + 90 * xOffset + w, 2 + yOffset + h, lightGrey);
        // ArrayList<Vector4> list = new ArrayList(locations.keySet());

        for (int k = 0; k < 1; k++)
        {
            if (k >= instance().locations.size()) break;
            TeleDest location = instance().locations.get((k + instance().indexLocation) % instance().locations.size());
            if (location != null)
            {

                if (k == 0) GL11.glColor4f(0F, 0.1F, 1.0F, 1.0F);
                else GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                String name = location.getName();
                int shift = 13 + 12 * i + yOffset + h;

                if (dir == -1)
                {
                    shift -= 25;
                }
                // bind texture
                minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                this.drawTexturedModalRect(90 * xOffset + w, shift, 0, 13, 100, 12);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                fontRenderer.drawString(name, 5 + xOffset * 90 + w, shift + 2, PokeType.fire.colour);
            }
            i++;
        }
        GlStateManager.popMatrix();
    }

    public boolean getState()
    {
        return instance().state;
    }

    public void nextMove()
    {
        instance().indexLocation++;
        if (instance().locations.size() > 0)
            instance().indexLocation = instance().indexLocation % instance().locations.size();
        else instance().indexLocation = 0;
    }

    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if (instance().state && minecraft.currentScreen == null
                    && !((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                    && event.getType() == ElementType.HOTBAR)
                draw(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public void previousMove()
    {
        instance().indexLocation--;
        if (instance().indexLocation < 0) instance().indexLocation = Math.max(0, instance().locations.size() - 1);
    }

    public void refresh()
    {
        instance.locations = PokecubeSerializer.getInstance()
                .getTeleports(instance.minecraft.thePlayer.getUniqueID().toString());
    }

    public void setState(boolean state)
    {
        instance().state = state;
    }
}

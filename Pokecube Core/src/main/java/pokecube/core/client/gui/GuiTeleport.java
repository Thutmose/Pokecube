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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.mod_Pokecube;
import pokecube.core.client.Resources;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class GuiTeleport extends Gui
{
    protected FontRenderer            fontRenderer;
    protected Minecraft               minecraft;
    protected static int              lightGrey = 0xDDDDDD;
    private static GuiTeleport instance;

    /**
     *
     */
    public GuiTeleport()
    {
        minecraft = (Minecraft) mod_Pokecube.getMinecraftInstance();
        fontRenderer = minecraft.fontRendererObj;
        instance = this;
    }

    public List<TeleDest> locations;

    public static GuiTeleport instance()
    {
        if (instance == null) new GuiTeleport();

        if (instance.locations == null) instance.locations = PokecubeSerializer.getInstance()
                .getTeleports(instance.minecraft.thePlayer.getUniqueID().toString());

        return instance;
    }

    public void refresh()
    {
        instance.locations = PokecubeSerializer.getInstance()
                .getTeleports(instance.minecraft.thePlayer.getUniqueID().toString());
    }

    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if (instance().state && minecraft.currentScreen == null
                    && !((Minecraft) mod_Pokecube.getMinecraftInstance()).gameSettings.hideGUI
                    && event.type == ElementType.HOTBAR)
                draw(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void draw(RenderGameOverlayEvent.Post event)
    {
        int h = Mod_Pokecube_Helper.guiOffset[0];
        int w = Mod_Pokecube_Helper.guiOffset[1];
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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // bind texture
        minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
        this.drawTexturedModalRect(90 * xOffset + h, yOffset + w, 0, 0, 100, 13);
        fontRenderer.drawString("Teleports", 2 + 90 * xOffset + h, 2 + yOffset + w, lightGrey);
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

                // bind texture
                minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                this.drawTexturedModalRect(90 * xOffset + h, 13 + 12 * i + yOffset + w, 0, 13, 100, 12);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                fontRenderer.drawString(name, 5 + xOffset * 90 + h, i * 12 + 14 + yOffset + w, PokeType.fire.colour);
            }
            i++;
        }
        GlStateManager.popMatrix();
    }

    public int indexLocation = 0;

    public void nextMove()
    {
        instance().indexLocation++;
        if (instance().locations.size() > 0)
            instance().indexLocation = instance().indexLocation % instance().locations.size();
        else instance().indexLocation = 0;
    }

    boolean state = false;

    public void setState(boolean state)
    {
        instance().state = state;
    }

    public boolean getState()
    {
        return instance().state;
    }

    public void previousMove()
    {
        instance().indexLocation--;
        if (instance().indexLocation < 0) instance().indexLocation = Math.max(0, instance().locations.size() - 1);
    }
}

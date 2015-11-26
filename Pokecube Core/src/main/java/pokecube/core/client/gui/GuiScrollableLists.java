/**
 *
 */
package pokecube.core.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.mod_Pokecube;
import pokecube.core.client.Resources;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

/** @author Manchou */
public class GuiScrollableLists extends Gui
{
    protected FontRenderer            fontRenderer;
    protected Minecraft               minecraft;
    protected static int              lightGrey = 0xDDDDDD;
    private static GuiScrollableLists instance;

    /**
     *
     */
    public GuiScrollableLists()
    {
        minecraft = (Minecraft) mod_Pokecube.getMinecraftInstance();
        fontRenderer = minecraft.fontRendererObj;
        instance = this;
    }

    public List<TeleDest> locations;

    public static GuiScrollableLists instance()
    {
        if (instance == null) new GuiScrollableLists();

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
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        try
        {
            if (state && minecraft.currentScreen == null
                    && !((Minecraft) mod_Pokecube.getMinecraftInstance()).gameSettings.hideGUI)
                draw(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void draw(RenderWorldLastEvent event)
    {
        int currentItemIndex = mod_Pokecube.getPlayer(null).inventory.currentItem;
        int h = Mod_Pokecube_Helper.guiOffset[0];
        int w = Mod_Pokecube_Helper.guiOffset[1];

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();

        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        minecraft.entityRenderer.setupOverlayRendering();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        minecraft.entityRenderer.setupOverlayRendering();

        locations = PokecubeSerializer.getInstance().getTeleports(minecraft.thePlayer.getUniqueID().toString());

        int i = 0;
        int j = 0;

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
            if (k >= locations.size()) break;
            TeleDest location = locations.get((k + indexLocation) % locations.size());
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

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();

    }

    public int indexLocation = 0;

    public void nextMove()
    {
        indexLocation++;
        if (locations.size() > 0) indexLocation = indexLocation % locations.size();
        else indexLocation = 0;
    }

    boolean state = false;

    public void setState(boolean state)
    {
        this.state = state;
    }

    public boolean getState()
    {
        return state;
    }

    public void previousMove()
    {
        indexLocation--;
        if (indexLocation < 0) indexLocation = Math.max(0, locations.size() - 1);
    }
}

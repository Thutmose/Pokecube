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
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
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

    public static void create()
    {
        if (instance != null) MinecraftForge.EVENT_BUS.unregister(instance);
        instance = new GuiTeleport();
    }

    public static GuiTeleport instance()
    {
        if (instance == null) create();

        if (instance.locations == null) instance.locations = PokecubeSerializer.getInstance()
                .getTeleports(instance.minecraft.thePlayer.getCachedUniqueIdString());

        return instance;
    }

    protected FontRenderer fontRenderer;

    protected Minecraft    minecraft;

    public List<TeleDest>  locations;

    public int             indexLocation = 0;

    boolean                state         = false;

    /**
     *
     */
    private GuiTeleport()
    {
        minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        MinecraftForge.EVENT_BUS.register(this);
        fontRenderer = minecraft.fontRendererObj;
        instance = this;
    }

    private void draw(RenderGameOverlayEvent.Post event)
    {
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;
        GlStateManager.pushMatrix();
        int w = PokecubeMod.core.getConfig().guiPos[0];
        int h = PokecubeMod.core.getConfig().guiPos[1];
        int scaledWidth = Minecraft.getMinecraft().displayWidth;
        int scaledHeight = Minecraft.getMinecraft().displayHeight;
        float scaleFactor = GuiDisplayPokecubeInfo.scale(PokecubeMod.core.getConfig().guiSize, true);
        scaledWidth /= scaleFactor;
        scaledHeight /= scaleFactor;
        w = Math.min(scaledWidth, w);
        h = Math.min(scaledHeight, h);
        w = Math.max(0, w);
        h = Math.max(0, h);
        GlStateManager.enableBlend();
        locations = PokecubeSerializer.getInstance().getTeleports(minecraft.thePlayer.getCachedUniqueIdString());
        int i = 0;
        int xOffset = 43;
        int yOffset = 73;
        int dir = 1;
        if (!PokecubeMod.core.getConfig().guiDown)
        {
            int moveCount = 0;
            dir = -1;
            for (moveCount = 0; moveCount < 4; moveCount++)
            {
                if (pokemob.getMove(moveCount) == null) break;
            }
            yOffset = -(11 + 14 * (moveCount - 1));
        }
        else
        {
            int moveCount = 0;
            for (moveCount = 0; moveCount < 4; moveCount++)
            {
                if (pokemob.getMove(moveCount) == null) break;
            }
            yOffset = (34 + 13 * (moveCount - 1));
        }
        // bind texture
        minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
        this.drawTexturedModalRect(xOffset + w, yOffset + h, 44, 0, 90, 13);
        fontRenderer.drawString(I18n.format("gui.pokemob.teleport"), 2 + xOffset + w, 2 + yOffset + h, lightGrey);

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
                this.drawTexturedModalRect(xOffset + w, shift, 44, 22, 91, 12);
                fontRenderer.drawString(name, 5 + xOffset + w, shift + 2, PokeType.fire.colour);
            }
            i++;
            GlStateManager.disableBlend();
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
                .getTeleports(instance.minecraft.thePlayer.getCachedUniqueIdString());
    }

    public void setState(boolean state)
    {
        instance().state = state;
    }
}

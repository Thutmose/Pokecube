/**
 *
 */
package pokecube.core.client.gui;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class GuiTeleport extends Gui
{
    protected static int      lightGrey = 0xDDDDDD;
    /** This is made public incase an addon needs to replace it. Do not
     * reference this otherwise, always use instance() */
    public static GuiTeleport instance;

    public static void create()
    {
        if (instance != null) MinecraftForge.EVENT_BUS.unregister(instance);
        instance = new GuiTeleport();
    }

    public static GuiTeleport instance()
    {
        if (instance == null) create();
        return instance;
    }

    protected FontRenderer fontRenderer;

    protected Minecraft    minecraft;

    boolean                state = false;

    /**
     *
     */
    private GuiTeleport()
    {
        minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        MinecraftForge.EVENT_BUS.register(this);
        fontRenderer = minecraft.fontRenderer;
        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void draw(GuiEvent.RenderTeleports event)
    {
        if (!state) return;
        GuiDisplayPokecubeInfo.teleDims[0] = 89;
        GuiDisplayPokecubeInfo.teleDims[1] = 25;
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;
        GlStateManager.pushMatrix();
        GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().teleRef,
                PokecubeMod.core.getConfig().telePos, GuiDisplayPokecubeInfo.teleDims,
                PokecubeMod.core.getConfig().teleSize);
        GlStateManager.enableBlend();
        int h = 0;
        int w = 0;
        int i = 0;
        int xOffset = 0;
        int yOffset = 0;
        int dir = 1;
        // bind texture
        minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
        this.drawTexturedModalRect(xOffset + w, yOffset + h, 44, 0, 90, 13);
        fontRenderer.drawString(I18n.format("gui.pokemob.teleport"), 2 + xOffset + w, 2 + yOffset + h, lightGrey);

        TeleDest location = TeleportHandler.getTeleport(minecraft.player.getCachedUniqueIdString());
        if (location != null)
        {
            GL11.glColor4f(0F, 0.1F, 1.0F, 1.0F);
            String name = location.getName();
            int shift = 13 + 12 * i + yOffset + h;
            if (dir == -1)
            {
                shift -= 25;
            }
            // bind texture
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(xOffset + w, shift, 44, 22, 91, 12);
            fontRenderer.drawString(name, 5 + xOffset + w, shift + 2, PokeType.getType("fire").colour);
        }
        i++;
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public boolean getState()
    {
        return this.state;
    }

    public void nextMove()
    {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(5));
        buffer.writeByte(PokecubeServerPacket.TELEPORT);
        String uuid = minecraft.player.getCachedUniqueIdString();
        int index = TeleportHandler.getTeleIndex(uuid) + 1;
        buffer.writeInt(index);
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeServerPacket packet = new PokecubeServerPacket(buffer);
        PokecubePacketHandler.sendToServer(packet);
    }

    public void previousMove()
    {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(5));
        buffer.writeByte(PokecubeServerPacket.TELEPORT);
        String uuid = minecraft.player.getCachedUniqueIdString();
        int index = TeleportHandler.getTeleIndex(uuid) - 1;
        buffer.writeInt(index);
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeServerPacket packet = new PokecubeServerPacket(buffer);
        PokecubePacketHandler.sendToServer(packet);
    }

    public void setState(boolean state)
    {
        this.state = state;
    }
}

package pokecube.core.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;

public class GuiHealTable extends GuiContainer
{
    public GuiHealTable(InventoryPlayer player_inventory)
    {
        super(new ContainerHealTable(player_inventory));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id != 0) ;

        if (guibutton.id == 1)
        {
            PokecubeServerPacket packet = new PokecubeServerPacket(PokecubeServerPacket.POKECENTER);
            PokecubePacketHandler.sendToServer(packet);
            ((ContainerHealTable) inventorySlots).heal();// client side
            mc.thePlayer.playSound(new SoundEvent(new ResourceLocation(PokecubeMod.ID + ":pokecenter")), 3, 1);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // bind texture
        mc.renderEngine.bindTexture(Resources.GUI_HEAL_TABLE);
        int j2 = (width - xSize) / 2;
        int k2 = (height - ySize) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, xSize, ySize);
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        String heal = I18n.format("tile.pokecenter.heal");
        buttonList.add(new GuiButton(1, width / 2 + 20, height / 2 - 50, 60, 20, heal));
        super.initGui();
    }
}
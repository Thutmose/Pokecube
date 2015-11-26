package pokecube.core.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import pokecube.core.mod_Pokecube;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.blocks.healtable.TileHealTable;
import pokecube.core.client.Resources;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;

public class GuiHealTable extends GuiContainer
{
    public GuiHealTable(InventoryPlayer player_inventory,
            TileHealTable tile_entity)
    {
        super(new ContainerHealTable(tile_entity, player_inventory));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id != 0);

        if (guibutton.id == 1)
        {
        	PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubePacketHandler.CHANNEL_ID_HealTable, "coucou".getBytes());
        	PokecubePacketHandler.sendToServer(packet);
        	
            ((ContainerHealTable) inventorySlots).heal();// client side
            mc.thePlayer.playSound(mod_Pokecube.ID+":pokecenter", 3, 1);
        }
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        String heal = StatCollector.translateToLocal("tile.pokecenter.heal");
        buttonList.add(new GuiButton(1, width / 2 + 20, height / 2 - 50, 60, 20, heal));
        super.initGui();
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
}
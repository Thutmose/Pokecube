package pokecube.core.client.gui.blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PCPacketHandler;
import pokecube.core.network.PCPacketHandler.MessageServer;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokeType;

public class GuiTMCreator extends GuiContainer
{
    final TileEntityTradingTable table;
    ContainerTMCreator           cont;
    GuiTextField                 textFieldSearch;

    int               index = 0;
    ArrayList<String> moves = new ArrayList<String>();

    private Slot theSlot;

    public GuiTMCreator(ContainerTMCreator container)
    {
        super(container);
        cont = container;
        this.table = cont.getTile();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 2 && !moves.isEmpty())
        {
            index++;
        }
        if (guibutton.id == 3 && !moves.isEmpty())
        {
            index--;
        }
        if (guibutton.id == 4 && !moves.isEmpty())
        {
            table.addMoveToTM(moves.get(index));
            String message = 2 + "," + moves.get(index);
            MessageServer packet = PCPacketHandler.makeServerPacket(MessageServer.TRADE, message.getBytes());
            PokecubePacketHandler.sendToServer(packet);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/moveTutorGui.png"));
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        int yOffset = -ySize / 2 + 2;// height / 2 - 164;
        int xOffset = xSize / 2 - 42;

        if (moves.isEmpty() && textFieldSearch.getText().isEmpty())
        {
            List<String> mov = table.getMoves(mc.thePlayer.getUniqueID().toString());
            if (mov != null)
            {
                moves.addAll(mov);
            }
        }
        if (moves.isEmpty()) return;

        index = Math.min(index, moves.size());
        if (index < 0)
        {
            index = moves.size() - 1;
        }

        index = index % moves.size();

        if (moves != null && moves.size() > 0 && index >= 0)
        {
            String s = moves.get(index).trim();

            Move_Base move = MovesUtils.getMoveFromName(s);
            if (move != null)
            {
                drawString(fontRendererObj, MovesUtils.getTranslatedMove(s), xOffset + 14, yOffset + 99,
                        move.getType(null).colour);
                drawString(fontRendererObj, "" + move.getPWR(), xOffset + 102, yOffset + 99, 0xffffff);
            }
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        textFieldSearch.drawTextBox();
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        int xOffset = 0;
        int yOffset = 22;

        String next = StatCollector.translateToLocal("tile.pc.next");
        buttonList.add(new GuiButton(2, width / 2 - xOffset + 28, height / 2 - yOffset, 50, 20, next));
        String prev = StatCollector.translateToLocal("tile.pc.previous");
        buttonList.add(new GuiButton(3, width / 2 - xOffset - 78, height / 2 - yOffset, 50, 20, prev));
        String apply = StatCollector.translateToLocal("tile.tradingtable.apply");
        buttonList.add(new GuiButton(4, width / 2 - xOffset - 25, height / 2 - yOffset, 50, 20, apply));
        textFieldSearch = new GuiTextField(0, fontRendererObj, width / 2 - xOffset - 29, height / 2 - yOffset - 25, 90,
                10);
        textFieldSearch.setText("");

        super.initGui();
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        keyTyped2(par1, par2);
        textFieldSearch.textboxKeyTyped(par1, par2);

        ArrayList<String> playerMoves = table.getMoves(mc.thePlayer.getUniqueID().toString());

        if (playerMoves == null) return;

        moves.clear();

        moves.addAll(playerMoves);

        ArrayList<String> noMatch = new ArrayList<String>();
        if (!textFieldSearch.getText().isEmpty()) for (String s : moves)
        {
            Move_Base move = MovesUtils.getMoveFromName(s.trim());
            boolean nameMatch = MovesUtils.getTranslatedMove(s.trim()).toLowerCase()
                    .contains(textFieldSearch.getText().toLowerCase());
            boolean typeMatch = false;
            if (!nameMatch)
            {
                for (PokeType t : PokeType.values())
                {
                    if (move.getType(null) == t && PokeType.getTranslatedName(t).toLowerCase()
                            .contains(textFieldSearch.getText().toLowerCase()))
                    {
                        typeMatch = true;
                        break;
                    }
                }
            }
            if (!typeMatch && !nameMatch)
            {
                noMatch.add(s);
            }

        }
        moves.removeAll(noMatch);
    }

    /** Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). */
    protected void keyTyped2(char par1, int par2)
    {
        this.checkHotbarKeys(par2);
        if (par2 == Keyboard.KEY_LEFT)
        {
            index--;
        }
        else if (par2 == Keyboard.KEY_RIGHT)
        {
            index++;
        }
        if (par2 == 1)
        {
            mc.thePlayer.closeScreen();
            return;
        }
        if (this.theSlot != null && this.theSlot.getHasStack())
        {
            if (par2 == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, 3);
            }
            else if (par2 == this.mc.gameSettings.keyBindDrop.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        textFieldSearch.mouseClicked(par1, par2, par3);

    }

}

package pokecube.core.client.gui.blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.TileEntityTMMachine;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketTrade;
import pokecube.core.utils.PokeType;

public class GuiTMCreator extends GuiContainer
{
    final TileEntityTMMachine table;
    ContainerTMCreator           cont;
    GuiTextField                 textFieldSearch;

    int                          index = 0;
    ArrayList<String>            moves = new ArrayList<String>();

    private Slot                 theSlot;

    public GuiTMCreator(ContainerTMCreator container)
    {
        super(container);
        cont = container;
        this.table = cont.getTile() == null ? new TileEntityTMMachine() : cont.getTile();
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
            PacketTrade packet = new PacketTrade(PacketTrade.MAKETM);
            packet.data.setString("M", moves.get(index));
            PokecubePacketHandler.sendToServer(packet);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/movetutorgui.png"));
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
            List<String> mov = table.getMoves(mc.player.getCachedUniqueIdString());
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
                drawString(fontRenderer, MovesUtils.getMoveName(s).getFormattedText(), xOffset + 14, yOffset + 99,
                        move.getType(null).colour);
                drawString(fontRenderer, "" + move.getPWR(), xOffset + 102, yOffset + 99, 0xffffff);
            }
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        this.drawDefaultBackground();
        super.drawScreen(i, j, f);
        textFieldSearch.drawTextBox();
        this.renderHoveredToolTip(i, j);
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        int xOffset = 0;
        int yOffset = 22;

        String next = I18n.format("tile.pc.next");
        buttonList.add(new GuiButton(2, width / 2 - xOffset + 28, height / 2 - yOffset, 50, 20, next));
        String prev = I18n.format("tile.pc.previous");
        buttonList.add(new GuiButton(3, width / 2 - xOffset - 78, height / 2 - yOffset, 50, 20, prev));
        String apply = I18n.format("tile.tradingtable.apply");
        buttonList.add(new GuiButton(4, width / 2 - xOffset - 25, height / 2 - yOffset, 50, 20, apply));
        textFieldSearch = new GuiTextField(0, fontRenderer, width / 2 - xOffset - 29, height / 2 - yOffset - 25, 90,
                10);
        textFieldSearch.setText("");

        super.initGui();
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        keyTyped2(par1, par2);
        textFieldSearch.textboxKeyTyped(par1, par2);

        ArrayList<String> playerMoves = table.getMoves(mc.player.getCachedUniqueIdString());

        if (playerMoves == null) return;

        moves.clear();

        moves.addAll(playerMoves);

        ArrayList<String> noMatch = new ArrayList<String>();
        if (!textFieldSearch.getText().isEmpty()) for (String s : moves)
        {
            Move_Base move = MovesUtils.getMoveFromName(s.trim());
            if (move == null) continue;
            boolean nameMatch = MovesUtils.getMoveName(s.trim()).getFormattedText()
                    .toLowerCase(java.util.Locale.ENGLISH)
                    .contains(textFieldSearch.getText().toLowerCase(java.util.Locale.ENGLISH));
            boolean typeMatch = false;
            if (!nameMatch)
            {
                for (PokeType t : PokeType.values())
                {
                    if (move.getType(null) == t && PokeType.getTranslatedName(t).toLowerCase(java.util.Locale.ENGLISH)
                            .contains(textFieldSearch.getText().toLowerCase(java.util.Locale.ENGLISH)))
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
            mc.player.closeScreen();
            return;
        }
        if (this.theSlot != null && this.theSlot.getHasStack())
        {
            if (par2 == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, ClickType.CLONE);
            }
            else if (par2 == this.mc.gameSettings.keyBindDrop.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
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

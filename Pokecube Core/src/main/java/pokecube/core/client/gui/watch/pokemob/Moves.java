package pokecube.core.client.gui.watch.pokemob;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;

public class Moves extends ListPage
{
    private int[][] moveOffsets;

    public Moves(GuiPokeWatch watch, IPokemob pokemob)
    {
        super(watch, pokemob, "moves");
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
    }

    @Override
    void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 20;
        int offsetY = (watch.height - 160) / 2 + 85;
        int height = fontRenderer.FONT_HEIGHT * 6;
        int width = 135;

        int y0 = offsetY;
        int y1 = offsetY + height;
        int colour = 0xFFFFFFFF;

        if (!watch.canEdit(pokemob))
        {
            width = 111;
            int dx = 25;
            int dy = -57;
            y0 += dy;
            y1 += dy;
            offsetY += dy;
            offsetX += dx;
        }

        final Moves thisObj = this;
        IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(ITextComponent component)
            {
                return thisObj.handleComponentClick(component);
            }

            @Override
            public void handleHovor(ITextComponent component, int x, int y)
            {
                thisObj.handleComponentHover(component, x, y);
            }
        };

        PokedexEntry entry = pokemob.getPokedexEntry();
        Set<String> added = Sets.newHashSet();
        for (int i = 0; i < 100; i++)
        {
            List<String> moves = entry.getMovesForLevel(i, i - 1);
            for (String s : moves)
            {
                added.add(s);
                ITextComponent moveName = MovesUtils.getMoveName(s);
                moveName.setStyle(new Style());
                moveName.getStyle().setColor(TextFormatting.RED);
                ITextComponent main = new TextComponentTranslation("pokewatch.moves.lvl", i, moveName);
                main.setStyle(new Style());
                main.getStyle().setColor(TextFormatting.GREEN);
                entries.add(new LineEntry(y0, y1, fontRenderer, main, colour).setClickListner(listener));
            }
        }
        for (String s : entry.getMoves())
        {
            added.add(s);
            ITextComponent moveName = MovesUtils.getMoveName(s);
            moveName.setStyle(new Style());
            moveName.getStyle().setColor(TextFormatting.RED);
            ITextComponent main = new TextComponentTranslation("pokewatch.moves.tm", moveName);
            main.setStyle(new Style());
            main.getStyle().setColor(TextFormatting.GREEN);
            entries.add(new LineEntry(y0, y1, fontRenderer, main, colour).setClickListner(listener));
        }
        list = new ScrollGui(mc, width, height, fontRenderer.FONT_HEIGHT, offsetX, offsetY, entries);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        //@formatter:off
        moveOffsets = new int[][]{
        // i = index, b = selected, dc = cursor offset
        //   dx  dy  b  i  dc
            {00, 00, 0, 0, 0},
            {00, 10, 0, 1, 0},
            {00, 20, 0, 2, 0},
            {00, 30, 0, 3, 0},
            {00, 42, 0, 4, 0}
        };
        //@formatter:on
    }

    @Override
    void drawInfo(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
        if (watch.canEdit(pokemob)) drawMoves(x, y);
    }

    private void drawMoves(int x, int y)
    {
        // Draw the pokemob's moves
        int dx = -30;
        int dy = 20;
        int held = -1;
        for (int i = 0; i < moveOffsets.length; i++)
        {
            int[] offset = moveOffsets[i];
            if (offset[2] > 0)
            {
                held = i;
                continue;
            }
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(offset[3]));
            if (move != null)
            {
                drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), x + dx,
                        y + dy + offset[1] + offset[4], move.getType(pokemob).colour);
            }
        }
        if (held != -1)
        {
            int[] offset = moveOffsets[held];
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(offset[3]));
            if (move != null)
            {
                drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), x + dx,
                        y + dy + offset[1], move.getType(pokemob).colour);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0 && new Exception().getStackTrace()[4].getClassName()
                .equals("pokecube.core.client.gui.watch.GuiPokeWatch"))
        {
            for (int i = 0; i < moveOffsets.length; i++)
            {
                if (moveOffsets[i][2] != 0) return;
            }
            int x = (watch.width - 160) / 2 + 80;
            int y = (watch.height - 160) / 2 + 8;
            int dx = -30;
            int dy = 20;
            int x1 = mouseX - (x + dx);
            int y1 = mouseY - (y + dy);
            boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                moveOffsets[index][2] = 1;
                moveOffsets[index][4] = (y1 - 10 * index);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick)
    {
        if (mouseButton == 0 && new Exception().getStackTrace()[4].getClassName()
                .equals("pokecube.core.client.gui.watch.GuiPokeWatch"))
        {
            int heldIndex = -1;
            for (int i = 0; i < moveOffsets.length; i++)
            {
                if (moveOffsets[i][2] != 0)
                {
                    heldIndex = i;
                    break;
                }
            }
            if (heldIndex != -1)
            {
                int x = (watch.width - 160) / 2 + 80;
                int y = (watch.height - 160) / 2 + 8;
                int dx = -30;
                int dy = 20;
                int x1 = mouseX - (x + dx);
                int y1 = mouseY - (y + dy);
                boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
                if (inBox)
                {
                    moveOffsets[heldIndex][1] = y1 - moveOffsets[heldIndex][4];
                }
            }
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0 && new Exception().getStackTrace()[4].getClassName()
                .equals("pokecube.core.client.gui.watch.GuiPokeWatch"))
        {
            int oldIndex = -1;
            for (int i = 0; i < moveOffsets.length; i++)
            {
                if (moveOffsets[i][2] != 0)
                {
                    oldIndex = i;
                    moveOffsets[i][2] = 0;
                }
            }
            if (oldIndex == -1) return;
            int x = (watch.width - 160) / 2 + 80;
            int y = (watch.height - 160) / 2 + 8;
            int dx = -30;
            int dy = 20;
            int x1 = mouseX - (x + dx);
            int y1 = mouseY - (y + dy);
            boolean inBox = x1 > 0 && y1 > 0 && x1 < 95 && y1 < 52;
            if (inBox)
            {
                int index = y1 / 10;
                index = Math.min(index, 4);
                index = Math.max(index, 0);
                pokemob.exchangeMoves(oldIndex, index);
            }
            //@formatter:off
            moveOffsets = new int[][]{
                {00, 00, 0, 0, 0},
                {00, 10, 0, 1, 0},
                {00, 20, 0, 2, 0},
                {00, 30, 0, 3, 0},
                {00, 42, 0, 4, 0}
            };
            //@formatter:on
        }
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        return super.handleComponentClick(component);
    }

    @Override
    protected void handleComponentHover(ITextComponent component, int x, int y)
    {
        super.handleComponentHover(component, x, y);
    }

}

package pokecube.core.client.gui.watch.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.utils.PokeType;

public class PerTypeProgress extends Progress
{
    GuiTextField text;
    PokeType     type;

    public PerTypeProgress(GuiPokeWatch watch)
    {
        super(watch);
        setTitle(I18n.format("pokewatch.progress.type.title"));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = watch.width / 2 - 30;
        int y = watch.height / 2 + 53;
        text = new GuiTextField(0, fontRender, x, y, 60, 10);
    }

    @Override
    public void onPageOpened()
    {
        lines.clear();
        if (type == null)
        {
            int index = PokeType.values().length == 0 ? 0 : 1;
            type = PokeType.values()[index];
        }
        text.setText(PokeType.getTranslatedName(type));

        caught0 = CaptureStats.getUniqueOfTypeCaughtBy(watch.player.getUniqueID(), type);
        caught1 = CaptureStats.getTotalOfTypeCaughtBy(watch.player.getUniqueID(), type);

        hatched0 = EggStats.getUniqueOfTypeHatchedBy(watch.player.getUniqueID(), type);
        hatched1 = EggStats.getTotalOfTypeHatchedBy(watch.player.getUniqueID(), type);

        killed0 = KillStats.getUniqueOfTypeKilledBy(watch.player.getUniqueID(), type);
        killed1 = KillStats.getTotalOfTypeKilledBy(watch.player.getUniqueID(), type);

        String captureLine = I18n.format("pokewatch.progress.type.caught", caught1, caught0, type);
        String killLine = I18n.format("pokewatch.progress.type.killed", killed1, killed0, type);
        String hatchLine = I18n.format("pokewatch.progress.type.hatched", hatched1, hatched0, type);

        for (String line : fontRender.listFormattedStringToWidth(captureLine, 120))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(killLine, 120))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(hatchLine, 120))
        {
            lines.add(line);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 30;
        int dy = 0;
        int colour = 0xFFFFFFFF;
        for (String s : lines)
        {
            this.drawCenteredString(fontRender, s, x, y + dy, colour);
            dy += fontRender.FONT_HEIGHT;
        }
        text.drawTextBox();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (new Exception().getStackTrace()[2].getClassName().equals("pokecube.core.client.gui.watch.GuiPokeWatch"))
            text.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        text.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB)
        {
            String text = this.text.getText();
            List<String> ret = new ArrayList<String>();
            for (PokeType type : PokeType.values())
            {
                String check = PokeType.getTranslatedName(type).toLowerCase(java.util.Locale.ENGLISH);
                if (check.startsWith(text.toLowerCase(java.util.Locale.ENGLISH)))
                {
                    String name = PokeType.getTranslatedName(type);
                    ret.add(name);
                }
            }
            String[] args = { text };
            ret = CommandBase.getListOfStringsMatchingLastWord(args, ret);
            if (!ret.isEmpty()) this.text.setText(ret.get(0));
        }
        else if (keyCode == Keyboard.KEY_RETURN)
        {
            PokeType newType = PokeType.getType(text.getText());
            if (newType != null)
            {
                text.setText(PokeType.getTranslatedName(newType));
                this.type = newType;
                this.onPageOpened();
            }
            else
            {
                text.setText(PokeType.getTranslatedName(type));
            }
        }
    }

}

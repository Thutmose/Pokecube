package pokecube.core.client.gui.watch.progress;

import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;

public class GlobalProgress extends Progress
{

    public GlobalProgress(GuiPokeWatch watch)
    {
        super(watch);
        setTitle(I18n.format("pokewatch.progress.global.title"));
    }

    @Override
    public void onPageOpened()
    {
        lines.clear();
        caught0 = CaptureStats.getNumberUniqueCaughtBy(watch.player.getUniqueID());
        caught1 = CaptureStats.getTotalNumberCaughtBy(watch.player.getUniqueID());

        hatched0 = EggStats.getNumberUniqueHatchedBy(watch.player.getUniqueID());
        hatched1 = EggStats.getTotalNumberHatchedBy(watch.player.getUniqueID());

        killed0 = KillStats.getNumberUniqueKilledBy(watch.player.getUniqueID());
        killed1 = KillStats.getTotalNumberKilledBy(watch.player.getUniqueID());

        String captureLine = I18n.format("pokewatch.progress.global.caught", caught1, caught0);
        String killLine = I18n.format("pokewatch.progress.global.killed", killed1, killed0);
        String hatchLine = I18n.format("pokewatch.progress.global.hatched", hatched1, hatched0);

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
    }

}

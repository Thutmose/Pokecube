package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;

public class StatsInfo extends PokeInfoPage
{
    private FontRenderer fontRender;

    public StatsInfo(GuiPokeWatch watch, IPokemob pokemob)
    {
        super(watch, pokemob, "stats");
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.fontRender = this.fontRenderer;
    }

    @Override
    void drawInfo(int mouseX, int mouseY, float partialTicks)
    {
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;
        if (watch.canEdit(pokemob)) drawInfo(x, y);
        else drawBaseStats(x, y);
    }

    private void drawBaseStats(int x, int y)
    {
        int HP = pokemob.getPokedexEntry().getStatHP();
        int ATT = pokemob.getPokedexEntry().getStatATT();
        int DEF = pokemob.getPokedexEntry().getStatDEF();
        int ATTSPE = pokemob.getPokedexEntry().getStatATTSPE();
        int DEFSPE = pokemob.getPokedexEntry().getStatDEFSPE();
        int VIT = pokemob.getPokedexEntry().getStatVIT();
        int statYOffSet = y + 0;
        int offsetX = -50;
        int dx = 20 + offsetX;
        drawString(fontRender, "HP", x + dx, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, "ATT", x + dx, statYOffSet + 27, 0xF08030);
        drawString(fontRender, "DEF", x + dx, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, "ATTSPE", x + dx, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, "DEFSPE", x + dx, statYOffSet + 54, 0x78C850);
        drawString(fontRender, "VIT", x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        drawString(fontRender, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        drawString(fontRender, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        drawString(fontRender, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);
    }

    private void drawInfo(int x, int y)
    {
        byte[] nature = pokemob.getNature().getStatsMod();
        int HP = pokemob.getStat(Stats.HP, true);
        int ATT = pokemob.getStat(Stats.ATTACK, true);
        int DEF = pokemob.getStat(Stats.DEFENSE, true);
        int ATTSPE = pokemob.getStat(Stats.SPATTACK, true);
        int DEFSPE = pokemob.getStat(Stats.SPDEFENSE, true);
        int VIT = pokemob.getStat(Stats.VIT, true);
        int statYOffSet = y + 58;
        String[] nat = new String[6];
        int[] colours = new int[6];
        for (int n = 0; n < 6; n++)
        {
            nat[n] = "";
            colours[n] = 0;
            if (nature[n] == -1)
            {
                nat[n] = "-";
                colours[n] = 0x4400FF00;
            }
            if (nature[n] == 1)
            {
                nat[n] = "+";
                colours[n] = 0x44FF0000;
            }
        }
        int offsetX = -52;
        int dx = 20 + offsetX;
        for (int i = 0; i < nature.length; i++)
        {
            int dy = 17 + i * 9;
            drawRect(x + dx, statYOffSet + dy, x + dx + 107, statYOffSet + dy + 9, colours[i]);
        }
        drawString(fontRender, "           TV   IV   EV", x + dx, statYOffSet + 9, 0xFFFFFF);
        drawString(fontRender, "HP", x + dx, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, "ATT", x + dx, statYOffSet + 27, 0xF08030);
        drawString(fontRender, "DEF", x + dx, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, "ATTSPE", x + dx, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, "DEFSPE", x + dx, statYOffSet + 54, 0x78C850);
        drawString(fontRender, "VIT", x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        drawString(fontRender, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        drawString(fontRender, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        drawString(fontRender, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);

        dx = 20 + offsetX;
        byte[] stats2 = pokemob.getIVs();
        HP = stats2[0];
        ATT = stats2[1];
        DEF = stats2[2];
        ATTSPE = stats2[3];
        DEFSPE = stats2[4];
        VIT = stats2[5];
        stats2 = pokemob.getEVs();
        int HP2 = stats2[0] + 128;
        int ATT2 = stats2[1] + 128;
        int DEF2 = stats2[2] + 128;
        int ATTSPE2 = stats2[3] + 128;
        int DEFSPE2 = stats2[4] + 128;
        int VIT2 = stats2[5] + 128;

        int shift = 88 + offsetX;
        drawString(fontRender, "" + HP, x + shift, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, "" + ATT, x + shift, statYOffSet + 27, 0xF08030);
        drawString(fontRender, "" + DEF, x + shift, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, "" + ATTSPE, x + shift, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, "" + DEFSPE, x + shift, statYOffSet + 54, 0x78C850);
        drawString(fontRender, "" + VIT, x + shift, statYOffSet + 63, 0xF85888);
        shift += 21;
        drawString(fontRender, "" + HP2, x + shift, statYOffSet + 18, 0xFF0000);
        drawString(fontRender, "" + ATT2, x + shift, statYOffSet + 27, 0xF08030);
        drawString(fontRender, "" + DEF2, x + shift, statYOffSet + 36, 0xF8D030);
        drawString(fontRender, "" + ATTSPE2, x + shift, statYOffSet + 45, 0x6890F0);
        drawString(fontRender, "" + DEFSPE2, x + shift, statYOffSet + 54, 0x78C850);
        drawString(fontRender, "" + VIT2, x + shift, statYOffSet + 63, 0xF85888);

        // Draw ability, Happiness and Size
        Ability ability = pokemob.getAbility();
        dx = -25;
        int dy = 25;
        // Draw ability
        if (ability != null)
        {
            drawString(fontRender, "AB: " + I18n.format(ability.getName()), x + dx, y + dy, 0xFFFFFF);
        }
        int happiness = pokemob.getHappiness();
        String message = "";

        // Draw size
        dy += 11;
        message = "Size: " + pokemob.getSize();
        fontRender.drawSplitString(message, x + dx, y + dy, 100, 0xFFFFFF);

        if (happiness == 0)
        {
            message = "pokemob.info.happy0";
        }
        if (happiness > 0)
        {
            message = "pokemob.info.happy1";
        }
        if (happiness > 49)
        {
            message = "pokemob.info.happy2";
        }
        if (happiness > 99)
        {
            message = "pokemob.info.happy3";
        }
        if (happiness > 149)
        {
            message = "pokemob.info.happy4";
        }
        if (happiness > 199)
        {
            message = "pokemob.info.happy5";
        }
        if (happiness > 254)
        {
            message = "pokemob.info.happy6";
        }
        // Draw Happiness
        message = I18n.format(message);
        dy += 11;
        fontRender.drawSplitString(message, x + dx, y + dy, 100, 0xFFFFFF);
    }

}

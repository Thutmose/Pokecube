package pokecube.core.client.gui.watch.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Predicate;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.AxisAlignedBB;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
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

        AxisAlignedBB centre = watch.player.getEntityBoundingBox();
        AxisAlignedBB bb = centre.grow(PokecubeMod.core.getConfig().maxSpawnRadius, 5,
                PokecubeMod.core.getConfig().maxSpawnRadius);
        List<Entity> otherMobs = watch.player.getEntityWorld().getEntitiesInAABBexcluding(watch.player, bb,
                new Predicate<Entity>()
                {
                    @Override
                    public boolean apply(Entity input)
                    {
                        IPokemob pokemob;
                        if (!(input instanceof EntityAnimal
                                && (pokemob = CapabilityPokemob.getPokemobFor(input)) != null))
                            return false;
                        return pokemob.isType(type);
                    }
                });
        String nearbyLine = I18n.format("pokewatch.progress.global.nearby", otherMobs.size());

        for (String line : fontRender.listFormattedStringToWidth(captureLine, 140))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(killLine, 140))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(hatchLine, 140))
        {
            lines.add(line);
        }
        lines.add("");
        for (String line : fontRender.listFormattedStringToWidth(nearbyLine, 140))
        {
            lines.add(line);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
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
            PokeType newType = getType(text.getText());
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

    private PokeType getType(String name)
    {
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        for (PokeType type : PokeType.values())
        {
            if (name.equalsIgnoreCase(type.name)) return type;
            if (name.equalsIgnoreCase(PokeType.getTranslatedName(type).toLowerCase(java.util.Locale.ENGLISH).trim()))
                return type;
        }
        return PokeType.unknown;
    }

}

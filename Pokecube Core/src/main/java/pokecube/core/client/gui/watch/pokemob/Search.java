package pokecube.core.client.gui.watch.pokemob;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.interfaces.IPokemob;

public class Search extends PokeInfoPage
{
    final PokemobInfoPage parent;

    public Search(PokemobInfoPage parent, IPokemob pokemob)
    {
        super(parent.watch, pokemob, "search");
        this.parent = parent;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    void drawInfo(int mouseX, int mouseY, float partialTicks)
    {

    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
    }

}

package pokecube.core.client.gui.pokemob;

import net.minecraft.client.gui.GuiScreen;
import pokecube.core.interfaces.IPokemob;

public class GuiPokemobAI extends GuiScreen
{
    final IPokemob pokemob;

    public GuiPokemobAI(IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

}

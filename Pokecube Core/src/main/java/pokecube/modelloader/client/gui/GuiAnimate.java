package pokecube.modelloader.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.custom.RenderAdvancedPokemobModel;

public class GuiAnimate extends GuiScreen
{
    int pokedexNb = 0;

    protected GuiTextField anim;
    protected GuiTextField part;
    protected GuiTextField name;
    protected GuiTextField rotOff;
    protected GuiTextField rotChange;
    protected GuiTextField posOff;
    protected GuiTextField posChange;
    protected GuiTextField length;
    protected GuiTextField start;

    List<String> components;

    @Override
    /** Adds the buttons (and other controls) to the screen in question. Called
     * when the GUI is displayed and when the window resizes, the buttonList is
     * cleared beforehand. */
    public void initGui()
    {
        int yOffset = height / 2 - 80;
        int xOffset = width / 2;

        anim = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 13, 100, 10);
        part = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 23, 100, 10);
        name = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 33, 100, 10);
        rotOff = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 43, 100, 10);
        posOff = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 53, 100, 10);
        length = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 63, 100, 10);
        start = new GuiTextField(0, fontRendererObj, xOffset, yOffset + 73, 100, 10);
        buttonList.add(new GuiButton(2, width / 2 - xOffset, height / 2 - yOffset, 40, 20, "button1"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset, height / 2 - yOffset - 20, 40, 20, "button2"));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    /** Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int yOffset = height / 2 - 80;
        int xOffset = width / 2;
        fontRendererObj.drawString("Animation", xOffset, yOffset, 0xFFFFFF);
        anim.drawTextBox();
//        part.drawTextBox();
//        name.drawTextBox();
//        rotOff.drawTextBox();
//        posOff.drawTextBox();
//        length.drawTextBox();
//        start.drawTextBox();
        PokedexEntry entry = null;
        if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
        IPokemob pokemob = EventsHandlerClient.renderMobs.get(entry);
        if (pokemob == null)
        {
            EventsHandlerClient.renderMobs.put(entry,
                    pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(entry.getPokedexNb(), mc.theWorld));
        }
        float zLevel = 800;
        GL11.glPushMatrix();
        GlStateManager.translate(150, 150, zLevel);
        double size = Math.max(1,Math.max(entry.height, Math.max(entry.width, entry.length)));
        double scale = 8 / Math.sqrt(size);
        
        GL11.glScaled(scale, scale, scale);

        Object o;
        ((Entity) pokemob).ticksExisted = mc.thePlayer.ticksExisted;
        if ((o = RenderPokemobs.getInstance().getRenderer(entry)) instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel render = (RenderAdvancedPokemobModel) o;
            render.anim = anim.getText();
            render.overrideAnim = true;
        }
        EventsHandlerClient.renderMob(pokemob, partialTicks);
        if ((o = RenderPokemobs.getInstance().getRenderer(entry)) instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel render = (RenderAdvancedPokemobModel) o;
            render.anim = "";
            render.overrideAnim = false;
        }
        GL11.glPopMatrix();
    }

    @Override
    /** Returns true if this GUI should pause the game when it is displayed in
     * single-player */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    /** Fired when a key is typed (except F11 which toggles full screen). This
     * is the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
     * (character on the key), keyCode (lwjgl Keyboard key code) */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        anim.textboxKeyTyped(typedChar, keyCode);
//        part.textboxKeyTyped(typedChar, keyCode);
//        name.textboxKeyTyped(typedChar, keyCode);
//        rotOff.textboxKeyTyped(typedChar, keyCode);
//        posOff.textboxKeyTyped(typedChar, keyCode);
//        length.textboxKeyTyped(typedChar, keyCode);
//        start.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    /** Called when the mouse is clicked. Args : mouseX, mouseY,
     * clickedButton */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        anim.mouseClicked(mouseX, mouseY, mouseButton);
//        part.mouseClicked(mouseX, mouseY, mouseButton);
//        name.mouseClicked(mouseX, mouseY, mouseButton);
//        rotOff.mouseClicked(mouseX, mouseY, mouseButton);
//        posOff.mouseClicked(mouseX, mouseY, mouseButton);
//        length.mouseClicked(mouseX, mouseY, mouseButton);
//        start.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    /** Called by the controls from the buttonList when activated. (Mouse
     * pressed for buttons) */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        System.out.println(button);
        if (button.id == 2)
        {
            PokedexEntry entry = null;
            if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
            int num = Pokedex.getInstance().getNext(entry, 1).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = Pokedex.getInstance().getFirstEntry().getPokedexNb();
        }
        else
        {
            PokedexEntry entry = null;
            if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
            int num = Pokedex.getInstance().getPrevious(entry, 1).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = Pokedex.getInstance().getLastEntry().getPokedexNb();
        }
    }

    @Override
    /** Called from the main game loop to update the screen. */
    public void updateScreen()
    {
    }
}

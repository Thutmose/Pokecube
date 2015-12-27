package pokecube.modelloader.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.ClientProxy;
import pokecube.modelloader.client.custom.RenderAdvancedPokemobModel;
import pokecube.modelloader.client.custom.animation.TextureHelper;

public class GuiAnimate extends GuiScreen
{
    int pokedexNb = 0;

    protected GuiTextField anim;
    protected GuiTextField state;

    private float xRenderAngle     = 0;
    private float yHeadRenderAngle = 0;
    private float xHeadRenderAngle = 0;
    private int   mouseRotateControl;
    int           prevX            = 0;
    int           prevY            = 0;
    float         scale            = 1;
    int[]         shift            = { 0, 0 };

    GuiButton groundButton;
    boolean   ground = true;

    List<String> components;

    @Override
    /** Adds the buttons (and other controls) to the screen in question. Called
     * when the GUI is displayed and when the window resizes, the buttonList is
     * cleared beforehand. */
    public void initGui()
    {
        int yOffset = height / 2;
        int xOffset = width / 2;

        anim = new GuiTextField(0, fontRendererObj, width - 101, yOffset + 13 - yOffset / 2, 100, 10);
        anim.setText("idle");
        state = new GuiTextField(0, fontRendererObj, width - 101, yOffset + 43 - yOffset / 2, 100, 10);
        yOffset += 0;
        buttonList.add(new GuiButton(2, width / 2 - xOffset, yOffset, 40, 20, "next"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset, yOffset - 20, 40, 20, "prev"));
        buttonList.add(groundButton = new GuiButton(3, width / 2 - xOffset, yOffset - 40, 40, 20, "ground"));
        buttonList.add(new GuiButton(4, width / 2 - xOffset, yOffset + 80, 40, 20, "F5"));
        buttonList.add(new GuiButton(5, width / 2 - xOffset, yOffset + 20, 40, 20, "Reset"));
        buttonList.add(new GuiButton(6, width / 2 - xOffset + 20, yOffset - 60, 20, 20, "+"));
        buttonList.add(new GuiButton(7, width / 2 - xOffset, yOffset - 60, 20, 20, "-"));
        buttonList.add(new GuiButton(8, width / 2 - xOffset + 20, yOffset - 80, 20, 20, "\u25b6"));
        buttonList.add(new GuiButton(9, width / 2 - xOffset, yOffset - 80, 20, 20, "\u25c0"));
        buttonList.add(new GuiButton(10, width / 2 - xOffset + 20, yOffset - 100, 20, 20, "\u25bc"));
        buttonList.add(new GuiButton(11, width / 2 - xOffset, yOffset - 100, 20, 20, "\u25b2"));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    /** Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int yOffset = height / 2;
        int xOffset = width / 2;
        fontRendererObj.drawString("Animation", width - 101, yOffset - yOffset / 2, 0xFFFFFF);
        fontRendererObj.drawString("State", width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        anim.drawTextBox();
        state.drawTextBox();
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
        GlStateManager.translate(xOffset + shift[0], yOffset + shift[1], zLevel);
        double size = Math.max(1, Math.max(entry.height, Math.max(entry.width, entry.length)));
        double scale = 8 * this.scale / Math.sqrt(size);

        GL11.glScaled(scale, scale, scale);

        Object o;
        String tex = state.getText().trim();
        if (!tex.isEmpty() && !state.isFocused())
        {
            int state = 0;
            try
            {
                state = Integer.parseInt(tex);
            }
            catch (NumberFormatException e)
            {
                state = TextureHelper.getState(tex);
            }
            if (state >= 0)
            {
                for (int i = 1; i < 32; i++)
                    pokemob.setPokemonAIState(1 << i, false);
                pokemob.setPokemonAIState(state, true);
            }
        }
        else
        {
            for (int i = 1; i < 31; i++)
                pokemob.setPokemonAIState(1 << i, 1 << i == IMoveConstants.SITTING ? true : false);
        }

        EntityLiving entity = (EntityLiving) pokemob;
        entity.renderYawOffset = 0F;
        entity.rotationYaw = xRenderAngle;
        entity.rotationPitch = xHeadRenderAngle;
        if (isAltKeyDown()) yHeadRenderAngle = xRenderAngle;
        entity.rotationYawHead = yHeadRenderAngle;

        entity.onGround = ground;

        ((Entity) pokemob).ticksExisted = mc.thePlayer.ticksExisted;
        if ((o = RenderPokemobs.getInstance().getRenderer(entry)) instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel render = (RenderAdvancedPokemobModel) o;
            render.anim = anim.getText();
            render.overrideAnim = true;
        }
        EventsHandlerClient.renderMob(pokemob, partialTicks, false);
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
        state.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    /** Called when the mouse is clicked. Args : mouseX, mouseY,
     * clickedButton */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        anim.mouseClicked(mouseX, mouseY, mouseButton);
        state.mouseClicked(mouseX, mouseY, mouseButton);
        int xConv = mouseX - ((width));
        boolean view = false;

        view = xConv < -101 && xConv > -width + 40;

        if (view)
        {
            mouseRotateControl = mouseButton;
            prevX = mouseX;
            prevY = mouseY;
        }
    }

    @Override
    /** Called by the controls from the buttonList when activated. (Mouse
     * pressed for buttons) */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 2)
        {
            PokedexEntry entry = null;
            if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
            int num = Pokedex.getInstance().getNext(entry, 1).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = Pokedex.getInstance().getFirstEntry().getPokedexNb();
        }
        else if (button.id == 1)
        {
            PokedexEntry entry = null;
            if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
            int num = Pokedex.getInstance().getPrevious(entry, 1).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = Pokedex.getInstance().getLastEntry().getPokedexNb();
        }
        else if (button.id == 3)
        {
            ground = !ground;
            groundButton.displayString = ground ? "ground" : "float";
        }
        else if (button.id == 4)
        {
            Database.updateSizes();
            ClientProxy.populateModels();
        }
        else if (button.id == 5)
        {
            xRenderAngle = 0;
            yHeadRenderAngle = 0;
            xHeadRenderAngle = 0;
            scale = 1;
            shift[0] = 0;
            shift[1] = 0;
        }
        else if (button.id == 6)
        {
            scale += 0.1;
        }
        else if (button.id == 7)
        {
            scale -= 0.1;
        }
        else if (button.id == 8)
        {
            shift[0] += isShiftKeyDown()?10:1;
        }
        else if (button.id == 9)
        {
            shift[0] -= isShiftKeyDown()?10:1;
        }
        else if (button.id == 10)
        {
            shift[1] += isShiftKeyDown()?10:1;
        }
        else if (button.id == 11)
        {
            shift[1] -= isShiftKeyDown()?10:1;
        }
    }

    private void handleMouseMove(int x, int y, int mouseButton)
    {
        if (mouseButton != -1)
        {
            mouseRotateControl = -1;
        }

        if (mouseRotateControl == 0)
        {
            xRenderAngle += prevX - x;
            prevX = x;
            prevY = y;
        }
        else if (mouseRotateControl == 1)
        {
            yHeadRenderAngle += (prevX - x);
            prevX = x;
            xHeadRenderAngle += y - prevY;
            prevY = y;
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        this.handleMouseMove(x, y, Mouse.getEventButton());
        super.handleMouseInput();
    }

    @Override
    /** Called from the main game loop to update the screen. */
    public void updateScreen()
    {
    }
}

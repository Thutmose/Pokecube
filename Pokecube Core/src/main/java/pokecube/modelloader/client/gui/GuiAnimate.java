package pokecube.modelloader.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.entity.RenderAdvancedPokemobModel;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.ClientProxy;

public class GuiAnimate extends GuiScreen
{
    static String          mob              = "";

    int                    pokedexNb        = 0;
    protected GuiTextField anim;
    protected GuiTextField state;
    protected GuiTextField forme;

    protected GuiTextField info;
    private float          xRenderAngle     = 0;
    private float          yRenderAngle     = 0;
    private float          yHeadRenderAngle = 0;
    private float          xHeadRenderAngle = 0;
    private int            mouseRotateControl;
    int                    prevX            = 0;
    int                    prevY            = 0;
    float                  scale            = 1;

    int[]                  shift            = { 0, 0 };
    GuiButton              groundButton;
    boolean                ground           = true;

    boolean                shiny            = false;

    List<String>           components;

    @Override
    /** Called by the controls from the buttonList when activated. (Mouse
     * pressed for buttons) */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        PokedexEntry entry = null;
        if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
        String[] gender = buttonList.get(12).displayString.split(":");
        byte sexe = IPokemob.NOSEXE;
        if (gender[1].equalsIgnoreCase("m"))
        {
            sexe = IPokemob.MALE;
        }
        else if (gender[1].equalsIgnoreCase("f"))
        {
            sexe = IPokemob.FEMALE;

        }
        if (button.id == 2)
        {
            int num = (entry = Pokedex.getInstance().getNext(entry, 1)).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = (entry = Pokedex.getInstance().getFirstEntry()).getPokedexNb();
            mob = entry.getForGender(sexe).getName();
        }
        else if (button.id == 1)
        {
            int num = (entry = Pokedex.getInstance().getPrevious(entry, 1)).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            else pokedexNb = (entry = Pokedex.getInstance().getLastEntry()).getPokedexNb();
            mob = entry.getForGender(sexe).getName();
        }
        else if (button.id == 3)
        {
            ground = !ground;
            groundButton.displayString = ground ? "ground" : "float";
        }
        else if (button.id == 4)
        {
            if (entry.getBaseForme() != null)
            {
                entry = entry.getBaseForme();
            }
            for (PokedexEntry e : Database.getFormes(entry))
            {
                ((ClientProxy) ModPokecubeML.proxy).reloadModel(e);
            }
            if (Database.getFormes(entry).isEmpty()) ((ClientProxy) ModPokecubeML.proxy).reloadModel(entry);
        }
        else if (button.id == 5)
        {
            xRenderAngle = 0;
            yRenderAngle = 0;
            yHeadRenderAngle = 0;
            xHeadRenderAngle = 0;
            scale = 1;
            shift[0] = 0;
            shift[1] = 0;
        }
        else if (button.id == 6)
        {
            scale += isShiftKeyDown() ? 1 : 0.1;
        }
        else if (button.id == 7)
        {
            scale -= isShiftKeyDown() ? 1 : 0.1;
        }
        else if (button.id == 8)
        {
            shift[0] += isShiftKeyDown() ? 10 : 1;
        }
        else if (button.id == 9)
        {
            shift[0] -= isShiftKeyDown() ? 10 : 1;
        }
        else if (button.id == 10)
        {
            shift[1] += isShiftKeyDown() ? 10 : 1;
        }
        else if (button.id == 11)
        {
            shift[1] -= isShiftKeyDown() ? 10 : 1;
        }
        else if (button.id == 12)
        {
            shiny = !shiny;
            button.displayString = shiny ? "shiny" : "normal";
        }
        else if (button.id == 13)
        {
            entry = Database.getEntry(pokedexNb);
        }
        else if (button.id == 14)
        {
            entry = Database.getEntry(mob);
            if (entry != null)
            {
                List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(entry));
                Collections.sort(formes, new Comparator<PokedexEntry>()
                {
                    @Override
                    public int compare(PokedexEntry o1, PokedexEntry o2)
                    {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (int i = 0; i < formes.size(); i++)
                {
                    if (formes.get(i) == entry)
                    {
                        entry = (i + 1 < formes.size()) ? formes.get(i + 1) : formes.get(0);
                        mob = entry.getName();
                        break;
                    }
                }
            }
        }
        else if (button.id == 15)
        {
            entry = Database.getEntry(mob);
            if (entry != null)
            {
                List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(entry));
                Collections.sort(formes, new Comparator<PokedexEntry>()
                {
                    @Override
                    public int compare(PokedexEntry o1, PokedexEntry o2)
                    {
                        return o2.getName().compareTo(o1.getName());
                    }
                });
                for (int i = 0; i < formes.size(); i++)
                {
                    if (formes.get(i) == entry)
                    {
                        entry = (i + 1 < formes.size()) ? formes.get(i + 1) : formes.get(0);
                        mob = entry.getName();
                        break;
                    }
                }
            }
        }
        if (entry != null)
        {
            IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
            if (pokemob == null) return;
            pokemob.onGenesChanged();
            PacketPokedex.updateWatchEntry(entry);
            if (button.id == 13)
            {
                if (pokemob.getSexe() == IPokemob.MALE)
                {
                    entry = pokemob.getEvolutionEntry().getForGender((IPokemob.FEMALE));
                    button.displayString = "sexe:F";
                }
                else if (pokemob.getSexe() == IPokemob.FEMALE)
                {
                    entry = pokemob.getEvolutionEntry().getForGender((IPokemob.MALE));
                    button.displayString = "sexe:M";
                }
                mob = entry.getName();
            }
            else
            {
            }
            forme.setText(entry.getName());
            info.setText("" + pokemob.getSpecialInfo());

        }
    }

    @Override
    /** Returns true if this GUI should pause the game when it is displayed in
     * single-player */
    public boolean doesGuiPauseGame()
    {
        return false;
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
        fontRenderer.drawString("Animation", width - 101, yOffset - yOffset / 2, 0xFFFFFF);
        fontRenderer.drawString("State       Info:", width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        fontRenderer.drawString("Forme", width - 101, yOffset + 60 - yOffset / 2, 0xFFFFFF);
        anim.drawTextBox();
        state.drawTextBox();
        forme.drawTextBox();
        info.drawTextBox();
        PokedexEntry entry = null;
        if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
        IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
        if (pokemob != null)
        {
            pokemob.specificSpawnInit();
            pokemob.setShiny(shiny);
        }
        else
        {
            return;
        }
        String form = mob;
        PokedexEntry e1 = Database.getEntry(form);
        PokedexEntry e0 = pokemob.getPokedexEntry();
        if (e0.getPokedexNb() == e1.getPokedexNb())
        {
            pokemob.setPokedexEntry(e1);
        }
        else if (!e0.base && e0.getBaseForme().hasForm(form))
        {
            pokemob.setPokedexEntry(e1);
        }
        else if (e1 != null && e1 != entry)
        {
            entry = e1;
            pokedexNb = entry.getPokedexNb();
            pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
            if (pokemob == null) return;
        }
        String[] gender = buttonList.get(12).displayString.split(":");
        if (gender[1].equalsIgnoreCase("m") && pokemob.getSexe() == IPokemob.FEMALE)
        {
            pokemob.setSexe(IPokemob.MALE);
        }
        else if (gender[1].equalsIgnoreCase("f") && pokemob.getSexe() == IPokemob.MALE)
        {
            pokemob.setSexe(IPokemob.FEMALE);
        }
        entry = pokemob.getPokedexEntry();
        mob = entry.getName();
        fontRenderer.drawString(pokemob.getPokemonDisplayName().getFormattedText(), xOffset, 10, 0xFFFFFF);
        float zLevel = 800;
        GL11.glPushMatrix();
        GlStateManager.translate(xOffset + shift[0], yOffset + shift[1], zLevel);
        double scale = 5;

        GL11.glScaled(scale, scale, scale);

        String tex = state.getText().trim();

        if (!tex.isEmpty() && !state.isFocused())
        {
            // TODO per state rendering for this.
        }

        EntityLiving entity = pokemob.getEntity();
        entity.renderYawOffset = 0F;
        entity.prevRenderYawOffset = 0F;
        entity.rotationYaw = 0;
        entity.prevRotationPitch = xHeadRenderAngle;
        entity.rotationPitch = xHeadRenderAngle;
        entity.rotationYawHead = yHeadRenderAngle;
        entity.prevRotationYawHead = yHeadRenderAngle;
        GL11.glRotated(-xRenderAngle, 0, 1, 0);
        if (isAltKeyDown()) GL11.glRotated(yRenderAngle, 1, 0, 0);
        entity.onGround = ground;

        entity.ticksExisted = mc.player.ticksExisted;
        entity.prevLimbSwingAmount = entity.limbSwingAmount % 1f;
        entity.limbSwingAmount = entity.prevLimbSwingAmount + 0.01f;
        entity.limbSwing += 0.1f;

        String arg = info.getText();
        try
        {
            int num = Integer.parseInt(arg);
            pokemob.setSpecialInfo(num);
        }
        catch (NumberFormatException e)
        {

        }
        Render<Entity> rend = null;
        try
        {
            rend = Minecraft.getMinecraft().getRenderManager().<Entity> getEntityRenderObject(pokemob.getEntity());
        }
        catch (Exception e)
        {

        }
        if (rend instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel render = (RenderAdvancedPokemobModel) rend;
            render.anim = anim.getText();
            render.overrideAnim = true;
        }
        EventsHandlerClient.renderMob(pokemob, partialTicks, false);
        if (rend instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel render = (RenderAdvancedPokemobModel) rend;
            render.anim = "";
            render.overrideAnim = false;
        }
        GL11.glPopMatrix();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        this.handleMouseMove(x, y, Mouse.getEventButton());
        super.handleMouseInput();
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
            yRenderAngle += prevY - y;
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
    /** Adds the buttons (and other controls) to the screen in question. Called
     * when the GUI is displayed and when the window resizes, the buttonList is
     * cleared beforehand. */
    public void initGui()
    {
        super.initGui();
        int yOffset = height / 2;
        int xOffset = width / 2;
        pokedexNb = Pokedex.getInstance().getFirstEntry().getPokedexNb();
        anim = new GuiTextField(0, fontRenderer, width - 101, yOffset + 13 - yOffset / 2, 100, 10);
        anim.setText("idle");
        state = new GuiTextField(0, fontRenderer, width - 101, yOffset + 43 - yOffset / 2, 100, 10);
        forme = new GuiTextField(0, fontRenderer, width - 101, yOffset + 73 - yOffset / 2, 100, 10);
        mob = PokecubePlayerDataHandler.getCustomDataTag(mc.player).getString("WEntry");
        PokedexEntry entry = Database.getEntry(mob);
        if (entry == null) mob = Pokedex.getInstance().getFirstEntry().getName();
        else mob = entry.getName();
        forme.setText(mob);
        info = new GuiTextField(0, fontRenderer, width - 21, yOffset + 28 - yOffset / 2, 20, 10);
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
        buttonList.add(new GuiButton(12, width / 2 - xOffset, yOffset + 40, 40, 20, "normal"));
        buttonList.add(new GuiButton(13, width / 2 - xOffset, yOffset + 60, 40, 20, "sexe:M"));

        buttonList.add(new GuiButton(14, width - 101 + 20, yOffset + 85 - yOffset / 2, 20, 20, "\u25b6"));
        buttonList.add(new GuiButton(15, width - 101, yOffset + 85 - yOffset / 2, 20, 20, "\u25c0"));
    }

    @Override
    /** Fired when a key is typed (except F11 which toggles full screen). This
     * is the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
     * (character on the key), keyCode (lwjgl Keyboard key code) */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        boolean hit = anim.textboxKeyTyped(typedChar, keyCode);
        hit = hit || state.textboxKeyTyped(typedChar, keyCode);
        hit = hit || forme.textboxKeyTyped(typedChar, keyCode);
        hit = hit || info.textboxKeyTyped(typedChar, keyCode);

        if (!hit && keyCode == 205)
        {
            PokedexEntry entry = null;
            if ((entry = Database.getEntry(pokedexNb)) == null) entry = Pokedex.getInstance().getFirstEntry();
            int num = (entry = Pokedex.getInstance().getNext(entry, 1)).getPokedexNb();
            if (num != pokedexNb) pokedexNb = num;
            IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
            if (pokemob == null) return;
            forme.setText(pokemob.getPokedexEntry().getName());
            mob = forme.getText();
            info.setText("" + pokemob.getSpecialInfo());
        }
        if (forme.isFocused() && typedChar == 13)
        {
            PokedexEntry entry = Database.getEntry(forme.getText());
            if (entry == null)
            {
                entry = Database.getEntry(pokedexNb);
            }
            if (entry != null)
            {
                IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
                if (pokemob == null) pokemob = EventsHandlerClient.getRenderMob(Pokedex.getInstance().getFirstEntry(),
                        PokecubeCore.proxy.getWorld());
                forme.setText(pokemob.getPokedexEntry().getName());
                mob = forme.getText();
                info.setText("" + pokemob.getSpecialInfo());
            }
        }
    }

    @Override
    /** Called when the mouse is clicked. Args : mouseX, mouseY,
     * clickedButton */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        anim.mouseClicked(mouseX, mouseY, mouseButton);
        state.mouseClicked(mouseX, mouseY, mouseButton);
        forme.mouseClicked(mouseX, mouseY, mouseButton);
        info.mouseClicked(mouseX, mouseY, mouseButton);
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
    /** Called from the main game loop to update the screen. */
    public void updateScreen()
    {
    }
}

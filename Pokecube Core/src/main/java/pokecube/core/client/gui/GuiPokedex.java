/**
 *
 */
package pokecube.core.client.gui;

import static pokecube.core.utils.PokeType.getTranslatedName;

import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;

public class GuiPokedex extends GuiScreen
{

    public static PokedexEntry pokedexEntry     = null;
    public IPokemob            pokemob          = null;
    protected EntityPlayer     entityPlayer     = null;
    protected ScrollGui        list;
    protected GuiTextField     pokemobTextField;
    /** The X size of the inventory window in pixels. */
    protected int              xSize;
    /** The Y size of the inventory window in pixels. */
    protected int              ySize;

    private float              xRenderAngle     = 0;
    private float              yHeadRenderAngle = 10;
    private float              xHeadRenderAngle = 0;
    private int                mouseRotateControl;

    int                        prevX            = 0;
    int                        prevY            = 0;

    /**
     *
     */
    public GuiPokedex(IPokemob pokemob, EntityPlayer entityPlayer)
    {
        xSize = 256;
        ySize = 197;
        this.pokemob = pokemob;
        this.entityPlayer = entityPlayer;

        if (pokemob != null)
        {
            pokedexEntry = pokemob.getPokedexEntry();
        }
        else
        {
            String name = PokecubePlayerDataHandler.getCustomDataTag(entityPlayer).getString("WEntry");
            pokedexEntry = Database.getEntry(name);
            if (pokedexEntry == null)
            {
                pokedexEntry = Pokedex.getInstance().getFirstEntry();
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick)
    {
        super.drawScreen(mouseX, mouseY, partialTick);
        // Draw background
        Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        minecraft.renderEngine.bindTexture(Resources.GUI_POKEDEX);
        int j2 = (width - xSize) / 2;
        int k2 = (height - ySize) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, xSize, ySize);

        // Draw description
        list.drawScreen(mouseX, mouseY, partialTick);

        // Draw mob
        GL11.glPushMatrix();
        IPokemob renderMob = EventsHandlerClient.getRenderMob(pokedexEntry, entityPlayer.getEntityWorld());
        if (!renderMob.getEntity().addedToChunk)
        {
            EntityTools.copyEntityTransforms(renderMob.getEntity(), entityPlayer);
        }
        GlStateManager.enableDepth();
        renderMob(renderMob.getEntity(), mc, 0, 0, 1f, height, width, xSize, ySize, xHeadRenderAngle, yHeadRenderAngle,
                xRenderAngle);
        GL11.glPopMatrix();

        // Draw info about mob
        GL11.glPushMatrix();
        int yOffset = height / 2 - 80;
        int xOffset = width / 2;
        int length = fontRenderer.getStringWidth(pokemobTextField.getText()) / 2;
        pokemobTextField.x -= length;
        pokemobTextField.drawTextBox();
        pokemobTextField.x += length;
        int nb = pokedexEntry != null ? pokedexEntry.getPokedexNb() : 0;
        PokeType type1 = pokemob != null && pokedexEntry == pokemob.getPokedexEntry() ? pokemob.getType1()
                : pokedexEntry != null ? pokedexEntry.getType1() : PokeType.unknown;
        PokeType type2 = pokemob != null && pokedexEntry == pokemob.getPokedexEntry() ? pokemob.getType2()
                : pokedexEntry != null ? pokedexEntry.getType2() : PokeType.unknown;
        drawCenteredString(fontRenderer, "#" + nb, xOffset - 28, yOffset + 02, 0xffffff);
        try
        {
            drawCenteredString(fontRenderer, getTranslatedName(type1), xOffset - 88, yOffset + 137, type1.colour);
            drawCenteredString(fontRenderer, getTranslatedName(type2), xOffset - 44, yOffset + 137, type2.colour);
        }
        catch (Exception e)
        {
        }
        GL11.glPopMatrix();
    }

    private int getButtonId(int x, int y)
    {
        int xConv = x - ((width - xSize) / 2) - 74;
        int yConv = y - ((height - ySize) / 2) - 107;
        int button = 0;

        if (xConv >= 37 && xConv <= 42 && yConv >= 63 && yConv <= 67)
        {
            button = 1;// Next
        }
        else if (xConv >= 25 && xConv <= 30 && yConv >= 63 && yConv <= 67)
        {
            button = 2;// Previous
        }
        else if (xConv >= 32 && xConv <= 36 && yConv >= 58 && yConv <= 63)
        {
            button = 3;// Next 10
        }
        else if (xConv >= 32 && xConv <= 36 && yConv >= 69 && yConv <= 73)
        {
            button = 4;// Previous 10
        }
        else if (xConv >= -65 && xConv <= -58 && yConv >= 65 && yConv <= 72)
        {
            button = 5;// Sound
        }
        else if (xConv >= -55 && xConv <= 30 && yConv >= -60 && yConv <= 15)
        {
            button = 10;// Rotate Mouse control
        }
        return button;
    }

    public void handleGuiButton(int button)
    {
        if (button == 1)
        {
            pokedexEntry = Pokedex.getInstance().getNext(pokedexEntry, 1);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            initList();
            PacketPokedex.updateWatchEntry(pokedexEntry);
        }
        else if (button == 2)
        {
            pokedexEntry = Pokedex.getInstance().getPrevious(pokedexEntry, 1);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            initList();
            PacketPokedex.updateWatchEntry(pokedexEntry);
        }
        else if (button == 3)
        {
            pokedexEntry = Pokedex.getInstance().getNext(pokedexEntry, 10);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            initList();
            PacketPokedex.updateWatchEntry(pokedexEntry);
        }
        else if (button == 4)
        {
            pokedexEntry = Pokedex.getInstance().getPrevious(pokedexEntry, 10);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            initList();
            PacketPokedex.updateWatchEntry(pokedexEntry);
        }
        if (button >= 1 && button <= 5 || button == 5)
        {
            float volume = 0.2F;

            if (button == 5)
            {
                volume = 1F;
            }
            mc.player.playSound(pokedexEntry.getSoundEvent(), volume, 1.0F);
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        this.handleMouseMove(x, y, Mouse.getEventButton());
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    private void handleMouseMove(int x, int y, int mouseButton)
    {
        // System.out.println("handleMouseMove("+x+", "+y+",
        // "+mouseButton+")");
        if (mouseButton != -1)
        {
            mouseRotateControl = -1;
        }

        if (mouseRotateControl == 0)
        {
            xRenderAngle -= (x - prevX) * 5;
            prevX = x;
            prevY = y;
        }

        if (mouseRotateControl == 1)
        {
            yHeadRenderAngle += (prevX - x) * 2;
            prevX = x;
            xHeadRenderAngle += y - prevY;
            prevY = y;

            if (xHeadRenderAngle > 20)
            {
                xHeadRenderAngle = 20;
            }

            if (xHeadRenderAngle < -30)
            {
                xHeadRenderAngle = -30;
            }

            if (yHeadRenderAngle > 40)
            {
                yHeadRenderAngle = 40;
            }

            if (yHeadRenderAngle < -40)
            {
                yHeadRenderAngle = -40;
            }
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        buttonList.clear();
        int yOffset = height / 2 - 80;
        int xOffset = width / 2;

        pokemobTextField = new GuiTextField(0, fontRenderer, xOffset - 65, yOffset + 123, 110, 10);
        pokemobTextField.setEnableBackgroundDrawing(false);
        pokemobTextField.setFocused(false);
        pokemobTextField.setEnabled(true);

        if (pokedexEntry != null)
        {
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
        }
        initList();
    }

    private void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (width - 160) / 2 + 92;
        int offsetY = (height - 160) / 2 + 15;
        int height = 140;
        ITextComponent line;
        ITextComponent page = pokedexEntry.getDescription();// ITextComponent.Serializer.jsonToComponent(bookPages.getStringTagAt(i));
        List<ITextComponent> list = GuiUtilRenderComponents.splitText(page, 100, fontRenderer, true, true);
        for (int j = 0; j < list.size(); j++)
        {
            line = list.get(j);
            if (j < list.size() - 1 && line.getUnformattedText().trim().isEmpty())
            {
                for (int l = j; l < list.size(); l++)
                {
                    if (!list.get(l).getUnformattedText().trim().isEmpty() || (l == list.size() - 1))
                    {
                        if (j < l - 1) j = l - 1;
                        break;
                    }
                }
            }
            entries.add(new LineEntry(offsetY + 4, offsetY + height + 4, fontRenderer, line, 0xFFFFFF));
        }
        this.list = new ScrollGui(mc, 107, height, fontRenderer.FONT_HEIGHT + 2, offsetX, offsetY, entries);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        list.actionPerformed(button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        list.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        if (isAltKeyDown()) return;
        boolean b2 = pokemobTextField.textboxKeyTyped(par1, par2) || pokemobTextField.isFocused();
        if (b2)
        {
            if ((par2 == Keyboard.KEY_RETURN))
            {
                PokedexEntry entry = Database.getEntry(pokemobTextField.getText());
                if (entry == null)
                {
                    for (PokedexEntry e : Database.getSortedFormes())
                    {
                        String translated = I18n.format(e.getUnlocalizedName());
                        if (translated.equalsIgnoreCase(pokemobTextField.getText()))
                        {
                            Database.data2.put(translated, e);
                            entry = e;
                            break;
                        }
                    }
                    // If the pokedex entry is not actually registered, use old
                    // entry.
                    if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
                }
                if (entry != null)
                {
                    pokedexEntry = entry;
                    initList();
                    PacketPokedex.updateWatchEntry(pokedexEntry);
                }
                else
                {
                    pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
                }
            }
        }
        else if (par2 == Keyboard.KEY_UP)
        {
            handleGuiButton(3);
        }
        else if (par2 == Keyboard.KEY_DOWN)
        {
            handleGuiButton(4);
        }
        if (!b2) super.keyTyped(par1, par2);
    }

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
        pokemobTextField.mouseClicked(x, y, mouseButton);
        list.mouseClicked(x, y, mouseButton);
        // System.out.println(mouseButton);
        int button = getButtonId(x, y);

        if (button != 0)
        {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (button == 14)
        {
            PacketPokedex.sendInspectPacket(true, FMLClientHandler.instance().getCurrentLanguage());
            return;
        }

        if (button == 10)
        {
            mouseRotateControl = mouseButton;
            prevX = x;
            prevY = y;
        }
        else
        {
            handleGuiButton(button);
        }
    }

    public static void renderMob(EntityLiving entity, Minecraft mc, int dx, int dy, float scale, int height, int width,
            int xSize, int ySize, float xHeadRenderAngle, float yHeadRenderAngle, float yaw)
    {
        try
        {

            float size = 0;
            int j = 0;
            int k = 0;

            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob == null) { return; }
            PokedexEntry pokedexEntry = pokemob.getPokedexEntry();
            PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance()
                    .getPlayerData(Minecraft.getMinecraft().player).getData(PokecubePlayerStats.class);
            if ((StatsCollector.getCaptured(pokedexEntry, Minecraft.getMinecraft().player) > 0
                    || StatsCollector.getHatched(pokedexEntry, Minecraft.getMinecraft().player) > 0)
                    || mc.player.capabilities.isCreativeMode)
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);
            }
            else if (stats.hasInspected(pokedexEntry))
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(127, 127, 127, 255);
            }
            else
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(15, 15, 15, 255);
            }

            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);

            float mobScale = pokemob.getSize();
            Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            j = (width - xSize) / 2 + dx;
            k = (height - ySize) / 2 + dy;

            GL11.glPushMatrix();
            GL11.glTranslatef(j + 60, k + 100, 50F);
            float zoom = (float) (25F / size) * scale;
            GL11.glScalef(zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.prevRenderYawOffset = yaw;
            entity.renderYawOffset = yaw;
            entity.rotationYaw = yaw;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = yHeadRenderAngle;
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.prevRotationPitch = entity.rotationPitch;

            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.prevLimbSwingAmount = 0;
            PokeType flying = PokeType.getType("flying");
            entity.onGround = !pokemob.isType(flying);

            if (isAltKeyDown())
            {
                entity.onGround = true;
                entity.limbSwingAmount = 0.05f;
                entity.prevLimbSwingAmount = entity.limbSwingAmount - 0.5f;
            }
            int i = 15728880;
            int j2 = i % 65536;
            int k2 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j2 / 1.0F, k2 / 1.0F);

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, 0, 0, 0, 0, false);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);

            GL11.glPopMatrix();

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}

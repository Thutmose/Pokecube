package pokecube.core.client.gui;

import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.render.entity.RenderAdvancedPokemobModel;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.api.entity.IHungrymob;

public class GuiPokemob extends GuiContainer
{
    public static class NameField extends GuiTextField
    {
        private final FontRenderer fontRendererInstance;

        public NameField(int componentId, FontRenderer fontrenderer, int x, int y, int par5Width, int par6Height)
        {
            super(componentId, fontrenderer, x, y, par5Width, par6Height);
            this.fontRendererInstance = fontrenderer;
        }

        @Override
        public void drawTextBox()
        {
            int i = 4210752;
            int j = this.getCursorPosition();
            int k = this.getSelectionEnd();
            String s = this.fontRendererInstance.trimStringToWidth(this.getText(), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && flag;
            int l = this.x;
            int i1 = this.y;
            int j1 = l;

            if (k > s.length())
            {
                k = s.length();
            }

            if (!s.isEmpty())
            {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = this.fontRendererInstance.drawString(s1, (float) l, (float) i1, i, false);
            }

            boolean flag2 = this.getCursorPosition() < this.getText().length()
                    || this.getText().length() >= this.getMaxStringLength();
            int k1 = j1;

            if (!flag)
            {
                k1 = j > 0 ? l + this.width : l;
            }
            else if (flag2)
            {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length())
            {
                j1 = this.fontRendererInstance.drawString(s.substring(j), (float) j1, (float) i1, i, false);
            }

            if (flag1)
            {
                if (flag2)
                {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT, -3092272);
                }
                else
                {
                    this.fontRendererInstance.drawString("_", (float) k1, (float) i1, i, false);
                }
            }

            if (k != j)
            {
                int l1 = l + this.fontRendererInstance.getStringWidth(s.substring(0, k));
                this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT);
            }
        }

        /** Draws the current selection and a vertical line cursor in the text
         * box. */
        private void drawCursorVertical(int startX, int startY, int endX, int endY)
        {
            if (startX < endX)
            {
                int i = startX;
                startX = endX;
                endX = i;
            }

            if (startY < endY)
            {
                int j = startY;
                startY = endY;
                endY = j;
            }

            if (endX > this.x + this.width)
            {
                endX = this.x + this.width;
            }

            if (startX > this.x + this.width)
            {
                startX = this.x + this.width;
            }

            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableColorLogic();
            GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
            tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION);
            tessellator.getBuffer().pos((double) startX, (double) endY, 0.0D).endVertex();
            tessellator.getBuffer().pos((double) endX, (double) endY, 0.0D).endVertex();
            tessellator.getBuffer().pos((double) endX, (double) startY, 0.0D).endVertex();
            tessellator.getBuffer().pos((double) startX, (double) startY, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.disableColorLogic();
            GlStateManager.enableTexture2D();
        }

    }

    public static class PokemobButton extends GuiButton
    {
        final IPokemob pokemob;

        public PokemobButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IPokemob pokemob)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
            this.pokemob = pokemob;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float f)
        {
            super.drawButton(mc, mouseX, mouseY, f);
            if (id == StanceHandler.BUTTONTOGGLESTAY || id == StanceHandler.BUTTONTOGGLESIT)
            {
                PokedexEntry entry = Database.getEntry("eevee");
                IPokemob renderMob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
                if (renderMob == null)
                {
                    // No Eevee found
                    ResourceLocation texture;
                    if (id == StanceHandler.BUTTONTOGGLESTAY)
                    {
                        if (pokemob.getGeneralState(GeneralStates.STAYING))
                        {
                            texture = new ResourceLocation(PokecubeMod.ID, "textures/gui/standing.png");
                        }
                        else
                        {
                            texture = new ResourceLocation(PokecubeMod.ID, "textures/gui/walking.png");
                        }
                    }
                    else
                    {

                        if (pokemob.getLogicState(LogicStates.SITTING))
                        {
                            texture = new ResourceLocation(PokecubeMod.ID, "textures/gui/sitting.png");
                        }
                        else
                        {
                            texture = new ResourceLocation(PokecubeMod.ID, "textures/gui/standing.png");
                        }
                    }
                    mc.getTextureManager().bindTexture(texture);
                    int x = this.x + 2;
                    int y = this.y + 1;
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder vertexbuffer = tessellator.getBuffer();
                    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                    vertexbuffer.pos(x + 0, y + height - 2, this.zLevel).tex(0, 1).endVertex();
                    vertexbuffer.pos(x + width - 2, y + height - 2, this.zLevel).tex(1, 1).endVertex();
                    vertexbuffer.pos(x + width - 2, y + 0, this.zLevel).tex(1, 0).endVertex();
                    vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex(0, 0).endVertex();
                    tessellator.draw();
                    return;
                }
                renderMob.getEntity().onGround = true;

                int x = this.x + width / 2 - 2;
                int y = this.y + height / 2 + 7;
                double scale = 0.9f;
                GL11.glPushMatrix();
                GL11.glTranslatef(x, y, 10F);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glScaled(scale, scale, scale);
                GlStateManager.enableDepth();
                renderMob.getEntity().ticksExisted = mc.player.ticksExisted;
                renderMob.getEntity().renderYawOffset = 0;
                renderMob.getEntity().prevRenderYawOffset = 0;
                renderMob.getEntity().rotationYawHead = 0;
                renderMob.getEntity().rotationYawHead = 0;
                renderMob.getEntity().prevRotationYawHead = 0;
                renderMob.getEntity().rotationYaw = 0;
                renderMob.getEntity().prevRotationYaw = 0;
                renderMob.getEntity().limbSwing += 0.05;
                renderMob.getEntity().limbSwingAmount = 0.05f;
                Object o = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(renderMob.getEntity());
                if (o instanceof RenderAdvancedPokemobModel
                        || (o = RenderPokemobs.getInstance().getRenderer(entry)) instanceof RenderAdvancedPokemobModel)
                {
                    RenderAdvancedPokemobModel<?> render = (RenderAdvancedPokemobModel<?>) o;
                    if (id == StanceHandler.BUTTONTOGGLESTAY)
                    {
                        if (pokemob.getLogicState(LogicStates.SITTING))
                        {
                            render.anim = "sitting";
                        }
                        else render.anim = "idle";
                    }
                    else
                    {
                        if (pokemob.getGeneralState(GeneralStates.STAYING)) render.anim = "idle";
                        else render.anim = "walking";
                    }
                    render.overrideAnim = true;
                }
                EventsHandlerClient.renderMob(renderMob, mc.getRenderPartialTicks(), false);
                if (o instanceof RenderAdvancedPokemobModel)
                {
                    RenderAdvancedPokemobModel<?> render = (RenderAdvancedPokemobModel<?>) o;
                    render.anim = "";
                    render.overrideAnim = false;
                }
                GL11.glPopMatrix();
            }
            else
            {
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                this.drawTexturedModalRect(x + 2, y + 2,
                        mc.getTextureMapBlocks().getAtlasSprite("minecraft:items/diamond_sword"), 16, 16);
                if (!pokemob.getCombatState(CombatStates.GUARDING))
                {
                    this.drawGradientRect(x + 2, y + 2, x + width - 2, y + width - 2, 0x88884444, 0x88884444);
                }
            }
        }

        @Override
        /** Fired when the mouse button is dragged. Equivalent of
         * MouseListener.mouseDragged(MouseEvent e). */
        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
        {
        }

        @Override
        /** Fired when the mouse button is released. Equivalent of
         * MouseListener.mouseReleased(MouseEvent e). */
        public void mouseReleased(int mouseX, int mouseY)
        {
        }

        @Override
        /** Returns true if the mouse has been pressed on this control.
         * Equivalent of MouseListener.mousePressed(MouseEvent e). */
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
        {
            return this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
                    && mouseY < this.y + this.height;
        }

        @Override
        /** Whether the mouse cursor is currently over the button. */
        public boolean isMouseOver()
        {
            return this.hovered;
        }

        @Override
        public void drawButtonForegroundLayer(int mouseX, int mouseY)
        {

        }
    }

    public static void renderMob(IPokemob pokemob, int width, int height, int xSize, int ySize, float xRenderAngle,
            float yRenderAngle, float zRenderAngle, float scale)
    {
        try
        {
            EntityLiving entity = pokemob.getEntity();
            float size = 0;
            int j = width;
            int k = height;
            float mobScale = pokemob.getSize();
            Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            yRenderAngle = entity.renderYawOffset - entity.ticksExisted;
            float zoom = (25f / size) * scale;
            GL11.glPushMatrix();
            GL11.glTranslatef(j + 55, k + 60, 50F);
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(zRenderAngle, 0.0F, 0.0F, 1.0F);
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, 0, 0, 0,
                    Minecraft.getMinecraft().getRenderPartialTicks(), false);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private IInventory   playerInventory;
    private IPokemob     pokemob;
    private EntityLiving entity;
    private float        yRenderAngle = 10;
    private GuiTextField name;
    private float        xRenderAngle = 0;

    PokemobButton        stance;

    public GuiPokemob(IInventory playerInv, IPokemob pokemob)
    {
        super(new ContainerPokemob(playerInv, pokemob.getPokemobInventory(), pokemob));
        pokemob.getPokemobInventory().setCustomName(pokemob.getPokemonDisplayName().getFormattedText());
        this.playerInventory = playerInv;
        this.pokemob = pokemob;
        this.entity = pokemob.getEntity();
        this.allowUserInput = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (name.isFocused())
        {
            if (keyCode == Keyboard.KEY_ESCAPE)
            {
                name.setFocused(false);
            }
            else
            {
                name.textboxKeyTyped(typedChar, keyCode);
                if (keyCode == Keyboard.KEY_RETURN)
                {
                    pokemob.setPokemonNickname(name.getText());
                }
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);

    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton instanceof PokemobButton)
        {
            byte type = (byte) guibutton.id;
            boolean state = type == StanceHandler.BUTTONTOGGLEGUARD ? !pokemob.getCombatState(CombatStates.GUARDING)
                    : type == StanceHandler.BUTTONTOGGLESIT ? !pokemob.getLogicState(LogicStates.SITTING)
                            : !pokemob.getGeneralState(GeneralStates.STAYING);
            PacketCommand.sendCommand(pokemob, Command.STANCE, new StanceHandler(state, type));
        }
        else if (guibutton.id == 3)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.AI, entity.getEntityId());
        }
        else if (guibutton.id == 4)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, entity.getEntityId());
        }
        else if (guibutton.id == 5)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, entity.getEntityId());
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(k + 79, l + 17, 0, this.ySize, 90, 18);
        this.drawTexturedModalRect(k + 7, l + 35, 0, this.ySize + 54, 18, 18);
        yRenderAngle = -entity.rotationYaw + 45;
        xRenderAngle = 0;
        renderMob(pokemob, k, l, xSize, ySize, xRenderAngle, yRenderAngle, 0, 1);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
                : I18n.format(this.playerInventory.getName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
        IHungrymob mob = (IHungrymob) pokemob;
        float full = PokecubeMod.core.getConfig().pokemobLifeSpan / 4 + PokecubeMod.core.getConfig().pokemobLifeSpan;
        float current = -(mob.getHungerTime() - PokecubeMod.core.getConfig().pokemobLifeSpan);
        float scale = 100f / full;
        current *= scale / 100f;
        current = Math.min(1, current);
        int i = 84, j = 37;
        int col = 0xFF555555;
        this.drawGradientRect(i, j, i + 80, j + 10, col, col);
        col = 0xFFFFFFFF;
        int col1 = 0xFF00FF77;
        this.drawGradientRect(i, j, i + (int) (80 * current), j + 10, col, col1);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int x, int y, float z)
    {
        super.drawScreen(x, y, z);
        name.drawTextBox();
        GuiButton stay = buttonList.get(0);
        GuiButton guard = buttonList.get(1);
        GuiButton sit = buttonList.get(2);
        List<String> text = Lists.newArrayList();
        if (guard.isMouseOver())
        {
            text.add(I18n.format("pokemob.stance.guard"));
            this.drawHoveringText(text, x, y);
        }
        if (stay.isMouseOver())
        {
            if (pokemob.getGeneralState(GeneralStates.STAYING)) text.add(I18n.format("pokemob.stance.stay"));
            else text.add(I18n.format("pokemob.stance.follow"));
            this.drawHoveringText(text, x, y);
        }
        if (sit.isMouseOver())
        {
            if (pokemob.getLogicState(LogicStates.SITTING)) text.add(I18n.format("pokemob.stance.sit"));
            else text.add(I18n.format("pokemob.stance.stand"));
            this.drawHoveringText(text, x, y);
        }
        this.renderHoveredToolTip(x, y);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = 10;
        int yOffset = 33;
        String sit = "";
        String stay = "";
        String guard = "";
        buttonList.add(new PokemobButton(0, width / 2 - xOffset + 70, height / 2 - yOffset, 20, 20, stay, pokemob));
        buttonList.add(new PokemobButton(1, width / 2 - xOffset + 36, height / 2 - yOffset, 20, 20, guard, pokemob));
        buttonList.add(new PokemobButton(2, width / 2 - xOffset + 2, height / 2 - yOffset, 20, 20, sit, pokemob));
        yOffset = 77;
        buttonList.add(new GuiButton(3, width / 2 - xOffset + 60, height / 2 - yOffset, 30, 10, "AI"));
        buttonList.add(new GuiButton(4, width / 2 - xOffset + 30, height / 2 - yOffset, 30, 10, "ST"));
        buttonList.add(new GuiButton(5, width / 2 - xOffset + 00, height / 2 - yOffset, 30, 10, "RT"));
        xOffset = 80;
        name = new NameField(0, fontRenderer, width / 2 - xOffset, height / 2 - yOffset, 120, 10);
        name.setText(pokemob.getPokemonDisplayName().getUnformattedComponentText().trim());
        name.setEnableBackgroundDrawing(false);
        name.setTextColor(0xFFFFFFFF);
    }

    /** Called when the mouse is clicked.
     * 
     * @throws IOException */
    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException
    {
        super.mouseClicked(x, y, button);
        name.mouseClicked(x, y, button);
    }
}

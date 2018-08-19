/**
 *
 */
package pokecube.core.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveToHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.AbleStatus;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class GuiDisplayPokecubeInfo extends Gui
{
    protected static int                 lightGrey  = 0xDDDDDD;
    public static int[]                  guiDims    = { 147, 42 };
    public static int[]                  targetDims = { 147, 42 };
    public static int[]                  teleDims   = { 147, 42 };
    public static GuiDisplayPokecubeInfo instance;

    public static float scale(float scaled, boolean apply)
    {
        if (PokecubeMod.core.getConfig().guiAutoScale) return 1;

        Minecraft mc = Minecraft.getMinecraft();
        float scaleFactor = 1;
        boolean flag = mc.isUnicode();
        int i = mc.gameSettings.guiScale;
        int scaledWidth = mc.displayWidth;
        int scaledHeight = mc.displayHeight;
        if (i == 0)
        {
            i = 1000;
        }
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240)
        {
            ++scaleFactor;
        }

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1)
        {
            --scaleFactor;
        }
        float scaleFactor2 = 1;
        i = 1000;
        while (scaleFactor2 < i && scaledWidth / (scaleFactor2 + 1) >= 320 && scaledHeight / (scaleFactor2 + 1) >= 240)
        {
            ++scaleFactor2;
        }

        if (flag && scaleFactor2 % 2 != 0 && scaleFactor2 != 1)
        {
            --scaleFactor2;
        }
        scaleFactor2 *= scaled;
        if (apply) GL11.glScaled(scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor);
        return scaleFactor2;
    }

    public static void sendMoveIndexPacket(IPokemob pokemob, int moveIndex)
    {
        PacketCommand.sendCommand(pokemob, Command.CHANGEMOVEINDEX,
                new MoveIndexHandler((byte) moveIndex).setFromOwner(true));
    }

    public static int[] applyTransform(String ref, int[] shift, int[] dims, float scale)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(minecraft);
        int w = shift[0];
        int h = shift[1];
        int x = 0;
        int y = 0;
        int scaledWidth = res.getScaledWidth();
        int scaledHeight = res.getScaledHeight();
        int dx = 1;
        int dy = 1;
        switch (ref)
        {
        case "top_left":
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            break;
        case "middle_left":
            h = scaledHeight / 2 - h - dims[1];
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            break;
        case "bottom_left":
            h = scaledHeight - h - dims[1];
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            dy = -1;
            break;
        case "top_right":
            w = scaledWidth - w;
            h = Math.min(h + dims[1], scaledHeight);
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            dx = -1;
            break;
        case "right_bottom":
            w = scaledWidth - w - dims[0];
            h = scaledHeight - h - dims[1];
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            dx = -1;
            dy = -1;
            break;
        case "right_middle":
            w = scaledWidth - w - dims[0];
            h = scaledHeight / 2 - h - dims[1];
            GlStateManager.translate(w, h, 0);
            GlStateManager.scale(scale, scale, scale);
            dx = -1;
            dy = -1;
            break;
        case "bottom_middle":
            x = scaledWidth / 2 - w;
            y = scaledHeight;
            w = scaledWidth / 2 - w;
            h = scaledHeight - h - dims[1];
            GlStateManager.translate(w, h, 0);
            h = (int) (-dims[1] / scale - shift[1]);
            w = 0;
            dx = -1;
            dy = -1;
            GlStateManager.scale(scale, scale, scale);
            break;
        }
        int[] ret = { x, y, w, h, dx, dy };
        return ret;
    }

    public static GuiDisplayPokecubeInfo instance()
    {
        return instance;
    }

    protected FontRenderer fontRenderer;

    protected Minecraft    minecraft;

    IPokemob[]             arrayRet         = new IPokemob[0];

    int                    refreshCounter   = 0;

    int                    indexPokemob     = 0;
    public int             currentMoveIndex = 0;

    /**
     *
     */
    public GuiDisplayPokecubeInfo()
    {
        minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        fontRenderer = minecraft.fontRenderer;
        if (instance != null)
        {
            MinecraftForge.EVENT_BUS.unregister(instance);
        }
        instance = this;
    }

    protected void draw(RenderGameOverlayEvent.Post event)
    {
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderMoveMessages(event.getType()));
        if (indexPokemob > getPokemobsToDisplay().length)
        {
            refreshCounter = 0;
            indexPokemob = 0;
            arrayRet = getPokemobsToDisplay();
        }
        if (indexPokemob >= getPokemobsToDisplay().length)
        {
            indexPokemob = 0;
        }
        if (indexPokemob >= getPokemobsToDisplay().length) { return; }
        if (fontRenderer == null) fontRenderer = minecraft.fontRenderer;
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderSelectedInfo());
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTargetInfo());
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTeleports());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawSelected(GuiEvent.RenderSelectedInfo evt)
    {
        int dir = PokecubeMod.core.getConfig().guiDown ? 1 : -1;
        int nameOffsetX = dir == 1 ? 43 : 43;
        int nameOffsetY = dir == 1 ? 0 : 23;
        int movesOffsetX = 42;
        int movesOffsetY = dir == 1 ? 22 : 10;
        int mobOffsetX = 0;
        int mobOffsetY = 0;
        int hpOffsetX = 42;
        int hpOffsetY = 13;
        int xpOffsetX = 42;
        int xpOffsetY = 20;
        int statusOffsetX = 0;
        int statusOffsetY = 27;
        int confuseOffsetX = 12;
        int confuseOffsetY = 1;
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);
        applyTransform(PokecubeCore.core.getConfig().guiRef, PokecubeMod.core.getConfig().guiPos, guiDims,
                PokecubeMod.core.getConfig().guiSize);
        int w = 0;// trans[0];
        int h = 0;// trans[1];
        IPokemob originalPokemob = getCurrentPokemob();
        if (originalPokemob != null)
        {
            EntityLiving entity = originalPokemob.getEntity();
            String displayName = originalPokemob.getPokemonDisplayName().getFormattedText();
            int currentMoveIndex = originalPokemob.getMoveIndex();
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            // Render HP
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(hpOffsetX + w, hpOffsetY + h, 43, 12, 92, 7);
            float total = entity.getMaxHealth();
            float ratio = entity.getHealth() / total;
            float f = 0.00390625F;
            float f1 = 0.00390625F;
            float x = hpOffsetX + 1 + w;
            float y = hpOffsetY + 1 + h;
            float width = 92 * ratio;
            float height = 5;
            int u = 0;
            int v = 85;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.zLevel).tex((u) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.zLevel).tex((u + width) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + 0, this.zLevel).tex((u + width) * f, (v) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex((u) * f, (v) * f1).endVertex();
            tessellator.draw();

            // Render XP
            this.drawTexturedModalRect(xpOffsetX + w, xpOffsetY + h, 43, 19, 92, 5);

            int current = originalPokemob.getExp();
            int level = originalPokemob.getLevel();
            int prev = Tools.levelToXp(originalPokemob.getExperienceMode(), level);
            int next = Tools.levelToXp(originalPokemob.getExperienceMode(), level + 1);
            int levelDiff = next - prev;
            int diff = current - prev;
            ratio = diff / ((float) levelDiff);
            if (level == 100) ratio = 1;
            x = xpOffsetX + 1 + w;
            y = xpOffsetY + h;
            width = 92 * ratio;
            height = 2;
            u = 0;
            v = 97;
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.zLevel).tex((u) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.zLevel).tex((u + width) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + 0, this.zLevel).tex((u + width) * f, (v) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex((u) * f, (v) * f1).endVertex();
            tessellator.draw();

            // Render Status
            byte status = originalPokemob.getStatus();
            if (status != IMoveConstants.STATUS_NON)
            {
                int dv = 0;
                if ((status & IMoveConstants.STATUS_BRN) != 0)
                {
                    dv = 2 * 14;
                }
                if ((status & IMoveConstants.STATUS_FRZ) != 0)
                {
                    dv = 1 * 14;
                }
                if ((status & IMoveConstants.STATUS_PAR) != 0)
                {
                    dv = 3 * 14;
                }
                if ((status & IMoveConstants.STATUS_PSN) != 0)
                {
                    dv = 4 * 14;
                }
                this.drawTexturedModalRect(statusOffsetX + w, statusOffsetY + h, 0, 138 + dv, 15, 15);
            }
            if ((originalPokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
            {
                GlStateManager.translate(0, 0, 100);
                this.drawTexturedModalRect(confuseOffsetX + w, confuseOffsetY + h, 0, 211, 24, 16);
                GlStateManager.translate(0, 0, -100);
            }

            // Render Name
            if (currentMoveIndex == 5)
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(nameOffsetX + w, nameOffsetY + h, 44, 0, 90, 13);
            if (fontRenderer.getStringWidth(displayName) > 70)
            {
                displayName = fontRenderer.trimStringToWidth(displayName, 70);
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            fontRenderer.drawString(displayName, nameOffsetX + 3 + w, nameOffsetY + 3 + h, lightGrey);

            // Render level
            GL11.glColor4f(1.0F, 0.5F, 0.0F, 1.0F);
            fontRenderer.drawString("L." + level, nameOffsetX + 88 + w - fontRenderer.getStringWidth("L." + level),
                    nameOffsetY + 3 + h, lightGrey);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw number of pokemon
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            int n = getPokemobsToDisplay().length;
            int num = fontRenderer.getStringWidth("" + n);
            this.drawTexturedModalRect(nameOffsetX + 89 + w, nameOffsetY + h, 0, 27, 15, 15);
            fontRenderer.drawString("" + n, nameOffsetX + 95 - num / 4 + w, nameOffsetY + 4 + h, lightGrey);

            // Render Moves
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            int h1 = 1;
            int moveIndex = 0;
            int moveCount = 0;
            for (moveCount = 0; moveCount < 4; moveCount++)
            {
                if (originalPokemob.getMove(moveCount) == null) break;
            }
            if (dir == -1)
            {
                h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            }
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                int index = moveIndex;

                Move_Base move = MovesUtils.getMoveFromName(originalPokemob.getMove(index));
                boolean disabled = index >= 0 && index < 4 && originalPokemob.getDisableTimer(index) > 0;
                if (move != null)
                {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    // bind texture
                    GL11.glPushMatrix();// TODO find out why both needed
                    minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                    this.drawTexturedModalRect(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91, 13);

                    // Render colour overlays.
                    if (currentMoveIndex == index)
                    {
                        // Draw selected indictator
                        GL11.glColor4f(0F, 1F, 1F, 0.5F);
                        this.drawTexturedModalRect(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91,
                                13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                        // Draw cooldown box
                        float timer = 1;
                        Move_Base lastMove;
                        if (MovesUtils.isAbleToUseMoves(originalPokemob) != AbleStatus.ABLE)
                        {
                            timer = 0;
                        }
                        else if ((lastMove = MovesUtils.getMoveFromName(originalPokemob.getLastMoveUsed())) != null)
                        {
                            timer -= (originalPokemob.getAttackCooldown() / (float) MovesUtils.getAttackDelay(
                                    originalPokemob, originalPokemob.getLastMoveUsed(),
                                    (lastMove.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0, false));
                        }
                        timer = Math.max(0, Math.min(timer, 1));
                        GL11.glColor4f(0F, 0.1F, 1.0F, 0.5F);
                        this.drawTexturedModalRect(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1,
                                (int) (91 * timer), 13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                    }
                    if (disabled)
                    {
                        GL11.glColor4f(1F, 0.0F, 0.0F, 0.5F);
                        this.drawTexturedModalRect(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91,
                                13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                    }

                    GL11.glPopMatrix();
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glPushMatrix();
                    Color moveColor = new Color(move.getType(originalPokemob).colour);
                    GL11.glColor4f(moveColor.getRed() / 255f, moveColor.getGreen() / 255f, moveColor.getBlue() / 255f,
                            1.0F);
                    fontRenderer.drawString(MovesUtils.getMoveName(move.getName()).getFormattedText(),
                            5 + movesOffsetX + w, index * 13 + movesOffsetY + 3 + h,
                            move.getType(originalPokemob).colour);
                    GL11.glPopMatrix();
                }
            }

            // Render Mob
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(mobOffsetX + w, mobOffsetY + h, 0, 0, 42, 42);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            GL11.glColor4f(1, 1, 1, 1);
            GuiPokemob.renderMob(originalPokemob, -30, -25, 0, 0, 0, 0, 0, 0.75f);
        }
        GL11.glPopMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawTarget(GuiEvent.RenderTargetInfo evt)
    {
        int dir = PokecubeMod.core.getConfig().guiDown ? 1 : -1;
        int nameOffsetX = dir == 1 ? 43 : 43;
        int nameOffsetY = dir == 1 ? 0 : 23;
        int mobOffsetX = 0;
        int mobOffsetY = 0;
        int hpOffsetX = 42;
        int hpOffsetY = 13;
        int statusOffsetX = 0;
        int statusOffsetY = 27;
        int confuseOffsetX = 12;
        int confuseOffsetY = 1;
        int i, j;
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);
        applyTransform(PokecubeCore.core.getConfig().targetRef, PokecubeMod.core.getConfig().targetPos, targetDims,
                PokecubeMod.core.getConfig().targetSize);
        int w = 0;
        int h = 0;
        IPokemob pokemob = getCurrentPokemob();
        render:
        if (pokemob != null)
        {
            EntityLivingBase entity = pokemob.getEntity().getAttackTarget();
            if (entity == null || entity.isDead) break render;

            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            // Render HP
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(hpOffsetX + w, hpOffsetY + h, 43, 12, 92, 7);
            float total = entity.getMaxHealth();
            float ratio = entity.getHealth() / total;
            float f = 0.00390625F;
            float f1 = 0.00390625F;
            float x = hpOffsetX + 1 + w;
            float y = hpOffsetY + 1 + h;
            float width = 92 * ratio;
            float height = 5;
            int u = 0;
            int v = 85;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.zLevel).tex((u) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.zLevel).tex((u + width) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + 0, this.zLevel).tex((u + width) * f, (v) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex((u) * f, (v) * f1).endVertex();
            tessellator.draw();

            // Render Status
            pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null)
            {
                byte status = pokemob.getStatus();
                if (status != IMoveConstants.STATUS_NON)
                {
                    int dv = 0;
                    if ((status & IMoveConstants.STATUS_BRN) != 0)
                    {
                        dv = 2 * 14;
                    }
                    if ((status & IMoveConstants.STATUS_FRZ) != 0)
                    {
                        dv = 1 * 14;
                    }
                    if ((status & IMoveConstants.STATUS_PAR) != 0)
                    {
                        dv = 3 * 14;
                    }
                    if ((status & IMoveConstants.STATUS_PSN) != 0)
                    {
                        dv = 4 * 14;
                    }
                    this.drawTexturedModalRect(statusOffsetX + w, statusOffsetY + h, 0, 138 + dv, 15, 15);
                }
                if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
                {
                    GlStateManager.translate(0, 0, 100);
                    this.drawTexturedModalRect(confuseOffsetX + w, confuseOffsetY + h, 0, 211, 24, 16);
                    GlStateManager.translate(0, 0, -100);
                }
            }

            // Render Name
            GL11.glColor4f(1.0F, 0.4F, 0.4F, 1.0F);
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(nameOffsetX + w, nameOffsetY + h, 44, 0, 90, 13);
            String displayName = entity.getDisplayName().getFormattedText();
            if (fontRenderer.getStringWidth(displayName) > 70)
            {

            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            fontRenderer.drawString(displayName, nameOffsetX + 3 + w, nameOffsetY + 3 + h, lightGrey);

            // Render Mob
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(mobOffsetX + w, mobOffsetY + h, 0, 0, 42, 42);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);

            float scale = 1;
            float size = 0;
            j = w;
            int k = h;

            size = Math.max(entity.width, entity.height) * scale;

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(j + 25, k + 25, 50F);
            float zoom = 20f / size;
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - 0;
            RenderHelper.enableStandardItemLighting();
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);

            i = 15728880;
            int j1 = i % 65536;
            int k1 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            GL11.glRotated(entity.rotationYaw - 40, 0, 1, 0);
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, -0.123456, 0, 0, 1.5F, false);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
        GL11.glPopMatrix();
    }

    /** @return the currently selected pokemob */
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = null;
        if (indexPokemob < arrayRet.length && indexPokemob >= 0 && arrayRet.length > 0)
        {
            pokemob = arrayRet[indexPokemob];
        }
        return pokemob;
    }

    public IPokemob[] getPokemobsToDisplay()
    {
        if (refreshCounter++ > 5)
        {
            refreshCounter = 0;
        }
        if (refreshCounter > 0) return arrayRet;

        EntityPlayer player = minecraft.player;

        if (player == null || player.getEntityWorld() == null) return new IPokemob[0];

        List<Entity> pokemobs = Lists.newArrayList(player.getEntityWorld().getLoadedEntityList());

        List<IPokemob> ret = new ArrayList<IPokemob>();
        Set<Integer> added = new HashSet<>();
        for (Entity object : pokemobs)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(object);
            if (pokemob == null) continue;

            boolean owner = pokemob.getGeneralState(GeneralStates.TAMED) && pokemob.getPokemonOwner() != null;

            if (owner)
            {
                owner = player.getEntityId() == pokemob.getPokemonOwner().getEntityId();
            }
            int id = pokemob.getPokemonUID();

            if (owner && !pokemob.getLogicState(LogicStates.SITTING) && !pokemob.getGeneralState(GeneralStates.STAYING)
                    && !added.contains(id))
            {
                ret.add(pokemob);
                added.add(id);
            }

        }
        arrayRet = ret.toArray(new IPokemob[ret.size()]);
        Arrays.sort(arrayRet, new Comparator<IPokemob>()
        {

            @Override
            public int compare(IPokemob o1, IPokemob o2)
            {
                Entity e1 = o1.getEntity();
                Entity e2 = o2.getOwner();

                if (e1.ticksExisted == e2.ticksExisted)
                {
                    if (o2.getLevel() == o1.getLevel()) return (o1.getPokemonDisplayName().getFormattedText()
                            .compareTo(o2.getPokemonDisplayName().getFormattedText()));
                    return o2.getLevel() - o1.getLevel();
                }
                return e1.ticksExisted - e2.ticksExisted;
            }
        });
        return arrayRet;
    }

    /** Shifts the gui by x and y
     * 
     * @param x
     * @param y */
    public void moveGui(int x, int y)
    {
        PokecubeMod.core.getConfig().guiPos[0] += x;
        PokecubeMod.core.getConfig().guiPos[1] += y;
        saveConfig();
    }

    /** Incremenrs pokemob move index
     * 
     * @param i */
    public void nextMove(int i)
    {
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = (pokemob.getMoveIndex() + i);

            int max = 0;
            for (max = 0; max < 4; max++)
            {
                if (pokemob.getMove(max) == null) break;
            }
            if (index >= 5) index = 0;
            if (index >= max) index = 5;
            sendMoveIndexPacket(pokemob, index);
        }
    }

    /** Select next pokemob */
    public void nextPokemob()
    {
        indexPokemob++;
        if (indexPokemob >= arrayRet.length) indexPokemob = 0;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if ((minecraft.currentScreen == null || GuiArranger.toggle)
                    && !((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                    && event.getType() == ElementType.HOTBAR || event.getType() == ElementType.CHAT)
            {
                draw(event);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /** Identifies target of attack, and sends the packet with info to server */
    public void pokemobAttack()
    {
        if (getCurrentPokemob() == null) return;
        EntityPlayer player = minecraft.player;
        Predicate<Entity> selector = new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity input)
            {
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
                if (pokemob == null) return true;
                return pokemob.getOwner() != getCurrentPokemob().getOwner();
            }
        };
        Entity target = Tools.getPointedEntity(player, 32, selector);
        Vector3 targetLocation = Tools.getPointedLocation(player, 32);
        boolean sameOwner = false;
        IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null)
        {
            sameOwner = targetMob.getPokemonOwner() == player;
        }
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) { return; }
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                GuiTeleport.instance().setState(false);
                PacketCommand.sendCommand(pokemob, Command.TELEPORT, new TeleportHandler().setFromOwner(true));
                return;
            }
        }
        if (target != null && !sameOwner && target instanceof EntityLivingBase)
        {
            PacketCommand.sendCommand(pokemob, Command.ATTACKENTITY,
                    new AttackEntityHandler(target.getEntityId()).setFromOwner(true));
        }
        else if (targetLocation != null)
        {
            PacketCommand.sendCommand(pokemob, Command.ATTACKLOCATION,
                    new AttackLocationHandler(targetLocation).setFromOwner(true));
        }
        else
        {
            PacketCommand.sendCommand(pokemob, Command.ATTACKNOTHING, new AttackNothingHandler().setFromOwner(true));
        }
    }

    /** Recalls selected pokemob, if none selected, will try to identify a
     * pokemob being looked at, and recalls that */
    public void pokemobBack()
    {
        IPokemob pokemob = getCurrentPokemob();

        if (GuiScreen.isShiftKeyDown() && pokemob != null && pokemob.getOwner() != null)
        {
            PacketCommand.sendCommand(pokemob, Command.MOVETO,
                    new MoveToHandler(Vector3.getNewVector().set(pokemob.getOwner()), (float) pokemob.getEntity()
                            .getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            return;
        }

        // System.out.println(pokemob+":");
        if (pokemob != null) pokemob.returnToPokecube();
        else
        {
            EntityPlayer player = minecraft.player;
            Entity target = null;
            Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && targetMob.getPokemonOwner() == player)
            {
                targetMob.returnToPokecube();
            }
        }

        if (indexPokemob >= arrayRet.length) indexPokemob--;

        if (indexPokemob < 0) indexPokemob = 0;

    }

    /** Sends the packet to toggle all pokemobs set to follow between sit and
     * stand */
    public void pokemobStance()
    {
        IPokemob pokemob;
        if ((pokemob = getCurrentPokemob()) != null)
        {
            PacketCommand.sendCommand(pokemob, Command.STANCE,
                    new StanceHandler(!pokemob.getLogicState(LogicStates.SITTING), StanceHandler.BUTTONTOGGLESIT)
                            .setFromOwner(true));
        }
        else
        {
            EntityPlayer player = minecraft.player;
            Entity target = null;
            Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && targetMob.getPokemonOwner() == player)
            {
                PacketCommand.sendCommand(targetMob, Command.STANCE,
                        new StanceHandler(!targetMob.getLogicState(LogicStates.SITTING), StanceHandler.BUTTONTOGGLESIT)
                                .setFromOwner(true));
            }
        }
    }

    /** Decrements pokemob move index
     * 
     * @param j */
    public void previousMove(int j)
    {
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex();

            if (index == 5)
            {
                for (int i = 3; i > 0; i -= j)
                {
                    if (pokemob.getMove(i) != null)
                    {
                        index = i;
                        break;
                    }
                }
            }
            else
            {
                index -= j;
            }

            if (index % 5 >= 0) index = index % 5;
            else index = 5;
            sendMoveIndexPacket(pokemob, index);
        }
    }

    /** Select previous pokemob */
    public void previousPokemob()
    {
        indexPokemob--;
        if (indexPokemob < 0) indexPokemob = arrayRet.length - 1;
    }

    private void saveConfig()
    {
        PokecubeMod.core.getConfig().setSettings();
    }

    /** Sets pokemob's move index.
     * 
     * @param num */
    public void setMove(int num)
    {
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = num;
            if (index % 4 >= 0) index = index % 4;
            sendMoveIndexPacket(pokemob, index);
        }
    }
}

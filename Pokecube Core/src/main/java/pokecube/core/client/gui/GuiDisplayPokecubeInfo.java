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

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
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
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketPokemobAttack;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
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
        case "bottom_right":
            w = scaledWidth - w - dims[0];
            h = scaledHeight - h - dims[1];
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
        fontRenderer = minecraft.fontRendererObj;
        if (instance != null)
        {
            MinecraftForge.EVENT_BUS.unregister(instance);
        }
        instance = this;
    }

    protected void draw(RenderGameOverlayEvent.Post event)
    {
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
        if (fontRenderer == null) fontRenderer = minecraft.fontRendererObj;
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderSelectedInfo());
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTargetInfo());
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
        int i, j;
        GL11.glPushMatrix();
        applyTransform(PokecubeCore.core.getConfig().guiRef, PokecubeMod.core.getConfig().guiPos, guiDims,
                PokecubeMod.core.getConfig().guiSize);
        int w = 0;// trans[0];
        int h = 0;// trans[1];
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            EntityLiving entity = (EntityLiving) pokemob;
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
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.zLevel).tex((u) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.zLevel).tex((u + width) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + 0, this.zLevel).tex((u + width) * f, (v) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex((u) * f, (v) * f1).endVertex();
            tessellator.draw();

            // Render XP
            this.drawTexturedModalRect(xpOffsetX + w, xpOffsetY + h, 43, 19, 92, 5);

            int current = pokemob.getExp();
            int level = pokemob.getLevel();
            int prev = Tools.levelToXp(pokemob.getExperienceMode(), level);
            int next = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
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

            // Render Name
            if (currentMoveIndex == 5)
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(nameOffsetX + w, nameOffsetY + h, 44, 0, 90, 13);
            String displayName = entity.getDisplayName().getFormattedText();
            if (fontRenderer.getStringWidth(displayName) > 70)
            {

            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            fontRenderer.drawString(displayName, nameOffsetX + 3 + w, nameOffsetY + 3 + h, lightGrey);

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
                if (pokemob.getMove(moveCount) == null) break;
            }
            if (dir == -1)
            {
                h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            }
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                int index = moveIndex;

                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(index));

                if (move != null)
                {
                    if (currentMoveIndex == index) GL11.glColor4f(0F, 0.1F, 1.0F, 1.0F);
                    else GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    // bind texture
                    minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                    this.drawTexturedModalRect(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91, 13);
                    GL11.glPushMatrix();// TODO find out why both needed
                    Color moveColor = new Color(move.move.type.colour);
                    GL11.glColor4f(moveColor.getRed() / 255f, moveColor.getGreen() / 255f, moveColor.getBlue() / 255f,
                            1.0F);
                    fontRenderer.drawString(MovesUtils.getMoveName(move.getName()).getFormattedText(),
                            5 + movesOffsetX + w, index * 13 + movesOffsetY + 3 + h, move.getType(pokemob).colour);
                    GL11.glPopMatrix();
                }
            }

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
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, -0.123456, 0, 0, 1.5F, false);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
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
        applyTransform(PokecubeCore.core.getConfig().targetRef, PokecubeMod.core.getConfig().targetPos, targetDims,
                PokecubeMod.core.getConfig().targetSize);
        int w = 0;
        int h = 0;
        IPokemob pokemob = getCurrentPokemob();
        render:
        if (pokemob != null)
        {
            EntityLivingBase entity = ((EntityLiving) pokemob).getAttackTarget();
            if (entity == null) break render;

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
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.zLevel).tex((u) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.zLevel).tex((u + width) * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + 0, this.zLevel).tex((u + width) * f, (v) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.zLevel).tex((u) * f, (v) * f1).endVertex();
            tessellator.draw();

            // Render Status
            if (entity instanceof IPokemob)
            {
                pokemob = (IPokemob) entity;
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
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, -0.123456, 0, 0, 1.5F, false);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
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
        if (pokemob != null)
        {
            currentMoveIndex = pokemob.getMoveIndex();
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

        EntityPlayer player = minecraft.thePlayer;

        if (player == null || player.getEntityWorld() == null) return new IPokemob[0];

        List<?> pokemobs = Lists.newArrayList(player.getEntityWorld().getLoadedEntityList());

        List<IPokemob> ret = new ArrayList<IPokemob>();
        Set<Integer> added = new HashSet<>();
        for (Object object : pokemobs)
        {
            if (!(object instanceof IPokemob)) continue;
            IPokemob pokemob = (IPokemob) object;

            boolean owner = pokemob.getPokemonAIState(IMoveConstants.TAMED) && pokemob.getPokemonOwner() != null;

            if (owner)
            {
                owner = player.getEntityId() == pokemob.getPokemonOwner().getEntityId();
            }
            int id = pokemob.getPokemonUID();

            if (owner && !pokemob.getPokemonAIState(IMoveConstants.SITTING)
                    && !pokemob.getPokemonAIState(IMoveConstants.STAYING) && !added.contains(id))
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
                Entity e1 = (Entity) o1;
                Entity e2 = (Entity) o2;

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
            pokemob.setMoveIndex(index);
        }
    }

    /** Select next pokemob */
    public void nextPokemob()
    {
        indexPokemob++;
        if (indexPokemob >= arrayRet.length) indexPokemob = 0;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        try
        {
            if ((minecraft.currentScreen == null || GuiArranger.toggle)
                    && !((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                    && event.getType() == ElementType.HOTBAR)
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

        EntityPlayer player = minecraft.thePlayer;
        Entity attacker = ((Entity) getCurrentPokemob());
        Entity target = Tools.getPointedEntity(player, 32);
        boolean teleport = false;
        Vector3 targetLocation = Tools.getPointedLocation(player, 32);

        boolean sameOwner = false;
        if (target instanceof IPokemob)
        {
            sameOwner = ((IPokemob) target).getPokemonOwner() == player;
        }

        IPokemob pokemob = getCurrentPokemob();

        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) { return; }
            boolean attack = false;
            if (target != null && !minecraft.thePlayer.isSneaking() && !sameOwner)
            {
                attack = true;
            }
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                GuiTeleport.instance().setState(false);

                Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
                List<TeleDest> locations = PokecubeSerializer.getInstance()
                        .getTeleports(minecraft.thePlayer.getCachedUniqueIdString());

                if (locations.size() > 0)
                {
                    teleport = true;
                }
            }
            else if (!attack)
            {
                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));
                if (move != null && (target != null || targetLocation != null))
                {
                    if (targetLocation != null)
                    {
                        targetLocation.addTo(Vector3.getNewVector().set(player.getLookVec()).scalarMultBy(0.5));
                    }
                }
            }
        }
        PacketPokemobAttack.sendAttackUse(attacker, target, targetLocation, teleport);
    }

    /** Recalls selected pokemob, if none selected, will try to identify a
     * pokemob being looked at, and recalls that */
    public void pokemobBack()
    {
        IPokemob pokemob = getCurrentPokemob();

        if (GuiScreen.isShiftKeyDown() && pokemob != null)
        {
            MessageServer message = new MessageServer(MessageServer.COME, ((Entity) pokemob).getEntityId());
            PokecubeMod.packetPipeline.sendToServer(message);
            return;
        }

        // System.out.println(pokemob+":");
        if (pokemob != null) pokemob.returnToPokecube();
        else
        {
            EntityPlayer player = minecraft.thePlayer;
            Entity target = null;
            Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            if (target != null && target instanceof IPokemob && ((IPokemob) target).getPokemonOwner() == player)
            {
                ((IPokemob) target).returnToPokecube();
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
            PacketPokemobGui packet = new PacketPokemobGui(PacketPokemobGui.BUTTONTOGGLESIT,
                    ((Entity) pokemob).getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
        else
        {
            EntityPlayer player = minecraft.thePlayer;
            Entity target = null;
            Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            if (target != null && target instanceof IPokemob && ((IPokemob) target).getPokemonOwner() == player)
            {
                PacketPokemobGui packet = new PacketPokemobGui(PacketPokemobGui.BUTTONTOGGLESIT, target.getEntityId());
                PokecubeMod.packetPipeline.sendToServer(packet);
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

            if (index % 5 >= 0) pokemob.setMoveIndex(index % 5);
            else pokemob.setMoveIndex(5);
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
            if (index % 4 >= 0) pokemob.setMoveIndex(index % 4);
        }
    }
}

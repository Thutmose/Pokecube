package pokecube.core.client.render.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;

@SideOnly(Side.CLIENT)
public abstract class RenderPokemobInfos<T extends EntityLiving> extends RenderLiving<T>
{
    public static boolean shouldShow(EntityLivingBase entity)
    {
        if (((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI
                || entity.isBeingRidden()) { return false; }

        EntityLivingBase player = Minecraft.getMinecraft().thePlayer;
        if (!entity.addedToChunk || entity.getRidingEntity() == player) return false;
        float d = entity.getDistanceToEntity(player);
        IPokemob pokemob = ((IPokemob) entity);
        boolean tameFactor = pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !pokemob.getPokemonAIState(IMoveConstants.STAYING);
        if ((tameFactor && d < 35) || d < 8)
        {
            return true;
        }
        else
        {
            Entity target = ((EntityCreature) entity).getAttackTarget();
            return (player.equals(target) || ((IPokemob) entity).getPokemonAIState(IMoveConstants.TAMED));
        }
    }

    public RenderPokemobInfos(RenderManager m, ModelBase modelbase, float shadowSize)
    {
        super(m, modelbase, shadowSize);
    }

    @Override
    public void doRender(T entityliving, double d, double d1, double d2, float f, float partialTick)
    {
        super.doRender(entityliving, d, d1, d2, f, partialTick);
        renderHp(entityliving, d, d1, d2, f, partialTick);
    }

    protected void renderHp(T entityliving, double d, double d1, double d2, float f, float partialTick)
    {
        if (partialTick <= 1 && RenderPokemobInfos.shouldShow(entityliving))
        {
            float f2 = 1.6F;
            float f3 = 0.01666667F * f2;
            GL11.glPushMatrix();
            EntityLivingBase player = Minecraft.getMinecraft().thePlayer;
            float dist = entityliving.getDistanceToEntity(player);
            if (dist < 5) GlStateManager.disableDepth();

            GL11.glTranslatef((float) d + 0.0F, (float) d1 + entityliving.height - 0.35f, (float) d2);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-f3, -f3, f3);
            int j = 0xF0F0F0;// 61680;
            int k = j % 0x000100;
            int l = j / 0xFFFFFF;
            GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, k / 1.0F, l / 1.0F);
            PTezzelator tez = PTezzelator.instance;
            int offset = -25;
            double width = 2;

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();

            int length = 40;
            float health = entityliving.getHealth();// getHealth()

            if (health < 0)
            {
                health = 0;
            }

            // Draw the Health bar
            float maxHealth = entityliving.getMaxHealth();// getMaxHealth()
            float relativeHp = health / maxHealth;
            float shift = length * 2 * relativeHp;

            VertexFormat format;
            int mode = 7;
            format = DefaultVertexFormats.POSITION_COLOR;

            tez.begin(mode, format);

            double start = -length + shift;
            double end = length;

            // Empty Health bar
            tez.vertex(start, offset, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(start, offset + width, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(end, offset + width, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(end, offset, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();

            int healthColor = 0x00FF00;

            if (relativeHp < 0.6F)
            {
                healthColor = 0xFF0000 + (((int) (0xFF * relativeHp)) * 0x100);
            }

            // Coloured Health bar
            relativeHp *= length * 2;
            tez.vertex(-length, offset, 0.0D).color(healthColor, 255).endVertex();
            tez.vertex(-length, offset + width, 0.0D).color(healthColor, 255).endVertex();
            tez.vertex(relativeHp - length, offset + width, 0.0D).color(healthColor, 255).endVertex();
            tez.vertex(relativeHp - length, offset, 0.0D).color(healthColor, 255).endVertex();

            tez.end();

            int exp = ((IPokemob) entityliving).getExp() - Tools
                    .levelToXp(((IPokemob) entityliving).getExperienceMode(), ((IPokemob) entityliving).getLevel());
            float maxExp = Tools.levelToXp(((IPokemob) entityliving).getExperienceMode(),
                    ((IPokemob) entityliving).getLevel() + 1)
                    - Tools.levelToXp(((IPokemob) entityliving).getExperienceMode(),
                            ((IPokemob) entityliving).getLevel());

            if (((IPokemob) entityliving).getLevel() == 100) maxExp = exp = 1;

            if (exp < 0 || !((IPokemob) entityliving).getPokemonAIState(IMoveConstants.TAMED))
            {
                exp = 0;
            }
            //
            float f62 = maxExp;
            float f72 = exp / f62;
            float f82 = length * 2 * f72;

            tez.begin(mode, format);
            // Draw the exp bar
            tez.vertex(-length + f82, offset + width, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(-length + f82, offset + width + 1, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(length, offset + width + 1, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            tez.vertex(length, offset + width, 0.0D).color(0.2F, 0.2F, 0.2F, 1.0F).endVertex();
            // Fill In exp
            tez.vertex(-length, offset + width, 0.0D).color(0.3F, 0.3F, 0.8F, 1.0F).endVertex();
            tez.vertex(-length, offset + width + 1, 0.0D).color(0.3F, 0.3F, 0.8F, 1.0F).endVertex();
            tez.vertex(f82 - length, offset + width + 1, 0.0D).color(0.3F, 0.3F, 0.8F, 1.0F).endVertex();
            tez.vertex(f82 - length, offset + width, 0.0D).color(0.3F, 0.3F, 0.8F, 1.0F).endVertex();
            tez.end();

            GlStateManager.enableTexture2D();

            FontRenderer fontrenderer = getFontRendererFromRenderManager();

            boolean nametag = ((IPokemob) entityliving).getPokemonAIState(IMoveConstants.TAMED);
            nametag = nametag
                    || Minecraft.getMinecraft().thePlayer.getStatFileWriter().hasAchievementUnlocked(
                            PokecubeMod.catchAchievements.get(((IPokemob) entityliving).getPokedexEntry()))
                    || Minecraft.getMinecraft().thePlayer.getStatFileWriter().hasAchievementUnlocked(
                            PokecubeMod.hatchAchievements.get(((IPokemob) entityliving).getPokedexEntry()));

            if (nametag)
            {
                String n;
                Entity owner = ((IPokemob) entityliving).getPokemonOwner();
                // Your pokemob has white name, other's has gray name.
                int colour = renderManager.renderViewEntity.equals(owner) ? 0xFFFFFF
                        : owner == null ? 0x444444 : 0xAAAAAA;
                if ((entityliving.hasCustomName()))
                {
                    n = entityliving.getCustomNameTag();
                }
                else
                {
                    n = ((IPokemob) entityliving).getPokemonDisplayName().getFormattedText();
                }

                int n1 = length - fontrenderer.getStringWidth(n) / 2;

                n1 = Math.max(0, n1);
                fontrenderer.drawString(n, -length + n1, offset - 8, colour);
            }

            int color = 0xBBBBBB;

            if (((IPokemob) entityliving).getSexe() == IPokemob.MALE)
            {
                color = 0x0011CC;
            }
            else if (((IPokemob) entityliving).getSexe() == IPokemob.FEMALE)
            {
                color = 0xCC5555;
            }

            String s = "L." + ((IPokemob) entityliving).getLevel();
            fontrenderer.drawString(s, -length, offset + 5, color);

            if (((IPokemob) entityliving).getPokemonAIState(IMoveConstants.TAMED))
            {
                color = 0x00FF00;
                s = ((int) entityliving.getHealth()) + "/" + ((int) entityliving.getMaxHealth());
                length = fontrenderer.getStringWidth(s);
                fontrenderer.drawString(s, 40 - length, offset + 5, color);
            }
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GL11.glPopMatrix();
        }
    }

    @Override
    public ModelBase getMainModel()
    {
        return mainModel;
    }

    public void setShadowSize(float size)
    {
        this.shadowSize = size;
    }
}

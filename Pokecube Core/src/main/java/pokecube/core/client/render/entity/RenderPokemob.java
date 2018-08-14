package pokecube.core.client.render.entity;

import java.awt.Color;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IPartTexturer;

@SideOnly(Side.CLIENT)
public class RenderPokemob<T extends EntityLiving> extends RenderPokemobInfos<T>
{
    public static final ResourceLocation FRZ = Resources.STATUS_FRZ;
    public static final ResourceLocation PAR = Resources.STATUS_PAR;

    public static void renderEvolution(IPokemob pokemob, float partialTick)
    {
        if (pokemob.isEvolving())
        {
            renderEffect(pokemob, partialTick, PokecubeCore.core.getConfig().evolutionTicks, true);
        }
    }

    public static void renderExitCube(IPokemob pokemob, float partialTick)
    {
        if (!pokemob.getGeneralState(GeneralStates.EXITINGCUBE)) return;
        Entity entity = pokemob.getEntity();
        NBTTagCompound sealTag = PokecubeManager.getSealTag(entity);
        if (sealTag != null && !sealTag.hasNoTags())
        {
            Random rand = new Random();
            Vector3 loc = Vector3.getNewVector().set(entity, true);
            float width = entity.width;
            Vector3 vel = Vector3.getNewVector();
            if (sealTag.getBoolean("Bubbles"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(),
                        EnumParticleTypes.WATER_BUBBLE.getParticleName(), loc, vel);
            }
            if (sealTag.getBoolean("Flames"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(), EnumParticleTypes.FLAME.getParticleName(), loc,
                        vel);
            }
            // *
            if (sealTag.getBoolean("Leaves"))
            {
                vel.x = rand.nextGaussian() / 100;
                vel.y = rand.nextGaussian() / 100;
                vel.z = rand.nextGaussian() / 100;
                loc.x += rand.nextGaussian() / 2;
                loc.y += rand.nextGaussian() / 2;
                loc.z += rand.nextGaussian() / 2;
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(), "leaf", loc, vel);
            }
            if (sealTag.hasKey("dye"))
            {
                vel.x = rand.nextGaussian() / 100;
                vel.y = rand.nextGaussian() / 100;
                vel.z = rand.nextGaussian() / 100;
                loc.x += width * rand.nextGaussian() / 2;
                loc.y += width * rand.nextGaussian() / 2;
                loc.z += width * rand.nextGaussian() / 2;
                PokecubeMod.core.spawnParticle(entity.getEntityWorld(), "powder", loc, vel,
                        ItemDye.DYE_COLORS[sealTag.getInteger("dye")] | 0xFF000000);
            }
        }
    }

    public static void renderEffect(IPokemob pokemob, float partialTick, int duration, boolean scaleMob)
    {
        int ticks = pokemob.getEvolutionTicks();
        PokedexEntry entry = pokemob.getPokedexEntry();
        int color1 = entry.getType1().colour;
        int color2 = entry.getType2().colour;
        if (entry.getType2() == PokeType.unknown)
        {
            color2 = color1;
        }
        Color col1 = new Color(color1);
        Color col2 = new Color(color2);
        ticks = ticks - 50;
        ticks = duration - ticks;

        float scale = 0.25f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderHelper.disableStandardItemLighting();
        float time = 40 * ((float) ticks + partialTick) / duration;
        float f = time / (200f);
        float f1 = 0.0F;
        if (f > 0.8F)
        {
            f1 = (f - 0.8F) / 0.2F;
        }
        Random random = new Random(432L);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        if (scaleMob)
        {
            float mobScale = pokemob.getSize();
            Vector3f dims = entry.getModelSize();
            scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            GL11.glTranslatef(0.0F, dims.y * pokemob.getSize() / 2, 0.0F);
        }
        for (int i = 0; (float) i < (f + f * f) / 2.0F * 100.0F; ++i)
        {
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F + f * 90.0F, 0.0F, 0.0F, 1.0F);
            float f2 = (random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F) * scale;
            float f3 = (random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F) * scale;
            bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(0.0D, 0.0D, 0.0D)
                    .color(col1.getRed(), col1.getGreen(), col1.getBlue(), (int) (255.0F * (1.0F - f1))).endVertex();
            bufferbuilder.pos(-0.866D * (double) f3, (double) f2, (double) (-0.5F * f3))
                    .color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            bufferbuilder.pos(0.866D * (double) f3, (double) f2, (double) (-0.5F * f3))
                    .color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            bufferbuilder.pos(0.0D, (double) f2, (double) (1.0F * f3))
                    .color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            bufferbuilder.pos(-0.866D * (double) f3, (double) f2, (double) (-0.5F * f3))
                    .color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
    }

    public static <V extends EntityLiving> void renderStatus(IModelRenderer<V> renderer, V entity, double d, double d1,
            double d2, float f, float partialTick)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return;
        final byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON) return;
        if (!(status == IMoveConstants.STATUS_FRZ || status == IMoveConstants.STATUS_PAR)) return;

        IPartTexturer oldTexturer = renderer.getTexturer();
        IPartTexturer statusTexturer = new IPartTexturer()
        {

            @Override
            public boolean shiftUVs(String part, double[] toFill)
            {
                return false;
            }

            @Override
            public boolean isFlat(String part)
            {
                return true;
            }

            @Override
            public boolean hasMapping(String part)
            {
                return true;
            }

            @Override
            public void bindObject(Object thing)
            {
            }

            @Override
            public void applyTexture(String part)
            {
                ResourceLocation texture = null;
                if (status == IMoveConstants.STATUS_FRZ)
                {
                    texture = FRZ;
                }
                else if (status == IMoveConstants.STATUS_PAR)
                {
                    texture = PAR;
                }
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
            }

            @Override
            public void addMapping(String part, String tex)
            {
            }

            @Override
            public void addCustomMapping(String part, String state, String tex)
            {
            }
        };

        float time = (entity.ticksExisted + partialTick);
        GL11.glPushMatrix();
        float speed = status == IMoveConstants.STATUS_FRZ ? 0.001f : 0.005f;
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        float var5 = time * speed;
        float var6 = time * speed;
        GL11.glTranslatef(var5, var6, 0.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        float var7 = status == IMoveConstants.STATUS_FRZ ? 0.5f : 1F;
        GL11.glColor4f(var7, var7, var7, 0.5F);
        var7 = 1;
        GL11.glScalef(var7, var7, var7);
        IMobColourable colour = (IMobColourable) entity;
        int[] col = colour.getRGBA();
        int[] bak = col.clone();
        col[3] = 85;
        colour.setRGBA(col);
        renderer.setTexturer(statusTexturer);
        renderer.doRender(entity, d, d1, d2, f, partialTick);
        renderer.setTexturer(oldTexturer);
        colour.setRGBA(bak);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    protected float     scale;

    boolean             blend;

    boolean             normalize;

    int                 src;
    int                 dst;

    protected ModelBase modelStatus;

    Vector3             v = Vector3.getNewVector();

    public RenderPokemob(RenderManager m, ModelBase modelbase, float shadowSize)
    {
        super(m, modelbase, shadowSize);
        try
        {
            modelStatus = modelbase.getClass().getConstructor().newInstance();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    /** Returns an ARGB int color back. Args: entityLiving, lightBrightness,
     * partialTickTime */
    @Override
    protected int getColorMultiplier(T par1EntityLiving, float par2, float par3)
    {
        return super.getColorMultiplier(par1EntityLiving, par2, par3);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return getPokemobTexture(CapabilityPokemob.getPokemobFor(entity));
    }

    @SuppressWarnings("unchecked")
    public ResourceLocation getEntityTexturePublic(Entity entity)
    {
        return this.getEntityTexture((T) entity);
    }

    protected ResourceLocation getPokemobTexture(IPokemob entity)
    {
        if (entity == null) return null;
        IPokemob mob = entity;
        IPokemob transformed = CapabilityPokemob.getPokemobFor(mob.getTransformedTo());
        if (transformed != null)
        {
            int num = mob.getPokedexNb();
            if (num == 132)
            {
                int rngval = entity.getRNGValue();
                if (rngval % 20 == 0) { return mob.getTexture(); }
            }
            mob = transformed;
        }
        return mob.getTexture();
    }

    protected void postRenderCallback()
    {
        // Reset to original state. This fixes changes to guis when rendered in
        // them.
        if (!normalize) GL11.glDisable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
    }

    @Override
    protected void preRenderCallback(T entity, float f)
    {
        blend = GL11.glGetBoolean(GL11.GL_BLEND);
        normalize = GL11.glGetBoolean(GL11.GL_NORMALIZE);
        src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        if (!normalize) GL11.glEnable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float yaw, float partialTick)
    {
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob == null) return;
        GL11.glPushMatrix();
        preRenderCallback(entity, partialTick);
        this.renderLivingAt(entity, x, y, z);
        GL11.glPushMatrix();
        int ticks = entity.ticksExisted;
        boolean exitCube = mob.getGeneralState(GeneralStates.EXITINGCUBE);
        if (exitCube && ticks <= 5 && !(partialTick <= 1))
        {
            float max = 5;// ;
            float s = (ticks) / max;
            GL11.glScalef(s, s, s);
        }
        if ((partialTick <= 1))
        {
            if (mob.isEvolving()) renderEvolution(mob, partialTick);
            if (exitCube) renderExitCube(mob, partialTick);
        }
        GL11.glPopMatrix();
        float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
        float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
        float f2 = f1 - f;
        float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick;
        float f8 = this.handleRotationFloat(entity, partialTick);
        this.applyRotations(entity, f8, f, partialTick);

        // This section here is what was prepareScale
        float f4 = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!entity.isRiding())
        {
            f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;
            f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTick);

            if (entity.isChild())
            {
                f6 *= 3.0F;
            }

            if (f5 > 1.0F)
            {
                f5 = 1.0F;
            }
            f2 = f1 - f; // Forge: Fix MC-1207
        }

        this.mainModel.setLivingAnimations(entity, f6, f5, partialTick);
        this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);

        if (mob.getStatus() == IMoveConstants.STATUS_SLP || mob.getLogicState(LogicStates.SLEEPING))
        {
            f6 = f5 = 0;
            f2 = -40;
            f7 = 19;
        }
        this.renderModel(entity, f6, f5, f8, f2, f7, f4);
        this.postRenderCallback();
        GL11.glPopMatrix();

    }
}

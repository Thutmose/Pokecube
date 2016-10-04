package pokecube.core.client.render.entity;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
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
import pokecube.core.client.Resources;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.helper.EntityEvolvablePokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.core.client.render.model.IModelRenderer;

@SideOnly(Side.CLIENT)
public class RenderPokemob<T extends EntityLiving> extends RenderPokemobInfos<T>
{
    public static final ResourceLocation FRZ = Resources.STATUS_FRZ;
    public static final ResourceLocation PAR = Resources.STATUS_PAR;

    public static void renderEvolution(IPokemob pokemob, float par2)
    {
        float f1 = 0, f2 = 0;

        boolean evolving = pokemob.getEvolutionTicks() > 0;

        if (evolving)
        {
            f1 = (pokemob.getEvolutionTicks() + par2) / 200.0F;
            f2 = 0.0F;
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer worldrenderer = tessellator.getBuffer();
            RenderHelper.disableStandardItemLighting();

            if (f1 > 0.8F)
            {
                f2 = (f1 - 0.8F) / 0.2F;
            }

            Random random = new Random(432L);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, pokemob.getPokedexEntry().height * pokemob.getSize() / 2, 0.0F);
            PokedexEntry entry = pokemob.getPokedexEntry();
            if (pokemob instanceof EntityEvolvablePokemob)
            {
                if ((entry = Database.getEntry(((EntityPokemob) pokemob).getEvolNumber())) == null)
                {
                    entry = pokemob.getPokedexEntry();
                }
            }

            int color1 = entry.getType1().colour;
            int color2 = entry.getType2().colour;

            if (entry.getType2() == PokeType.unknown)
            {
                color2 = color1;
            }

            Color col1 = new Color(color1);
            Color col2 = new Color(color2);

            float scale = pokemob.getPokedexEntry().length;
            for (int i = 0; i < (f1 + f1 * f1) / 2.0F * 60.0F; ++i)
            {
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
                float f4 = random.nextFloat() * 20.0F * scale + 5.0F * scale + f2 * 10.0F;
                float f3 = random.nextFloat() * 2.0F * scale + 1.0F * scale + f2 * 2.0F;
                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 0.0D, 0.0D)
                        .color(col1.getRed(), col1.getGreen(), col1.getBlue(), (int) (255.0F * (1.0F - f1)))
                        .endVertex();
                worldrenderer.pos(-0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                        .endVertex();
                worldrenderer.pos(0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                        .endVertex();
                worldrenderer.pos(0.0D, f4, 1.0F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                        .endVertex();
                worldrenderer.pos(-0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                        .endVertex();
                tessellator.draw();
            }
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            RenderHelper.enableStandardItemLighting();
        }
    }

    public static void renderExitCube(IPokemob pokemob, float partialTick)
    {
        if (!pokemob.getPokemonAIState(IMoveConstants.EXITINGCUBE)) return;

        int ticks = ((Entity) pokemob).ticksExisted;
        if (ticks > 20) return;
        NBTTagCompound sealTag = PokecubeManager.getSealTag((Entity) pokemob);
        if (sealTag != null && !sealTag.hasNoTags())
        {
            Entity entity = (Entity) pokemob;
            Random rand = new Random();
            Vector3 loc = Vector3.getNewVector().set(entity);
            Vector3 vel = Vector3.getNewVector();
            if (sealTag.getBoolean("Bubbles"))
            {
                System.out.println("bubble");
                loc.x += rand.nextDouble() - 0.5;
                loc.y += rand.nextDouble();
                loc.z += rand.nextDouble() - 0.5;
                PokecubeMod.core.spawnParticle(EnumParticleTypes.DRIP_WATER.getParticleName(), loc, vel);
            }
            if (sealTag.getBoolean("Flames"))
            {
                loc.x += rand.nextDouble() - 0.5;
                loc.y += rand.nextDouble();
                loc.z += rand.nextDouble() - 0.5;
                PokecubeMod.core.spawnParticle(EnumParticleTypes.FLAME.getParticleName(), loc, vel);
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
                PokecubeMod.core.spawnParticle("leaf", loc, vel);
            }
            if (sealTag.hasKey("dye"))
            {
                vel.x = rand.nextGaussian() / 100;
                vel.y = rand.nextGaussian() / 100;
                vel.z = rand.nextGaussian() / 100;
                loc.x += rand.nextGaussian() / 2;
                loc.y += rand.nextGaussian() / 2;
                loc.z += rand.nextGaussian() / 2;
                PokecubeMod.core.spawnParticle("powder", loc, vel,
                        ItemDye.DYE_COLORS[sealTag.getInteger("dye")] | 0xFF000000);
            }
        }
        float f1 = ((float) ticks * 5 + partialTick) / 200.0F;
        float f2 = 0.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        RenderHelper.disableStandardItemLighting();

        if (f1 > 0.8F)
        {
            f2 = (f1 - 0.8F) / 0.2F;
        }

        Random random = new Random(432L);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, pokemob.getPokedexEntry().height * pokemob.getSize() / 2, 0.0F);

        int color1 = pokemob.getType1().colour;
        int color2 = pokemob.getType2().colour;

        if (pokemob.getType2() == PokeType.unknown)
        {
            color2 = color1;
        }
        Color col1 = new Color(color1);
        Color col2 = new Color(color2);

        float scale = pokemob.getPokedexEntry().length;
        for (int i = 0; i < (f1 + f1 * f1) / 2.0F * 60.0F; ++i)
        {
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
            float f4 = random.nextFloat() * 20.0F * scale + 5.0F * scale + f2 * 10.0F;
            float f3 = random.nextFloat() * 2.0F * scale + 1.0F * scale + f2 * 2.0F;
            worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(0.0D, 0.0D, 0.0D)
                    .color(col1.getRed(), col1.getGreen(), col1.getBlue(), (int) (255.0F * (1.0F - f1))).endVertex();
            worldrenderer.pos(-0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            worldrenderer.pos(0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            worldrenderer.pos(0.0D, f4, 1.0F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            worldrenderer.pos(-0.866D * f3, f4, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            tessellator.draw();
        }

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderHelper.enableStandardItemLighting();
    }

    public static <V extends EntityLiving> void renderStatus(IModelRenderer<V> renderer, V entity, double d, double d1,
            double d2, float f, float partialTick)
    {
        IPokemob pokemob = (IPokemob) entity;
        byte status;
        if ((status = pokemob.getStatus()) == IMoveConstants.STATUS_NON) return;
        ResourceLocation texture = null;
        if (status == IMoveConstants.STATUS_FRZ)
        {
            texture = FRZ;
        }
        else if (status == IMoveConstants.STATUS_PAR)
        {
            texture = PAR;
        }
        if (texture == null) return;

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);

        float time = (((Entity) pokemob).ticksExisted + partialTick);
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
        var7 = status == IMoveConstants.STATUS_FRZ ? 1.08f : 1.05F;
        GL11.glScalef(var7, var7, var7);

        IMobColourable colour = (IMobColourable) entity;
        int[] col = colour.getRGBA();
        int[] bak = col.clone();
        col[3] = 85;
        colour.setRGBA(col);
        renderer.doRender(entity, d, d1, d2, f, partialTick);
        colour.setRGBA(bak);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

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
        return getPokemobTexture((IPokemob) entity);
    }

    @SuppressWarnings("unchecked")
    public ResourceLocation getEntityTexturePublic(Entity entity)
    {
        return this.getEntityTexture((T) entity);
    }

    protected ResourceLocation getPokemobTexture(IPokemob entity)
    {
        if (entity == null) return null;
        ResourceLocation texture = entity.getTexture();
        return texture;
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
    protected void renderModel(T entity, float walktime, float walkspeed, float time, float rotationYaw,
            float rotationPitch, float partialTicks)
    {
        GL11.glPushMatrix();
        preRenderCallback(entity, partialTicks);
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            int ticks = ((Entity) mob).ticksExisted;
            if (mob.getPokemonAIState(IMoveConstants.EXITINGCUBE) && ticks <= 5 && !(partialTicks <= 1))
            {
                float max = 5;// ;
                float s = (ticks) / max;
                GL11.glScalef(s, s, s);
            }
        }
        if ((time <= 1))
        {
            renderEvolution((IPokemob) entity, partialTicks);
            renderExitCube((IPokemob) entity, partialTicks);
        }
        if (time > 1)
        {
            long t = Minecraft.getMinecraft().theWorld.getWorldTime() % 1000;
            super.renderModel(entity, t / 3f, 0.6f, t, rotationYaw, rotationPitch, partialTicks);
        }
        else if (((IPokemob) entity).getStatus() == IMoveConstants.STATUS_SLP
                || ((IPokemob) entity).getPokemonAIState(IMoveConstants.SLEEPING))
        {
            super.renderModel(entity, 0, 0, 0, -40, 19, partialTicks);
        }
        else super.renderModel(entity, walktime, walkspeed, time, rotationYaw, rotationPitch, partialTicks);
        this.postRenderCallback();
        GL11.glPopMatrix();

    }

    @Override
    protected void rotateCorpse(T par1EntityLiving, float par2, float par3, float par4)
    {
        super.rotateCorpse(par1EntityLiving, par2, par3, par4);
        if (((IPokemob) par1EntityLiving).getStatus() == IMoveConstants.STATUS_SLP
                || ((IPokemob) par1EntityLiving).getPokemonAIState(IMoveConstants.SLEEPING))
        {
            short timer = ((IPokemob) par1EntityLiving).getStatusTimer();
            // TODO see if this is ever called.
            float ratio = 1F;
            if (timer <= 200 && timer > 175)
            {
                ratio = 1F - ((timer - 175F) / 25F);
            }
            if (timer > 0 && timer <= 25)
            {
                ratio = 1F - ((25F - timer) / 25F);
            }
            // System.out.println("TIMER = "+timer+ " | RATIO = " + ratio);
            GL11.glTranslatef(0.5F * ratio, 0.2F * ratio, 0.0F);
            GL11.glRotatef(80 * ratio, 0.0F, 0.0F, 1F);
        }
    }
}

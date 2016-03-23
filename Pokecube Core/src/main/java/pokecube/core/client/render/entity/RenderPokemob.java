package pokecube.core.client.render.entity;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;

@SideOnly(Side.CLIENT)
public class RenderPokemob<T extends EntityLiving> extends RenderPokemobInfos<T>
{
    protected float     scale;
    protected ModelBase modelStatus;
    Vector3             v = Vector3.getNewVector();

    public RenderPokemob(RenderManager m, ModelBase modelbase, float shadowSize)
    {
        this(m, modelbase, shadowSize, 1.0F);
    }

    public RenderPokemob(RenderManager m, ModelBase modelbase, float shadowSize, float scale)
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

        this.scale = scale;
    }

    public RenderPokemob(ModelBase modelbase, float f, float shadowSize)
    {
        this(Minecraft.getMinecraft().getRenderManager(), modelbase, shadowSize, 1.0F);
    }

    @Override
    protected void preRenderCallback(T entity, float f)
    {
        preRenderScale(entity, f);
        updateCreeperScale((EntityLiving) entity, f);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void preRenderScale(Entity entity, float f)
    {
        GL11.glScalef(scale, scale, scale);
    }

    /** Updates creeper scale in prerender callback */
    protected void updateCreeperScale(EntityLiving par1EntityCreeper, float par2)
    {
        // float var4 = par1EntityCreeper.getCreeperFlashIntensity(par2);//TODO
        // findout what this was for
        // float var5 = 1.0F + MathHelper.sin(var4 * 100.0F) * var4 * 0.01F;
        //
        // if (var4 < 0.0F)
        // {
        // var4 = 0.0F;
        // }
        //
        // if (var4 > 1.0F)
        // {
        // var4 = 1.0F;
        // }
        //
        // var4 *= var4;
        // var4 *= var4;
        // float var6 = (1.0F + var4 * 0.4F) * var5;
        // float var7 = (1.0F + var4 * 0.1F) / var5;
        // GL11.glScalef(var6, var7, var6);
    }

    /** Returns an ARGB int color back. Args: entityLiving, lightBrightness,
     * partialTickTime */
    @Override
    protected int getColorMultiplier(T par1EntityLiving, float par2, float par3)
    {
        return super.getColorMultiplier(par1EntityLiving, par2, par3);
    }

    public float getScale()
    {
        return scale;
    }

    @Override
    protected void renderModel(T entity, float walktime, float walkspeed, float time, float rotationYaw,
            float rotationPitch, float scale)
    {

        GL11.glPushMatrix();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            float red = 1;
            float green = 1;
            float blue = 1;
            float alpha = 1;
            if (mob instanceof IMobColourable)
            {
                IMobColourable colour = (IMobColourable) mob;
                red = colour.getRGBA()[0];
                green = colour.getRGBA()[1];
                blue = colour.getRGBA()[2];
                alpha = colour.getRGBA()[3];
            }
            PokedexEntry entry = mob.getPokedexEntry();

            if (mob.getPokedexEntry().canSitShoulder && mob.getPokemonAIState(IPokemob.SHOULDER)
                    && ((Entity) mob).ridingEntity != null)
            {
                GL11.glTranslated(1 - entry.width / 2, 0, 0);
            }
            else if (mob.getPokemonAIState(IPokemob.HELD) && ((Entity) mob).ridingEntity instanceof EntityLivingBase)
            {
                Vector3 look = v.set(-0.5, 0.5, -0.5);
                GL11.glTranslated(look.x, ((Entity) mob).height + 1 - look.y, look.z);
            }
            int ticks = ((Entity) mob).ticksExisted;
            if (mob.getPokemonAIState(IPokemob.EXITINGCUBE) && ticks <= 5 && !(time == GuiPokedex.POKEDEX_RENDER))
            {
                float max = 5;// ;
                float s = (ticks) / max;
                GL11.glScalef(s, s, s);
            }
            boolean shouldColour = (red == blue && blue == green && green == 0);
            boolean redCheck = mob.getPokedexEntry().hasSpecialTextures[0] && red == 0;
            boolean blueCheck = mob.getPokedexEntry().hasSpecialTextures[1] && blue == 0;
            boolean greenCheck = mob.getPokedexEntry().hasSpecialTextures[2] && green == 0;
            shouldColour = shouldColour || (!redCheck && !blueCheck && !greenCheck);

            if (shouldColour) GL11.glColor4f(red, green, blue, alpha);
        }
        if ((time != GuiPokedex.POKEDEX_RENDER))
        {
            renderEvolution((IPokemob) entity, scale);
            renderExitCube((IPokemob) entity, scale);
        }
        if (time == GuiPokedex.POKEDEX_RENDER)
        {
            long t = Minecraft.getMinecraft().theWorld.getWorldTime() % 1000;
            super.renderModel(entity, t / 3f, 0.6f, t, rotationYaw, rotationPitch, scale);
        }
        else if (((IPokemob) entity).getStatus() == IMoveConstants.STATUS_SLP
                || ((IPokemob) entity).getPokemonAIState(IPokemob.SLEEPING))
        {
            super.renderModel(entity, 0, 0, 0, -40, 19, scale);
        }
        else super.renderModel(entity, walktime, walkspeed, time, rotationYaw, rotationPitch, scale);

        glDisable(GL_BLEND);
        GL11.glPopMatrix();

    }

    @Override
    protected void rotateCorpse(T par1EntityLiving, float par2, float par3, float par4)
    {
        super.rotateCorpse(par1EntityLiving, par2, par3, par4);
        if (((IPokemob) par1EntityLiving).getStatus() == IMoveConstants.STATUS_SLP
                || ((IPokemob) par1EntityLiving).getPokemonAIState(IPokemob.SLEEPING))
        {
            short timer = ((IPokemob) par1EntityLiving).getStatusTimer();

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

    // @Override
    // protected void renderEquippedItems(EntityLivingBase entity,
    // float par2) {
    // super.renderEquippedItems(entity, par2);
    // }

    public static void renderExitCube(IPokemob pokemob, float partialTick)
    {
        if (!pokemob.getPokemonAIState(IPokemob.EXITINGCUBE)) return;

        NBTTagCompound sealTag = PokecubeManager.getSealTag(PokecubeManager.pokemobToItem(pokemob));

        if (!sealTag.hasNoTags())
        {
            Entity entity = (Entity) pokemob;
            Random rand = new Random();
            double x;
            double y;
            double z;
            if (sealTag.getBoolean("Bubbles"))
            {
                x = rand.nextDouble() - 0.5;
                y = rand.nextDouble();
                z = rand.nextDouble() - 0.5;
                entity.worldObj.spawnParticle(EnumParticleTypes.DRIP_WATER, entity.posX + x, entity.posY + y,
                        entity.posZ + z, 0d, 0d, 0d);
            }
            if (sealTag.getBoolean("Flames"))
            {
                x = rand.nextDouble() - 0.5;
                y = rand.nextDouble();
                z = rand.nextDouble() - 0.5;
                entity.worldObj.spawnParticle(EnumParticleTypes.FLAME, entity.posX, entity.posY, entity.posZ, x / 10,
                        y / 10, z / 10);
            }
            // *
            if (sealTag.getBoolean("Leaves"))
            {
                x = rand.nextGaussian();
                y = rand.nextGaussian();
                z = rand.nextGaussian();
                // mod_Pokecube.getProxy().spawnParticle("leaves", entity.posX,
                // entity.posY, entity.posZ, x/100, y/100, z/100);
            }
            if (sealTag.hasKey("dye"))
            {
                x = rand.nextGaussian();
                y = rand.nextGaussian();
                z = rand.nextGaussian();
                // TODO find out how to get the dyes again.
                // mod_Pokecube.getProxy().spawnParticle("powder:"+ItemDye.field_150922_c[sealTag.getInteger("dye")],
                // entity.posX, entity.posY, entity.posZ, x/100, y/100, z/100);
            }

            // */

        }
        int ticks = ((Entity) pokemob).ticksExisted;
        if (ticks > 10) return;

        float f1 = ((float) ticks * 5 + partialTick) / 200.0F;
        float f2 = 0.0F;

        PTezzelator tez = PTezzelator.instance;
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
        GL11.glTranslatef(0.0F, 1.0F, 0.0F);

        int color1 = pokemob.getType1().colour;
        int color2 = pokemob.getType2().colour;

        for (int i = 0; i < (f1 + f1 * f1) / 2.0F * 60.0F; ++i)
        {
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
            tez.begin(6);
            float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            tez.color(color1, (int) (255.0F * (1.0F - f2)));
            tez.vertex(0.0D, 0.0D, 0.0D);
            tez.color(color2, 0);
            tez.vertex(-0.866D * f4, f3, -0.5F * f4);
            tez.vertex(0.866D * f4, f3, -0.5F * f4);
            tez.vertex(0.0D, f3, 1.0F * f4);
            tez.vertex(-0.866D * f4, f3, -0.5F * f4);
            tez.end();
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

    public static void renderEvolution(IPokemob pokemob, float par2)
    {
        PTezzelator tez = PTezzelator.instance;
        float f1 = 0, f2 = 0;

        boolean evolving = pokemob.getEvolutionTicks() > 0;

        if (evolving)
        {
            f1 = (pokemob.getEvolutionTicks() + par2) / 200.0F;
            f2 = 0.0F;
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
            GL11.glTranslatef(0.0F, 1.0F, 0.0F);

            int color1 = pokemob.getType1().colour;
            int color2 = pokemob.getType2().colour;

            for (int i = 0; i < (f1 + f1 * f1) / 2.0F * 60.0F; ++i)
            {
                GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
                tez.begin(6);
                float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
                float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
                tez.color(color1, (int) (255.0F * (1.0F - f2)));
                tez.vertex(0.0D, 0.0D, 0.0D);
                tez.color(color2, 0);
                tez.vertex(-0.866D * f4, f3, -0.5F * f4);
                tez.vertex(0.866D * f4, f3, -0.5F * f4);
                tez.vertex(0.0D, f3, 1.0F * f4);
                tez.vertex(-0.866D * f4, f3, -0.5F * f4);
                tez.end();
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

    HashMap<String, ResourceLocation> mobTextures = new HashMap<String, ResourceLocation>();

    protected ResourceLocation getPokemobTexture(IPokemob entity)
    {
        if (entity == null) return null;
        String texture = entity.getTexture();
        if (!mobTextures.containsKey(texture))
        {
            String modId = entity.getPokedexEntry().getModId();

            ResourceLocation test = new ResourceLocation(modId, texture);
            try
            {
                Minecraft.getMinecraft().getResourceManager().getResource(test);
            }
            catch (IOException e)
            {
                String tex = "textures/FRZ.png";
                test = new ResourceLocation("pokecube", tex);
                System.err.println("a Texture for " + entity + " Is missing:" + texture);
            }
            mobTextures.put(texture, test);
        }

        return mobTextures.get(texture);
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
}

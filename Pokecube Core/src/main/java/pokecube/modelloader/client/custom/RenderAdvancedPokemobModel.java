package pokecube.modelloader.client.custom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.client.render.entity.RenderPokemob;
import pokecube.core.client.render.entity.RenderPokemobInfos;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import pokecube.modelloader.client.custom.animation.AnimationLoader;
import pokecube.modelloader.client.tabula.TabulaPackLoader;
import pokecube.modelloader.client.tabula.TabulaPackLoader.TabulaModelSet;
import pokecube.modelloader.client.tabula.components.ModelJson;
import pokecube.modelloader.client.tabula.model.IModelParser;
import pokecube.modelloader.client.tabula.model.TabulaModelRenderer;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModel;
import pokecube.modelloader.client.tabula.model.tabula.TabulaModelParser;

public class RenderAdvancedPokemobModel<T extends EntityLiving> extends RenderLiving<T>
{
    static final ResourceLocation FRZ = new ResourceLocation(PokecubeMod.ID, "textures/FRZ.png");
    static final ResourceLocation PAR = new ResourceLocation(PokecubeMod.ID, "textures/PAR.png");

    public IModelRenderer<T> model;
    final String             modelName;
    public boolean           overrideAnim = false;
    public String            anim         = "";

    public RenderAdvancedPokemobModel(String name, float par2)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, par2);
        modelName = name;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doRender(T entity, double d0, double d1, double d2, float yaw, float partialTick)
    {
        IPokemob mob = (IPokemob) entity;
        T toRender = entity;
        if (mob.getTransformedTo() instanceof IPokemob)
        {
            toRender = (T) mob.getTransformedTo();
        }
        model = getRenderer(modelName, entity);

        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre((EntityLivingBase) entity, this, d0, d1, d2)))
            return;

        GL11.glPushMatrix();
        this.preRenderCallback(entity, partialTick);
        GL11.glPushMatrix();
        GL11.glTranslated(d0, d1, d2);
        if ((partialTick != GuiPokedex.POKEDEX_RENDER))
        {
            RenderPokemob.renderEvolution((IPokemob) entity, yaw);
            RenderPokemob.renderExitCube((IPokemob) entity, yaw);
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated(d0, d1, d2);
        if (model.getTexturer() == null)
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(getEntityTexture(entity));
        float f8 = this.handleRotationFloat(entity, partialTick);
        if (entity.getHealth() <= 0) this.rotateCorpse(entity, f8, yaw, partialTick);
        // Lighting
        func_177105_a(entity, partialTick);

        if (model instanceof TabulaModelRenderer)
        {
            model.setPhase(getPhase(((TabulaModelRenderer) model).set, entity, partialTick));
        }
        else model.setPhase(getPhase(null, entity, partialTick));
        model.doRender(toRender, d0, d1, d2, yaw, partialTick);
        model.renderStatus(toRender, d0, d1, d2, yaw, partialTick);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post((EntityLivingBase) entity, this, d0, d1, d2));
        GL11.glPopMatrix();
        renderHp(entity, d0, d1, d2, yaw, partialTick);
        GL11.glPopMatrix();
    }

    protected void renderHp(T entityliving, double d, double d1, double d2, float f, float f1)
    {
        if (RenderPokemobInfos.shouldShow(entityliving))
        {
            float f2 = 1.6F;
            float f3 = 0.01666667F * f2;
            GL11.glPushMatrix();
            EntityLivingBase player = Minecraft.getMinecraft().thePlayer;
            float dist = entityliving.getDistanceToEntity(player);
            if (dist < 2) GlStateManager.disableDepth();

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

            if (exp < 0 || !((IPokemob) entityliving).getPokemonAIState(IPokemob.TAMED))
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

            if (((IPokemob) entityliving).getPokemonAIState(IPokemob.TAMED)) // &&
                                                                             // ))
            {
                String n;// = ((IPokemob) entityliving).getDisplayName();
                int colour = renderManager.livingPlayer.equals(((IPokemob) entityliving).getPokemonOwner()) ? 0xFFFFFF
                        : 0xAAAAAA;
                if ((entityliving.hasCustomName()))
                {
                    n = entityliving.getCustomNameTag();
                }
                else
                {
                    n = ((IPokemob) entityliving).getPokemonDisplayName();
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
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GL11.glPopMatrix();
        }
    }

    protected void renderTabula(T entity, double d0, double d1, double d2, float f, float partialTick)
    {
        PokedexEntry entry = null;
        if (entity instanceof IPokemob) entry = ((IPokemob) entity).getPokedexEntry();
        else return;

        float f2 = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
        float f3 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
        float f4;
        if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase1 = (EntityLivingBase) entity.ridingEntity;
            f2 = this.interpolateRotation(entitylivingbase1.prevRenderYawOffset, entitylivingbase1.renderYawOffset,
                    partialTick);
            f4 = MathHelper.wrapAngleTo180_float(f3 - f2);

            if (f4 < -85.0F)
            {
                f4 = -85.0F;
            }

            if (f4 >= 85.0F)
            {
                f4 = 85.0F;
            }

            f2 = f3 - f4;

            if (f4 * f4 > 2500.0F)
            {
                f2 += f4 * 0.2F;
            }
        }

        f4 = this.handleRotationFloat(entity, partialTick);
        this.preRenderCallback(entity, partialTick);

        TabulaModelSet set = TabulaPackLoader.modelMap.get(entry);

        if (set == null)
        {
            System.err.println(entry);
            set = TabulaPackLoader.modelMap.get(entry.baseForme);
        }

        TabulaModel model = set.model;
        IModelParser<TabulaModel> parser = set.parser;

        if (model == null || parser == null) { return; }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        TabulaModelParser pars = ((TabulaModelParser) parser);
        ModelJson modelj = pars.modelMap.get(model);
        modelj.texturer = set.texturer;
        String phase = getPhase(set, entity, partialTick);
        if (set.animator != null)
        {
            phase = set.animator.modifyAnimation(entity, partialTick, phase);
        }
        boolean inSet = false;
        if (modelj.animationMap.containsKey(phase) || (inSet = set.loadedAnimations.containsKey(phase)))
        {
            if (!inSet) modelj.startAnimation(phase);
            else modelj.startAnimation(set.loadedAnimations.get(phase));
        }
        else if (modelj.isAnimationInProgress())
        {
            modelj.stopAnimation();
        }

        GlStateManager.rotate(180f, 0f, 0f, 1f);

        GlStateManager.rotate(entity.rotationYaw + 180, 0, 1, 0);

        set.rotation.rotations.glRotate();
        GlStateManager.translate(set.shift.x, set.shift.y, set.shift.z);
        GlStateManager.scale(set.scale.x, set.scale.y, set.scale.z);

        parser.render(model, entity);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    protected void preRenderCallback(T entity, float f)
    {
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private String getPhase(TabulaModelSet set, EntityLiving entity, float partialTick)
    {
        String phase = "idle";
        if (overrideAnim) { return anim; }

        ModelJson modelj = null;
        if (set != null) modelj = set.parser.modelMap.get(set.model);
        IPokemob pokemob = (IPokemob) entity;
        float walkspeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        boolean asleep = pokemob.getStatus() == IMoveConstants.STATUS_SLP
                || pokemob.getPokemonAIState(IMoveConstants.SLEEPING);

        if (asleep && hasPhase(set, modelj, "sleeping"))
        {
            phase = "sleeping";
            return phase;
        }
        if (asleep && hasPhase(set, modelj, "asleep"))
        {
            phase = "asleep";
            return phase;
        }
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING) && hasPhase(set, modelj, "sitting"))
        {
            phase = "sitting";
            return phase;
        }
        if (!entity.onGround && hasPhase(set, modelj, "flight"))
        {
            phase = "flight";
            return phase;
        }
        if (!entity.onGround && hasPhase(set, modelj, "flying"))
        {
            phase = "flying";
            return phase;
        }
        if (entity.isInWater() && hasPhase(set, modelj, "swimming"))
        {
            phase = "swimming";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && hasPhase(set, modelj, "walking"))
        {
            phase = "walking";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && hasPhase(set, modelj, "walk"))
        {
            phase = "walk";
            return phase;
        }

        return phase;
    }

    private boolean hasPhase(TabulaModelSet set, ModelJson modelj, String phase)
    {
        if (set == null && model != null) { return LoadedModel.DEFAULTPHASE.equals(phase)
                || model.getAnimations().containsKey(phase); }
        return set.loadedAnimations.containsKey(phase) || (modelj != null && modelj.animationMap.containsKey(phase));
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(entity);
    }

    @SuppressWarnings("rawtypes")
    public static IModelRenderer getRenderer(String name, EntityLiving entity)
    {
        return AnimationLoader.getModel(name);
    }

    public static boolean isHidden(String partIdentifier, TabulaModelSet set, IPokemob pokemob, boolean default_)
    {
        if (set != null && set.shearableIdents.contains(partIdentifier))
        {
            boolean shearable = ((IShearable) pokemob).isShearable(new ItemStack(Items.shears),
                    ((Entity) pokemob).worldObj, ((Entity) pokemob).getPosition());
            return !shearable;
        }
        return default_;
    }

    public static int getColour(String partIdentifier, TabulaModelSet set, IPokemob pokemob, int default_)
    {
        if (set != null && set.dyeableIdents.contains(partIdentifier))
        {
            int rgba = 0xFF000000;
            rgba += EnumDyeColor.byDyeDamage(pokemob.getSpecialInfo()).getMapColor().colorValue;
            return rgba;
        }
        return default_;
    }

}

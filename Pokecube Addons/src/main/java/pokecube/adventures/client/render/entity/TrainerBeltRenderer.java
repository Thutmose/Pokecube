package pokecube.adventures.client.render.entity;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Tools;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatWrapper;

public class TrainerBeltRenderer implements LayerRenderer<EntityLivingBase>
{
    X3dModel                          model;
    X3dModel                          model2;
    ResourceLocation                  belt_1    = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt1.png");
    ResourceLocation                  belt_2    = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt2.png");
    boolean                           enabled   = true;
    public boolean                    wearables = false;
    float[]                           offsetArr;

    private final RenderLivingBase<?> livingEntityRenderer;
    private IHasPokemobs              pokemobCap;

    public TrainerBeltRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
        model = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
        model2 = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
        wearables = Loader.isModLoaded("thut_wearables");
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (!enabled) return;
        this.pokemobCap = CapabilityHasPokemobs.getHasPokemobs(entitylivingbaseIn);
        if (this.pokemobCap == null)
        {
            enabled = false;
            return;
        }
        if (pokemobCap.countPokemon() <= 0) return;

        if ((this.livingEntityRenderer.getMainModel() instanceof ModelBiped))
        {
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.13125F, -0.105F);
                if (wearables) if ((offsetArr = thut.wearables.ThutWearables.renderOffsetsSneak.get(8)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
        }
        if (wearables) if ((offsetArr = thut.wearables.ThutWearables.renderOffsets.get(8)) != null)
        {
            GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
        }

        int brightness = entitylivingbaseIn.getBrightnessForRender();
        // First pass of render
        GL11.glPushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GL11.glPushMatrix();
        float dx = 0, dy = -.0f, dz = -0.6f;
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslatef(dx, dy, dz);
        float s = 0.525f;
        if (!CompatWrapper.isValid(entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.LEGS)))
        {
            s = 0.465f;
        }
        if (entitylivingbaseIn instanceof EntityAgeable && entitylivingbaseIn.isChild())
        {
            s *= 0.6;
            dz -= 0.525;
            
        }
        GL11.glScalef(s, s, s);
        this.livingEntityRenderer.bindTexture(belt_1);
        model.renderAll();
        GL11.glPopMatrix();
        // Second pass with colour.
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(s, s, s);
        this.livingEntityRenderer.bindTexture(belt_2);
        EnumDyeColor ret = EnumDyeColor.GRAY;
        Color colour = new Color(ret.getColorValue() + 0xFF000000);
        int[] col = { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
        for (IExtendedModelPart part : model2.getParts().values())
        {
            part.setRGBAB(col);
        }
        model2.renderAll();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        float rx = 0, ry = -90, rz = 0;
        float max = pokemobCap.getMaxPokemobCount();
        dx = dy = dz = 0;
        float x = 0.55f;
        float y = 0.275f;
        float p = 2 * x + 2 * y;
        s = p / max;
        for (int i = 0; i < max; i++)
        {
            ItemStack stack = pokemobCap.getPokemob(i);
            if (CompatWrapper.isValid(stack) && !Tools.isSameStack(stack, entitylivingbaseIn.getHeldItemMainhand()))
            {
                float j = i >= max / 2 ? i - (max / 2) : i;
                float l = (j + 1f) * s;
                dz = -(l <= x ? l : x);
                dx = -(l <= x ? 0 : (l - x));
                ry = -(l <= x ? 90 : i >= max / 2 ? 180 : 0);
                if (l == x)
                {
                    ry += i >= max / 2 ? -45 : 45;
                }
                if (i >= max / 2)
                {
                    dz = -dz;
                }
                GlStateManager.pushMatrix();
                GlStateManager.translate(dx + 0.25, dy, dz);
                GlStateManager.rotate(rx, 1, 0, 0);
                GlStateManager.rotate(ry, 0, 1, 0);
                GlStateManager.rotate(rz, 0, 0, 1);
                GlStateManager.scale(0.15, 0.15, 0.15);
                Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, stack, null);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        GL11.glColor3f(1, 1, 1);
        GL11.glPopMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}

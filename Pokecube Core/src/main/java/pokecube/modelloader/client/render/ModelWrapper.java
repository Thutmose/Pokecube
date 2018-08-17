package pokecube.modelloader.client.render;

import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.common.Config;
import thut.core.client.render.model.IModelRenderer;

public class ModelWrapper extends ModelBase
{
    final String      name;
    private ModelBase wrapped;

    public ModelWrapper(String name)
    {
        this.name = name;
        if (ModPokecubeML.preload || Config.instance.toPreload.contains(name)) checkWrapped();
    }

    public void setWrapped(ModelBase wrapped)
    {
        this.wrapped = wrapped;
    }

    private void checkWrapped()
    {
        if (wrapped == null)
        {
            IModelRenderer<?> model = AnimationLoader.getModel(name);
            if (model != null && model instanceof RenderLivingBase)
            {
                wrapped = ((RenderLivingBase<?>) model).getMainModel();
            }
            else wrapped = new ModelBiped();
        }
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        checkWrapped();
        wrapped.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTickTime)
    {
        checkWrapped();
        wrapped.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn)
    {
        checkWrapped();
        wrapped.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
    }

    @Override
    public ModelRenderer getRandomModelBox(Random rand)
    {
        checkWrapped();
        return wrapped.getRandomModelBox(rand);
    }

    @Override
    public TextureOffset getTextureOffset(String partName)
    {
        checkWrapped();
        return wrapped.getTextureOffset(partName);
    }

    @Override
    public void setModelAttributes(ModelBase model)
    {
        checkWrapped();
        wrapped.setModelAttributes(model);
    }
}

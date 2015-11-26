package pokecube.adventures.client.models.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelSitrus extends ModelBerry {
	  //fields
    ModelRenderer Berry_bottom;
    ModelRenderer Berry_lower_tip;
    ModelRenderer Berry_top;
    ModelRenderer Stem_base;
    ModelRenderer Stem_leaf;
  
  public ModelSitrus()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Berry_bottom = new ModelRenderer(this, 0, 0);
      Berry_bottom.addBox(-6F, 0F, -6F, 12, 8, 12);
      Berry_bottom.setRotationPoint(0F, 14F, 0F);
      Berry_bottom.setTextureSize(64, 64);
      Berry_bottom.mirror = true;
      setRotation(Berry_bottom, 0F, 0F, 0F);
      Berry_lower_tip = new ModelRenderer(this, 0, 54);
      Berry_lower_tip.addBox(-4F, 8F, -4F, 8, 2, 8);
      Berry_lower_tip.setRotationPoint(0F, 14F, 0F);
      Berry_lower_tip.setTextureSize(64, 64);
      Berry_lower_tip.mirror = true;
      setRotation(Berry_lower_tip, 0F, 0.7853982F, 0F);
      Berry_top = new ModelRenderer(this, 0, 20);
      Berry_top.addBox(-5F, -6F, -5F, 10, 6, 10);
      Berry_top.setRotationPoint(0F, 14F, 0F);
      Berry_top.setTextureSize(64, 64);
      Berry_top.mirror = true;
      setRotation(Berry_top, 0F, 0F, 0F);
      Stem_base = new ModelRenderer(this, 0, 36);
      Stem_base.addBox(-3F, -7F, -3F, 6, 1, 6);
      Stem_base.setRotationPoint(0F, 14F, 0F);
      Stem_base.setTextureSize(64, 64);
      Stem_base.mirror = true;
      setRotation(Stem_base, 0F, 0F, 0F);
      Stem_leaf = new ModelRenderer(this, 0, 44);
      Stem_leaf.addBox(-2.5F, -10F, -2.5F, 5, 3, 5);
      Stem_leaf.setRotationPoint(0F, 14F, 0F);
      Stem_leaf.setTextureSize(64, 64);
      Stem_leaf.mirror = true;
      setRotation(Stem_leaf, 0F, 0F, 0F);
  }
  
  @Override
public void render(float f5)
  {

	  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/Sitrus.png");
	  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	  
    Berry_bottom.render(f5);
    Berry_lower_tip.render(f5);
    Berry_top.render(f5);
    Stem_base.render(f5);
    Stem_leaf.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  
}

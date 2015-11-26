package pokecube.adventures.client.models.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;


public class ModelOran extends ModelBerry
{
  //fields
    ModelRenderer Berry_base;
    ModelRenderer Lower_ridge;
    ModelRenderer Frontal_ridge;
    ModelRenderer Rear_ridge;
    ModelRenderer Left_ridge;
    ModelRenderer Right_ridge;
    ModelRenderer Top_ridge;
    ModelRenderer Top_lump;
  
  public ModelOran()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Berry_base = new ModelRenderer(this, 0, 0);
      Berry_base.addBox(-5F, -5F, -5F, 10, 10, 10);
      Berry_base.setRotationPoint(0F, 18F, 0F);
      Berry_base.setTextureSize(64, 64);
      Berry_base.mirror = true;
      setRotation(Berry_base, 0F, 0F, 0F);
      Lower_ridge = new ModelRenderer(this, 0, 20);
      Lower_ridge.addBox(-4F, 5F, -4F, 8, 1, 8);
      Lower_ridge.setRotationPoint(0F, 18F, 0F);
      Lower_ridge.setTextureSize(64, 64);
      Lower_ridge.mirror = true;
      setRotation(Lower_ridge, 0F, 0F, 0F);
      Frontal_ridge = new ModelRenderer(this, 0, 20);
      Frontal_ridge.addBox(-4F, 5F, -4F, 8, 1, 8);
      Frontal_ridge.setRotationPoint(0F, 18F, 0F);
      Frontal_ridge.setTextureSize(64, 64);
      Frontal_ridge.mirror = true;
      setRotation(Frontal_ridge, -1.570796F, 0F, 0F);
      Rear_ridge = new ModelRenderer(this, 0, 20);
      Rear_ridge.addBox(-4F, 5F, -4F, 8, 1, 8);
      Rear_ridge.setRotationPoint(0F, 18F, 0F);
      Rear_ridge.setTextureSize(64, 64);
      Rear_ridge.mirror = true;
      setRotation(Rear_ridge, 1.570796F, 0F, 0F);
      Left_ridge = new ModelRenderer(this, 0, 20);
      Left_ridge.addBox(-4F, 5F, -4F, 8, 1, 8);
      Left_ridge.setRotationPoint(0F, 18F, 0F);
      Left_ridge.setTextureSize(64, 64);
      Left_ridge.mirror = true;
      setRotation(Left_ridge, 0F, 0F, -1.570796F);
      Right_ridge = new ModelRenderer(this, 0, 20);
      Right_ridge.addBox(-4F, 5F, -4F, 8, 1, 8);
      Right_ridge.setRotationPoint(0F, 18F, 0F);
      Right_ridge.setTextureSize(64, 64);
      Right_ridge.mirror = true;
      setRotation(Right_ridge, 0F, 0F, 1.570796F);
      Top_ridge = new ModelRenderer(this, 0, 29);
      Top_ridge.addBox(-4F, -5.5F, -4F, 8, 1, 8);
      Top_ridge.setRotationPoint(0F, 18F, 0F);
      Top_ridge.setTextureSize(64, 64);
      Top_ridge.mirror = true;
      setRotation(Top_ridge, 0F, 0F, 0F);
      Top_lump = new ModelRenderer(this, 0, 38);
      Top_lump.addBox(-2F, -7F, -2F, 4, 2, 4);
      Top_lump.setRotationPoint(0F, 18F, 0F);
      Top_lump.setTextureSize(64, 64);
      Top_lump.mirror = true;
      setRotation(Top_lump, 0F, 0F, 0F);
  }
  
  @Override
public void render(float f5)
  {

	  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/Oran.png");
	  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	  
    Berry_base.render(f5);
    Lower_ridge.render(f5);
    Frontal_ridge.render(f5);
    Rear_ridge.render(f5);
    Left_ridge.render(f5);
    Right_ridge.render(f5);
    Top_ridge.render(f5);
    Top_lump.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}

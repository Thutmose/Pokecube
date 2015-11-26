package pokecube.adventures.client.models.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelEnigma extends ModelBerry {
	  //fields
    ModelRenderer Berry_bottom;
    ModelRenderer Berry_middle;
    ModelRenderer Berry_upper;
    ModelRenderer Berry_tip;
    ModelRenderer Berry_ball_tip;
    ModelRenderer Stem;
  
  public ModelEnigma()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Berry_bottom = new ModelRenderer(this, 0, 0);
      Berry_bottom.addBox(-3F, 0F, -3F, 6, 1, 6);
      Berry_bottom.setRotationPoint(0F, 23F, 0F);
      Berry_bottom.setTextureSize(64, 64);
      Berry_bottom.mirror = true;
      setRotation(Berry_bottom, 0F, 0F, 0F);
      Berry_middle = new ModelRenderer(this, 0, 7);
      Berry_middle.addBox(-3.5F, -10F, -3.5F, 7, 10, 7);
      Berry_middle.setRotationPoint(0F, 23F, 0F);
      Berry_middle.setTextureSize(64, 64);
      Berry_middle.mirror = true;
      setRotation(Berry_middle, 0F, 0F, 0F);
      Berry_upper = new ModelRenderer(this, 0, 24);
      Berry_upper.addBox(-2.5F, -12F, -2.5F, 5, 2, 5);
      Berry_upper.setRotationPoint(0F, 23F, 0F);
      Berry_upper.setTextureSize(64, 64);
      Berry_upper.mirror = true;
      setRotation(Berry_upper, 0F, 0F, 0F);
      Berry_tip = new ModelRenderer(this, 0, 32);
      Berry_tip.addBox(-1.5F, -13F, -1.5F, 3, 1, 3);
      Berry_tip.setRotationPoint(0F, 23F, 0F);
      Berry_tip.setTextureSize(64, 64);
      Berry_tip.mirror = true;
      setRotation(Berry_tip, 0F, 0F, 0F);
      Berry_ball_tip = new ModelRenderer(this, 13, 32);
      Berry_ball_tip.addBox(-2F, -17F, -2F, 4, 4, 4);
      Berry_ball_tip.setRotationPoint(0F, 23F, 0F);
      Berry_ball_tip.setTextureSize(64, 64);
      Berry_ball_tip.mirror = true;
      setRotation(Berry_ball_tip, 0F, 0F, 0F);
      Stem = new ModelRenderer(this, 8, 37);
      Stem.addBox(-0.5F, -20F, -0.5F, 1, 3, 1);
      Stem.setRotationPoint(0F, 23F, 0F);
      Stem.setTextureSize(64, 64);
      Stem.mirror = true;
      setRotation(Stem, 0F, 0F, 0F);
  }
  
  @Override
public void render(float f5)
  {

	  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/Enigma.png");
	  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	  
    Berry_bottom.render(f5);
    Berry_middle.render(f5);
    Berry_upper.render(f5);
    Berry_tip.render(f5);
    Berry_ball_tip.render(f5);
    Stem.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
}

package pokecube.adventures.client.models.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelNanab extends ModelBerry {
	  //fields
    ModelRenderer Stem;
    ModelRenderer Stem_branch;
    ModelRenderer Berry_hand_base;
    ModelRenderer Berry_hand_pt_2;
  
  public ModelNanab()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Stem = new ModelRenderer(this, 0, 0);
      Stem.addBox(-5.5F, -1F, -1F, 11, 2, 2);
      Stem.setRotationPoint(0F, 7F, 0F);
      Stem.setTextureSize(64, 64);
      Stem.mirror = true;
      setRotation(Stem, 0F, 0F, 0F);
      Stem_branch = new ModelRenderer(this, 0, 4);
      Stem_branch.addBox(-1F, 1F, -1F, 2, 2, 2);
      Stem_branch.setRotationPoint(0F, 7F, 0F);
      Stem_branch.setTextureSize(64, 64);
      Stem_branch.mirror = true;
      setRotation(Stem_branch, 0F, 0F, 0F);
      Berry_hand_base = new ModelRenderer(this, 0, 8);
      Berry_hand_base.addBox(-5F, 3F, -2F, 10, 7, 4);
      Berry_hand_base.setRotationPoint(0F, 7F, 0F);
      Berry_hand_base.setTextureSize(64, 64);
      Berry_hand_base.mirror = true;
      setRotation(Berry_hand_base, 0.0872665F, 0F, 0F);
      Berry_hand_pt_2 = new ModelRenderer(this, 0, 19);
      Berry_hand_pt_2.addBox(-5F, 7F, 2.5F, 10, 9, 5);
      Berry_hand_pt_2.setRotationPoint(0F, 7F, 0F);
      Berry_hand_pt_2.setTextureSize(64, 64);
      Berry_hand_pt_2.mirror = true;
      setRotation(Berry_hand_pt_2, -0.5235988F, 0F, 0F);
  }
  
  @Override
public void render(float f5)
  {

	  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/nanab.png");
	  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	  
    Stem.render(f5);
    Stem_branch.render(f5);
    Berry_hand_base.render(f5);
    Berry_hand_pt_2.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
}

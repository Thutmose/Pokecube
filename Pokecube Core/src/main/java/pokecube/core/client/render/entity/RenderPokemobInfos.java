package pokecube.core.client.render.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;

@SideOnly(Side.CLIENT)
public abstract class RenderPokemobInfos<T extends EntityLiving> extends RenderLiving<T>
{
    public RenderPokemobInfos(RenderManager m, ModelBase modelbase, float shadowSize)
    {
        super(m, modelbase, shadowSize);
    }

    public static boolean shouldShow(EntityLivingBase entity)
    {
        if (((Minecraft) PokecubeCore.getMinecraftInstance()).gameSettings.hideGUI || entity.riddenByEntity != null)
        {
            return false;
        }

        EntityLivingBase player = Minecraft.getMinecraft().thePlayer;
        if(!entity.addedToChunk || entity.ridingEntity == player)return false;
        float d = entity.getDistanceToEntity(player);
        IPokemob pokemob = ((IPokemob) entity);
        boolean tameFactor = pokemob.getPokemonAIState(IPokemob.TAMED) && !pokemob.getPokemonAIState(IPokemob.STAYING); 
        if (( tameFactor && d < 35) || d < 8)
        {
            return true;
        }
        else
        {
            Entity target = ((EntityCreature) entity).getAttackTarget();
            return (player.equals(target) || ((IPokemob) entity).getPokemonAIState(IPokemob.TAMED));
        }
    }

    @Override
    public void doRender(T entityliving, double d, double d1, double d2, float f, float f1)
    {
        super.doRender(entityliving, d, d1, d2, f, f1);

        if (shouldShow(entityliving) && d1 != -0.123456)
        {
            float f2 = 1.6F;
            float f3 = 0.01666667F * f2;
            GL11.glPushMatrix();
            GL11.glTranslatef((float) d + 0.0F, (float) d1 + entityliving.height, (float) d2);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-f3, -f3, f3);
            GL11.glDisable(GL11.GL_LIGHTING);
//			GL11.glEnable(GL11.GL_BLEND);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            int j = 0xF0F0F0;//61680;
            int k = j % 0x000100;
            int l = j / 0xFFFFFF;
            GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, k / 1.0F, l / 1.0F);
            PTezzelator tez = PTezzelator.instance;
            int offset = -25;
            double width = 2;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tez.begin();
            int length = 40;
            float health = entityliving.getHealth();//getHealth()

            if (health < 0)
            {
                health = 0;
            }

            float maxHealth = entityliving.getMaxHealth();//getMaxHealth()
            float f7 = health / maxHealth;
            float f8 = length * 2 * f7;
            tez.color(0.3F, 0.3F, 0.3F, 1.0F);
            tez.vertex(-length + f8, offset, 0.0D);
            tez.vertex(-length + f8, offset + width, 0.0D);
            tez.vertex(length, offset + width, 0.0D);
            tez.vertex(length, offset, 0.0D);
            tez.color(1, 1, 1);
            int healthColor = 0x00FF00;

            if (f7 > 0.6F)
            {
                tez.color(0.0F, 1.0F, 0.0F, 1.0F);
            }
            else
            {
                tez.color(1.0F, f7, 0.0F, 1.0F);
                healthColor = 0xFF0000 + (((int)(0xFF * f7)) * 0x100);
            }

            tez.vertex(-length, offset, 0.0D);
            tez.vertex(-length, offset + width, 0.0D);
            tez.vertex(f8 - length, offset + width, 0.0D);
            tez.vertex(f8 - length, offset, 0.0D);
            int exp = ((IPokemob) entityliving).getExp()
                    - Tools.levelToXp(((IPokemob) entityliving).getExperienceMode(), ((IPokemob) entityliving).getLevel());
            float maxExp = Tools.levelToXp(((IPokemob) entityliving).getExperienceMode(), ((IPokemob) entityliving).getLevel() + 1)
                    - Tools.levelToXp(((IPokemob) entityliving).getExperienceMode(), ((IPokemob) entityliving).getLevel());

            if(((IPokemob) entityliving).getLevel() == 100)
            	maxExp = exp = 1;
            
            
            if (exp < 0 || !((IPokemob) entityliving).getPokemonAIState(IPokemob.TAMED))
            {
                exp = 0;
            }
            
            float f62 = maxExp;
            float f72 = exp / f62;
            float f82 = length * 2 * f72;
            tez.color(0.2F, 0.2F, 0.2F, 1.0F);
            tez.vertex(-length + f82, offset + width, 0.0D);
            tez.vertex(-length + f82, offset + width + 1, 0.0D);
            tez.vertex(length, offset + width + 1, 0.0D);
            tez.vertex(length, offset + width, 0.0D);
            tez.color(1, 1, 1);
            tez.color(0.3F, 0.3F, 0.8F, 1.0F);
            tez.vertex(-length, offset + width, 0.0D);
            tez.vertex(-length, offset + width + 1, 0.0D);
            tez.vertex(f82 - length, offset + width + 1, 0.0D);
            tez.vertex(f82 - length, offset + width, 0.0D);
            tez.end();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            FontRenderer fontrenderer = getFontRendererFromRenderManager();

            if (((IPokemob) entityliving).getPokemonAIState(IPokemob.TAMED) && renderManager.livingPlayer.equals(((IPokemob) entityliving).getPokemonOwner()))
            {
                fontrenderer = getFontRendererFromRenderManager();
                String s = (int)health + "/" + (int)maxHealth;
                
                fontrenderer.drawString(s, length - fontrenderer.getStringWidth(s), offset + 5, healthColor);
         
            }
            
            //Nickname code
            
          if (((IPokemob) entityliving).getPokemonAIState(IPokemob.TAMED))// && ))
          {
          	String n;// = ((IPokemob) entityliving).getDisplayName();
          	int colour = renderManager.livingPlayer.equals(((IPokemob) entityliving).getPokemonOwner())?0xFFFFFF:0xAAAAAA;
          	int dx = 00;
          	if((entityliving.hasCustomName())){
          		n = entityliving.getCustomNameTag();
          		fontrenderer.drawString(n, length - dx - fontrenderer.getStringWidth(n), offset-8, colour);
          		}else{
          			
          			n = ((IPokemob) entityliving).getPokemonDisplayName();
          			
          			if(n.length() == 8)
          			 fontrenderer.drawString(n, length - (int)( fontrenderer.getStringWidth(n)*1.45), offset-8, colour);
          			if(n.length() >= 9)
          				fontrenderer.drawString(n, length - (int)( fontrenderer.getStringWidth(n)*1.2), offset-8, colour);
          			if (n.length() < 8)
          				fontrenderer.drawString(n, length - (int)(fontrenderer.getStringWidth(n)*1.8), offset-8, colour);
          		}
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
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
    }
    
    public void setShadowSize(float size)
    {
    	this.shadowSize = size;
    }

    public ModelBase getMainModel()
    {
        return mainModel;
    }
}

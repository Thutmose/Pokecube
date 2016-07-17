package pokecube.core.moves.animations;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class AnimationPowder extends MoveAnimationBase
{

    String  particle;
    float   width        = 1;
    float   density      = 1;
    boolean reverse      = false;
    boolean customColour = false;

    int     meshId       = 0;

    public AnimationPowder(String particle)
    {
        this.particle = particle;
        duration = 50;
        for (EnumDyeColor colour : EnumDyeColor.values())
        {
            if (colour.getName().equalsIgnoreCase(particle))
            {
                rgba = colour.getMapColor().colorValue + 0xFF000000;
                break;
            }
        }

        String[] args = particle.split(":");
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            if (ident.equals("w"))
            {
                width = Float.parseFloat(val);
            }
            else if (ident.equals("d"))
            {
                density = Float.parseFloat(val);
            }
            else if (ident.equals("r"))
            {
                reverse = Boolean.parseBoolean(val);
            }
            else if (ident.equals("c"))
            {
                int alpha = 255;
                rgba = EnumDyeColor.byDyeDamage(Integer.parseInt(val)).getMapColor().colorValue + 0x01000000 * alpha;
                customColour = true;
            }
            else if (ident.equals("t"))
            {
                duration = Integer.parseInt(val);
            }
        }

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);

        if (!reverse)
        {
            Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);
            GlStateManager.translate(-temp.x, -temp.y, -temp.z);
        }

        initColour(info.currentTick * 300, partialTick, info.move);

        float alpha = ((rgba >> 24) & 255) / 255f;
        float red = ((rgba >> 16) & 255) / 255f;
        float green = ((rgba >> 8) & 255) / 255f;
        float blue = (rgba & 255) / 255f;

        Random rand = new Random(info.currentTick);

        Vector3f rot = new Vector3f(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
        rot.normalize();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(20, rot.x, rot.y, rot.z);
        GlStateManager.color(red, green, blue, alpha);
        if (PokecubeMod.core.getConfig().moveAnimationCallLists)
        {
            compileList();
            GlStateManager.callList(meshId);
        }
        else
        {
            draw();
        }
        GlStateManager.popMatrix();
    }

    private void compileList()
    {
        if (!GL11.glIsList(meshId))
        {
            meshId = GL11.glGenLists(1);
            GL11.glNewList(meshId, GL11.GL_COMPILE);
            draw();
            GL11.glEndList();
        }
    }

    private void draw()
    {
        GlStateManager.disableTexture2D();
        Vector3 temp = Vector3.getNewVector();
        Random rand = new Random();
        for (int i = 0; i < 500 * density; i++)
        {
            GL11.glBegin(GL11.GL_LINE_LOOP);
            temp.set(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
            temp.scalarMult(0.010 * width);
            double size = 0.01;

            GL11.glVertex3d(temp.x, temp.y + size, temp.z);
            GL11.glVertex3d(temp.x - size, temp.y - size, temp.z - size);
            GL11.glVertex3d(temp.x - size, temp.y + size, temp.z - size);
            GL11.glVertex3d(temp.x, temp.y - size, temp.z);

            GL11.glEnd();
        }
        GlStateManager.enableTexture2D();
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (particle.equals("airbubble"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
        }
        else if (particle.equals("aurora"))
        {
            int rand = ItemDye.DYE_COLORS[new Random(time / 10).nextInt(ItemDye.DYE_COLORS.length)];
            rgba = 0x61000000 + rand;
        }
        else if (particle.equals("iceshard"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
        }
        else if (!customColour)
        {
            rgba = getColourFromMove(move, 255);
        }
    }

}

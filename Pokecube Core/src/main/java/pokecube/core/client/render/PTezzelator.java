package pokecube.core.client.render;

import java.awt.Color;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.realms.RealmsVertexFormat;

public class PTezzelator
{
    public static Tessellator t = Tessellator.getInstance();
    public static final PTezzelator instance = new PTezzelator();
    
    public void begin()
    {
        t.getBuffer().begin(7, DefaultVertexFormats.POSITION_COLOR);
    }

    public void begin(int mode)
    {
        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;
        switch (mode)
        {
        case 0:
            format = DefaultVertexFormats.POSITION_COLOR;
        default:
            format = DefaultVertexFormats.POSITION_COLOR;
        }

        t.getBuffer().begin(mode, format);
    }

    public PTezzelator begin(int p_begin_1_, RealmsVertexFormat p_begin_2_)
    {
        t.getBuffer().begin(p_begin_1_, p_begin_2_.getVertexFormat());
        return this;
    }

    public void begin(int mode, VertexFormat format)
    {
        t.getBuffer().begin(mode, format);
    }

    public PTezzelator color(float p_color_1_, float p_color_2_, float p_color_3_, float p_color_4_)
    {
        t.getBuffer().color(p_color_1_, p_color_2_, p_color_3_, p_color_4_);
        return this;
    }

    public PTezzelator color(int rgb)
    {
        Color color = new Color(rgb);
        instance.color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        return this;
    }
    
    public PTezzelator color(int rgb, int a)
    {
        Color color = new Color(rgb);
        instance.color(color.getRed(), color.getGreen(), color.getBlue(), a);
        return this;
    }

    public PTezzelator color(int r, int b, int g)
    {
        instance.color(r, g, b, 255);
        return this;
    }

    public PTezzelator color(int p_color_1_, int p_color_2_, int p_color_3_, int p_color_4_)
    {
        t.getBuffer().color(p_color_1_, p_color_2_, p_color_3_, p_color_4_);
        return this;
    }

    public void end()
    {
        t.draw();
    }

    public void endVertex()
    {
        t.getBuffer().endVertex();
    }

    public  PTezzelator  normal(float p_normal_1_, float p_normal_2_, float p_normal_3_)
    {
        t.getBuffer().normal(p_normal_1_, p_normal_2_, p_normal_3_);
        return this;
    }

    public PTezzelator offset(double p_offset_1_, double p_offset_3_, double p_offset_5_)
    {
        t.getBuffer().setTranslation(p_offset_1_, p_offset_3_, p_offset_5_);
        return this;
    }

    public PTezzelator tex(double p_tex_1_, double p_tex_3_)
    {
        t.getBuffer().tex(p_tex_1_, p_tex_3_);
        return this;
    }

    public PTezzelator tex2(short p_tex2_1_, short p_tex2_2_)
    {
        t.getBuffer().lightmap(p_tex2_1_, p_tex2_2_);
        return this;
    }

    public PTezzelator vertex(double p_vertex_1_, double p_vertex_3_, double p_vertex_5_)
    {
        t.getBuffer().pos(p_vertex_1_, p_vertex_3_, p_vertex_5_);
        return this;
    }
}

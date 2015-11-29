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
    
    public PTezzelator color(int r, int b, int g)
    {
        instance.color(r, g, b, 255);
        return this;
    }

    public PTezzelator color(int rgb, int a)
    {
        Color color = new Color(rgb);
        instance.color(color.getRed(), color.getGreen(), color.getBlue(), a);
        return this;
    }

    public PTezzelator color(int rgb)
    {
        Color color = new Color(rgb);
        instance.color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        return this;
    }

    public void begin()
    {
        t.getWorldRenderer().func_181668_a(7, DefaultVertexFormats.field_181706_f);
    }

    public void begin(int mode)
    {
        VertexFormat format = DefaultVertexFormats.field_181706_f;
        switch (mode)
        {
        case 0:
            format = DefaultVertexFormats.field_181706_f;
        default:
            format = DefaultVertexFormats.field_181706_f;
        }

        t.getWorldRenderer().func_181668_a(mode, format);
    }

    public void begin(int mode, VertexFormat format)
    {
        t.getWorldRenderer().func_181668_a(mode, format);
    }
    
    public void end()
    {
        t.draw();
    }

    public PTezzelator vertex(double p_vertex_1_, double p_vertex_3_, double p_vertex_5_)
    {
        t.getWorldRenderer().func_181662_b(p_vertex_1_, p_vertex_3_, p_vertex_5_);
        return this;
    }

    public PTezzelator color(float p_color_1_, float p_color_2_, float p_color_3_, float p_color_4_)
    {
        t.getWorldRenderer().func_181666_a(p_color_1_, p_color_2_, p_color_3_, p_color_4_);
        return this;
    }

    public PTezzelator tex2(short p_tex2_1_, short p_tex2_2_)
    {
        t.getWorldRenderer().func_181671_a(p_tex2_1_, p_tex2_2_);
        return this;
    }

    public  PTezzelator  normal(float p_normal_1_, float p_normal_2_, float p_normal_3_)
    {
        t.getWorldRenderer().func_181663_c(p_normal_1_, p_normal_2_, p_normal_3_);
        return this;
    }

    public PTezzelator begin(int p_begin_1_, RealmsVertexFormat p_begin_2_)
    {
        t.getWorldRenderer().func_181668_a(p_begin_1_, p_begin_2_.getVertexFormat());
        return this;
    }

    public void endVertex()
    {
        t.getWorldRenderer().func_181675_d();
    }

    public PTezzelator offset(double p_offset_1_, double p_offset_3_, double p_offset_5_)
    {
        t.getWorldRenderer().setTranslation(p_offset_1_, p_offset_3_, p_offset_5_);
        return this;
    }

    public PTezzelator color(int p_color_1_, int p_color_2_, int p_color_3_, int p_color_4_)
    {
        t.getWorldRenderer().func_181669_b(p_color_1_, p_color_2_, p_color_3_, p_color_4_);
        return this;
    }

    public PTezzelator tex(double p_tex_1_, double p_tex_3_)
    {
        t.getWorldRenderer().func_181673_a(p_tex_1_, p_tex_3_);
        return this;
    }
}

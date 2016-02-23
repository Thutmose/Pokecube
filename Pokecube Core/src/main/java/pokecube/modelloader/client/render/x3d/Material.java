package pokecube.modelloader.client.render.x3d;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Material
{
    public final String   name;
    public final String   texture;
    public final Vector3f diffuseColor;
    public final Vector3f specularColor;
    public final Vector3f emissiveColor;
    public final float    ambientIntensity;
    public final float    shininess;
    public final float    transparency;

    public Material(String name, String texture, Vector3f diffuse, Vector3f specular, Vector3f emissive, float ambient,
            float shiny, float transparent)
    {
        this.name = name;
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.ambientIntensity = ambient;
        this.shininess = shiny;
        this.transparency = transparent;
    }

    public void preRender()
    {
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, makeBuffer(ambientIntensity));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, makeBuffer(diffuseColor));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, makeBuffer(specularColor));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, makeBuffer(shininess));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, makeBuffer(emissiveColor));

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public void postRender()
    {

    }

    private FloatBuffer makeBuffer(Vector3f vector)
    {
        FloatBuffer ret = BufferUtils.createFloatBuffer(3 + 4);
        ret.put(new float[] { vector.x, vector.y, vector.z });
        return ret;
    }

    private FloatBuffer makeBuffer(float value)
    {
        FloatBuffer ret = BufferUtils.createFloatBuffer(1 + 4);
        ret.put(new float[] { value });
        return ret;
    }
}

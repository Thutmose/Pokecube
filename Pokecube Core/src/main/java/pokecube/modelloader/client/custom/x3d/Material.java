package pokecube.modelloader.client.custom.x3d;

import javax.vecmath.Vector3f;

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
}

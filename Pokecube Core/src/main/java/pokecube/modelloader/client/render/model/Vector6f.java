package pokecube.modelloader.client.render.model;

import org.lwjgl.util.vector.Vector3f;

public class Vector6f
{
    public Vector3f vector1;
    /** when used for rotation is Euler angles in radians */
    public Vector3f vector2;

    public Vector6f(float x, float y, float z, float x1, float y1, float z1)
    {
        vector1 = new Vector3f(x, y, z);
        vector2 = new Vector3f(x1, y1, z1);
    }

    public String toString()
    {
        return vector1 + " " + vector2;
    }
}

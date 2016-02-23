package pokecube.modelloader.client.render.model;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class VectorMath
{
    static final Vector3f     X_AXIS    = new Vector3f(1.0F, 0.0F, 0.0F);
    static final Vector3f     Y_AXIS    = new Vector3f(0.0F, 1.0F, 0.0F);
    static final Vector3f     Z_AXIS    = new Vector3f(0.0F, 0.0F, 1.0F);
    public static final float toDegrees = 57.29578F;
    public static final float toRadians = 0.01745329F;

    public static double[] rotate(double x, double y, double radians)
    {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double newX = x * cos - y * sin;
        double newY = y * cos + x * sin;
        return new double[] { newX, newY };
    }

    public static Matrix4f fromVector6f(float xl, float yl, float zl, float xr, float yr, float zr)
    {
        Vector3f loc = new Vector3f(xl, yl, zl);
        Matrix4f ret = new Matrix4f();
        ret.translate(loc);
        ret.rotate(zr, Z_AXIS);
        ret.rotate(yr, Y_AXIS);
        ret.rotate(xr, X_AXIS);
        return ret;
    }

    public static Matrix4f fromFloatArray(float[] vals)
    {
        return fromVector6f(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
    }

    public static Matrix4f fromFloat(float val)
    {
        return fromVector6f(val, val, val, val, val, val);
    }

    public static Matrix4f fromVector6f(Vector6f vector)
    {
        Matrix4f ret = new Matrix4f();
        ret.translate(vector.vector1);
        ret.rotate(vector.vector2.x, Z_AXIS);
        ret.rotate(vector.vector2.y, Y_AXIS);
        ret.rotate(vector.vector2.x, X_AXIS);
        return ret;
    }

    public static Vector4f mul(Vector4f target, float factor, Vector4f dest)
    {
        if (dest == null)
        {
            dest = new Vector4f();
        }
        target.x *= factor;
        target.y *= factor;
        target.z *= factor;
        target.w *= factor;
        return dest;
    }

    public static Matrix4f mul(Matrix4f target, float factor, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }
        target.m00 *= factor;
        target.m01 *= factor;
        target.m02 *= factor;
        dest.m02 = (target.m03 * factor);
        target.m10 *= factor;
        target.m11 *= factor;
        target.m12 *= factor;
        target.m13 *= factor;
        target.m20 *= factor;
        target.m21 *= factor;
        target.m22 *= factor;
        target.m23 *= factor;
        target.m30 *= factor;
        target.m31 *= factor;
        target.m32 *= factor;
        target.m33 *= factor;
        return target;
    }

    public static float[] toFloatArray(Matrix4f target)
    {
        return new float[] { target.m30, target.m31, target.m32 };
    }

    public static Vector3f toVector3f(Matrix4f target)
    {
        return new Vector3f(target.m30, target.m31, target.m32);
    }

    public static Vector3f inverse(Vector3f target)
    {
        return new Vector3f(-target.x, -target.y, -target.z);
    }

    public static Vector4f copy(Vector4f src)
    {
        return new Vector4f(src.x, src.y, src.z, src.w);
    }
}

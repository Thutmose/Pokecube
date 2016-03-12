package pokecube.modelloader.client.render.model;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class VectorMath
{
    static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    static final Vector3f Z_AXIS = new Vector3f(0.0F, 0.0F, 1.0F);

    public static void cleanSmall(Matrix4f matrix)
    {
        if (Math.abs(matrix.m00) < 1e-6) matrix.m00 = 0;
        if (Math.abs(matrix.m01) < 1e-6) matrix.m01 = 0;
        if (Math.abs(matrix.m02) < 1e-6) matrix.m02 = 0;
        if (Math.abs(matrix.m03) < 1e-6) matrix.m03 = 0;

        if (Math.abs(matrix.m10) < 1e-6) matrix.m10 = 0;
        if (Math.abs(matrix.m11) < 1e-6) matrix.m11 = 0;
        if (Math.abs(matrix.m12) < 1e-6) matrix.m12 = 0;
        if (Math.abs(matrix.m13) < 1e-6) matrix.m13 = 0;

        if (Math.abs(matrix.m20) < 1e-6) matrix.m20 = 0;
        if (Math.abs(matrix.m21) < 1e-6) matrix.m21 = 0;
        if (Math.abs(matrix.m22) < 1e-6) matrix.m22 = 0;
        if (Math.abs(matrix.m23) < 1e-6) matrix.m23 = 0;

        if (Math.abs(matrix.m30) < 1e-6) matrix.m30 = 0;
        if (Math.abs(matrix.m31) < 1e-6) matrix.m31 = 0;
        if (Math.abs(matrix.m32) < 1e-6) matrix.m32 = 0;
        if (Math.abs(matrix.m33) < 1e-6) matrix.m33 = 0;
    }

    public static Matrix4f fromFloat(float val)
    {
        return fromVector6f(val, val, val, val, val, val);
    }

    public static Matrix4f fromFloatArray(float[] vals)
    {
        return fromVector6f(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
    }

    public static Matrix4f fromVector6f(float xl, float yl, float zl, float xr, float yr, float zr)
    {
        Vector3f loc = new Vector3f(xl, yl, zl);
        Matrix4f ret = new Matrix4f();
        ret.translate(loc);
        ret.rotate(zr, Z_AXIS);
        ret.rotate(yr, Y_AXIS);
        ret.rotate(xr, X_AXIS);
        cleanSmall(ret);
        return ret;
    }
    
    public static Matrix4f fromVector6f(Vector6f vector)
    {
        Matrix4f ret = new Matrix4f();
        ret.translate(vector.vector1);
        ret.rotate(vector.vector2.z, Z_AXIS);
        ret.rotate(vector.vector2.y, Y_AXIS);
        ret.rotate(vector.vector2.x, X_AXIS);
        cleanSmall(ret);
        return ret;
    }
}

package pokecube.modelloader.client.render.smd;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;

public class Triangles
{
    ArrayList<Triangle> triangles;

    public static class Triangle
    {
        String              material;
        int[]               boneIds;
        Vector3f[]          positions;
        Vector3f[]          normals;
        TextureCoordinate[] uvss;
    }
}

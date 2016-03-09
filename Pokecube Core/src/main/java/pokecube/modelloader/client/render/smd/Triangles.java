package pokecube.modelloader.client.render.smd;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;
import pokecube.modelloader.client.render.smd.Skeleton.BoneVertex;

public class Triangles
{
    ArrayList<Triangle> triangles = new ArrayList<>();
    final SMDModel      model;

    public Triangles(SMDModel model)
    {
        this.model = model;
    }

    public void render()
    {
        GL11.glPushMatrix();
        GL11.glBegin(GL11.GL_TRIANGLES);
        boolean smooth = true;
        for (Triangle t : triangles)
        {
            if (t != null)
            {
                t.preRender();
                t.addForRender(smooth);
                t.postRender();
            }
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public static class Triangle
    {
        final String        material;
        final Triangles     triangles;
        int[]               parentBoneIds = new int[3];
        BoneVertex[]        vertices      = new BoneVertex[3];
        Vector3f            faceNormal;
        TextureCoordinate[] uvs           = new TextureCoordinate[3];
        int                 toAdd         = 0;

        public Triangle(String material, Triangles triangles)
        {
            this.triangles = triangles;
            this.material = material;
        }

        public void preRender()
        {

        }

        public void postRender()
        {

        }

        public void addVertex(String line)
        {
            line = line.replaceAll("\\s+", " ");
            String[] args = line.split(" ");
            BoneVertex vertex = new BoneVertex(Float.parseFloat(args[1]), Float.parseFloat(args[2]),
                    Float.parseFloat(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]),
                    Float.parseFloat(args[6]), triangles.model.getNextVertexID());
            int links = Integer.parseInt(args[9]);
            float[] weights = new float[links];
            float sum = 0.0F;
            for (int i = 0; i < links; i++)
            {
                weights[i] = Float.parseFloat(args[(i * 2 + 11)]);
                sum += weights[i];
            }
            for (int i = 0; i < links; i++)
            {
                int boneID = Integer.parseInt(args[(i * 2 + 10)]);
                float weight = weights[i] / sum;
                triangles.model.skeleton.getBone(boneID).vertices.put(vertex, weight);
            }
            uvs[toAdd] = new TextureCoordinate(Float.parseFloat(args[7]), Float.parseFloat(args[8]), 0);
            vertices[toAdd] = vertex;
            toAdd++;
        }

        public void addForRender(boolean smoothShading)
        {
            if ((!smoothShading) && (this.faceNormal == null))
            {
                calculateFaceNormal();
            }
            for (int i = 0; i < 3; i++)
            {
                GL11.glTexCoord2f(this.uvs[i].u, this.uvs[i].v);
                if (!smoothShading)
                {
                    GL11.glNormal3f(this.faceNormal.x, this.faceNormal.y, this.faceNormal.z);
                }
                else
                {
                    GL11.glNormal3f(vertices[i].xn, vertices[i].yn, vertices[i].zn);
                }
                GL11.glVertex3d(vertices[i].x, vertices[i].y, vertices[i].z);
            }
        }

        public void calculateFaceNormal()
        {
            Vector3f v1, v2, v3;
            v1 = new Vector3f(vertices[0].x, vertices[0].y, vertices[0].z);
            v2 = new Vector3f(vertices[1].x, vertices[1].y, vertices[1].z);
            v3 = new Vector3f(vertices[2].x, vertices[2].y, vertices[2].z);
            Vector3f a = new Vector3f(v2);
            a.sub(v1);
            Vector3f b = new Vector3f(v3);
            b.sub(v1);
            Vector3f c = new Vector3f();
            c.cross(a, b);
            c.normalize();
            faceNormal = c;
        }

        public String toString()
        {
            if (faceNormal == null) calculateFaceNormal();
            return Arrays.toString(vertices) + " " + faceNormal;
        }
    }
}

package pokecube.modelloader.client.render.smd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import pokecube.modelloader.client.render.model.Vertex;

public class Skeleton
{
    HashMap<Integer, Bone> boneMap = new HashMap<>();
    SkeletonAnimation      defaultPose;

    public void addBone(Bone bone)
    {
        if (boneMap.containsKey(bone.id)) throw new IllegalArgumentException("Already has bone of id " + bone.id);
        boneMap.put(bone.id, bone);
    }

    public Bone getBone(int id)
    {
        return boneMap.get(id);
    }

    public void reset()
    {
        for (Bone b : boneMap.values())
        {
            b.reset();
            for (BoneVertex v : b.vertices)
            {
                v.reset();
            }
        }
    }

    public void initChildren()
    {
        for (Bone bone : boneMap.values())
        {
            if (bone.parentId != -1)
            {
                Bone parent = boneMap.get(bone.parentId);
                bone.parent = parent;
                parent.children.add(bone);
            }
        }
    }

    public static class Bone
    {
        final int                 id;
        final int                 parentId;
        final String              name;
        final HashSet<Bone>       children = new HashSet<>();
        final HashSet<BoneVertex> vertices = new HashSet<>();
        public Matrix4f           deform   = new Matrix4f();
        Bone                      parent;

        public Bone(int id, int parentId, String name)
        {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        public Bone(String line)
        {
            String[] args = parse(line);
            id = Integer.parseInt(args[0]);
            name = args[1];
            parentId = Integer.parseInt(args[2]);
        }

        String[] parse(String line)
        {
            String[] ret = new String[3];

            int indexQuoteStart = line.indexOf("\"");
            int indexQuoteEnd = line.lastIndexOf("\"");

            ret[0] = line.substring(0, indexQuoteStart - 1);
            ret[1] = line.substring(indexQuoteStart + 1, indexQuoteEnd);
            ret[2] = line.substring(indexQuoteEnd + 2);
            System.out.println(Arrays.toString(ret));
            return ret;
        }

        public void reset()
        {

        }

        public String toString()
        {
            return id + " " + name + " " + parentId;
        }
    }

    public static class BoneVertex extends Vertex
    {
        private final Vector4f originalPos;
        public Vector4f        positionDeform = new Vector4f();
        private final Vector4f originalNormal;
        public Vector4f        normalDeform   = new Vector4f();
        public final int       id;
        public float           xn;
        public float           yn;
        public float           zn;

        public BoneVertex(float x, float y, float z, float xn, float yn, float zn, int id)
        {
            super(x, y, z);
            this.xn = xn;
            this.yn = yn;
            this.zn = zn;
            this.originalPos = new Vector4f(x, y, z, 1.0F);
            this.originalNormal = new Vector4f(xn, yn, zn, 0.0F);
            this.id = id;
        }

        public BoneVertex(BoneVertex vertex)
        {
            super(vertex.x, vertex.y, vertex.z);
            this.xn = vertex.xn;
            this.yn = vertex.yn;
            this.zn = vertex.zn;
            this.originalPos = new Vector4f(vertex.originalPos);
            this.originalNormal = new Vector4f(vertex.originalNormal);
            this.id = vertex.id;
            this.positionDeform = vertex.positionDeform;
            this.normalDeform = vertex.normalDeform;
        }

        public void reset()
        {
            this.positionDeform.set(originalPos);
            this.normalDeform.set(originalNormal);
        }

        public void applyModified(Bone bone, float weight)
        {
            Matrix4f modified = bone.deform;
            if (modified != null)
            {
                Vector4f locTemp = Matrix4f.transform(modified, this.originalPos, null);
                Vector4f normalTemp = Matrix4f.transform(modified, this.originalNormal, null);
                locTemp.scale(weight);
                normalTemp.scale(weight);
                Vector4f.add(locTemp, this.positionDeform, this.positionDeform);
                Vector4f.add(normalTemp, this.normalDeform, this.normalDeform);
            }
        }

        public void applyDeformation()
        {
            if (this.positionDeform == null)
            {
                this.x = this.originalPos.x;
                this.y = this.originalPos.y;
                this.z = this.originalPos.z;
            }
            else
            {
                this.x = this.positionDeform.x;
                this.y = this.positionDeform.y;
                this.z = this.positionDeform.z;
            }
            if (this.normalDeform == null)
            {
                this.xn = this.originalNormal.x;
                this.yn = this.originalNormal.y;
                this.zn = this.originalNormal.z;
            }
            else
            {
                this.xn = this.normalDeform.x;
                this.yn = this.normalDeform.y;
                this.zn = this.normalDeform.z;
            }
        }
    }
}

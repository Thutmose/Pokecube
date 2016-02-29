package pokecube.modelloader.client.render.smd;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Matrix4f;

import pokecube.modelloader.client.render.model.VectorMath;
import pokecube.modelloader.client.render.smd.Skeleton.Bone;

public class SkeletonAnimation
{
    public final Skeleton           skeleton;
    public int                      currentIndex = 0;
    public int                      lastIndex;
    public int                      frameCount;
    public String                   animationName;
    public ArrayList<SkeletonFrame> frames       = new ArrayList<>();

    public SkeletonAnimation(Skeleton skeleton)
    {
        this.skeleton = skeleton;
    }

    public void reset()
    {
        for (SkeletonFrame frame : frames)
            frame.reset();
    }

    public void reform()
    {
        for (int i = 0; i < this.frames.size(); i++)
        {
            SkeletonFrame frame = (SkeletonFrame) this.frames.get(i);
            frame.setAngles(skeleton.root.id, 0.0F);
            frame.reform();
        }
    }

    public void nextFrame()
    {
        if (this.currentIndex >= this.frames.size() - 1)
        {
            this.currentIndex = 0;
        }
        else
        {
            this.currentIndex++;
        }
    }

    public int getNumFrames()
    {
        return this.frames.size();
    }

    public void setCurrentFrame(int i)
    {
        if (this.lastIndex != i)
        {
            this.lastIndex = currentIndex;
            this.currentIndex = i;
        }
        if (currentIndex >= getNumFrames())
        {
            currentIndex = 0;
            lastIndex = getNumFrames() - 1;
        }
    }

    public void precalculateAnimation()
    {
        skeleton.reset();
        for (int i = 0; i < this.frames.size(); i++)
        {
            SkeletonFrame frame = this.frames.get(i);
            for (Integer j : skeleton.boneMap.keySet())
            {
                Bone bone = skeleton.boneMap.get(j);
                Matrix4f animated = frame.positions.get(j);
                bone.preloadAnimation(frame, animated);
            }
        }
    }

    static public class SkeletonFrame
    {
        public final SkeletonAnimation animation;
        final int                      time;
        /** Map of Bone Id -> Position + Rotation. */
        HashMap<Integer, Matrix4f>     positions                = new HashMap<>();
        HashMap<Integer, Matrix4f>     inversePositions         = new HashMap<>();
        HashMap<Integer, Matrix4f>     positionsOriginal        = new HashMap<>();
        HashMap<Integer, Matrix4f>     positionsOriginalInverse = new HashMap<>();

        public SkeletonFrame(int time, SkeletonAnimation animation)
        {
            this.time = time;
            this.animation = animation;
        }

        public void addFromLine(String line)
        {
            line = line.replaceAll("\\s+", " ");
            String[] args = line.split(" ");
            int id = Integer.parseInt(args[0]);
            Matrix4f matrix;
            matrix = VectorMath.fromVector6f(//
                    Float.parseFloat(args[1]), Float.parseFloat(args[3]), Float.parseFloat(args[2]),
                    Float.parseFloat(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6]));

            positions.put(id, matrix);
            inversePositions.put(id, Matrix4f.invert(matrix, null));
            positionsOriginal.put(id, new Matrix4f(matrix));
            positionsOriginalInverse.put(id, Matrix4f.invert(matrix, null));
        }

        public void reset()
        {
            for (Integer i : positions.keySet())
            {
                Matrix4f.load(positionsOriginal.get(i), positions.get(i));
                Matrix4f.load(positionsOriginalInverse.get(i), inversePositions.get(i));
            }
        }

        public String toString()
        {
            return time + "=" + positions;
        }

        public void setAngles(int id, float degrees)
        {
            float radians = (float) Math.toRadians(degrees);
            Matrix4f rotator = VectorMath.fromVector6f(0.0F, 0.0F, 0.0F, radians, 0.0F, 0.0F);
            Matrix4f.mul(rotator, this.positions.get(id), this.positions.get(id));
            Matrix4f.mul(Matrix4f.invert(rotator, null), this.inversePositions.get(id), this.inversePositions.get(id));
        }

        public void reform()
        {
            for (Integer i : positions.keySet())
            {
                Bone bone = this.animation.skeleton.getBone(i);
                if (bone.parent != null)
                {
                    Matrix4f temp = Matrix4f.mul((Matrix4f) this.positions.get(bone.parent.id),
                            (Matrix4f) this.positions.get(i), null);
                    this.positions.put(i, temp);
                    this.inversePositions.put(i, Matrix4f.invert(temp, null));
                }
            }
        }
    }
}

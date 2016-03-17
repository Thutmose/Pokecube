package pokecube.modelloader.client.render.smd;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Matrix4f;

import pokecube.modelloader.client.render.smd.Skeleton.Bone;
import thut.core.client.render.model.VectorMath;

public class SkeletonAnimation
{
    static public class SkeletonFrame
    {
        public final SkeletonAnimation animation;
        final int                      time;
        /** Map of Bone Id -> Position + Rotation. */
        HashMap<Integer, Matrix4f>     positions                = new HashMap<>();
        HashMap<Integer, Matrix4f>     inversePositions         = new HashMap<>();
        HashMap<Integer, Matrix4f>     diffs                    = new HashMap<>();
        HashMap<Integer, Matrix4f>     invDiffs                 = new HashMap<>();
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
                    Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]),
                    Float.parseFloat(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6]));
            positions.put(id, matrix);
            inversePositions.put(id, Matrix4f.invert(matrix, null));
            positionsOriginal.put(id, new Matrix4f(matrix));
            positionsOriginalInverse.put(id, Matrix4f.invert(matrix, null));
        }

        public void reform()
        {
            reformChildren(animation.skeleton.root);
        }

        private void reformChildren(Bone bone)
        {
            if (bone.parent != null)
            {
                Matrix4f temp = this.positions.get(bone.id);
                Matrix4f.mul(this.positions.get(bone.parent.id), temp, temp);
                Matrix4f.invert(temp, this.inversePositions.get(bone.id));
            }
            for (Bone b : bone.children)
            {
                reformChildren(b);
            }
        }

        public void reset()
        {
            for (Integer i : positions.keySet())
            {
                Matrix4f.load(positionsOriginal.get(i), positions.get(i));
                Matrix4f.load(positionsOriginalInverse.get(i), inversePositions.get(i));
            }
        }

        @Override
        public String toString()
        {
            return time + "=" + positions;
        }
    }
    public final Skeleton           skeleton;
    public int                      currentIndex   = 0;
    public int                      lastIndex;
    public int                      lastPoseChange = -1;
    public int                      frameCount;
    public String                   animationName;

    public ArrayList<SkeletonFrame> frames         = new ArrayList<>();

    public SkeletonAnimation(Skeleton skeleton)
    {
        this.skeleton = skeleton;
    }

    public SkeletonFrame getCurrentFrame()
    {
        return frames.get(currentIndex);
    }

    public int getNumFrames()
    {
        return this.frames.size();
    }

    public void nextFrame()
    {
        if (this.currentIndex >= this.frames.size() - 1)
        {
            // if (getNumFrames() > 1) currentIndex = 1;
            // else
            currentIndex = 0;
        }
        else
        {
            this.currentIndex++;
        }
    }

    public void precalculateAnimation()
    {
        if (lastPoseChange == currentIndex || frames.isEmpty()) return;
        SkeletonFrame prev = this.frames.get(0);
        for (int i = 0; i < this.frames.size(); i++)
        {
            SkeletonFrame frame = this.frames.get(i);
            for (Integer j : skeleton.boneMap.keySet())
            {
                if (frame == prev)
                {
                    frame.diffs.put(j, new Matrix4f());
                    frame.invDiffs.put(j, new Matrix4f());
                }
                else
                {
                    Matrix4f trans = frame.positions.get(j);
                    Matrix4f original = prev.positions.get(j);
                    Matrix4f temp = Matrix4f.invert(original, null);
                    Matrix4f diff = Matrix4f.mul(trans, temp, null);
                    frame.diffs.put(j, diff);
                    frame.invDiffs.put(j, Matrix4f.invert(temp, null));
                }
                Bone bone = skeleton.boneMap.get(j);
                Matrix4f transform = frame.positions.get(j);
                bone.preloadAnimation(frame, transform);
            }
            prev = frame;
        }
    }

    public void reform()
    {
        System.out.println("reform");
        for (int i = 0; i < this.frames.size(); i++)
        {
            SkeletonFrame frame = this.frames.get(i);
            frame.reform();
        }
    }

    public void reset()
    {
        for (SkeletonFrame frame : frames)
            frame.reset();
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
            // if (getNumFrames() > 1) currentIndex = 1;
            // else
            currentIndex = 0;
            lastIndex = getNumFrames() - 1;
        }
    }
}

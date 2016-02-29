package pokecube.modelloader.client.render.smd;

import java.util.Arrays;
import java.util.HashMap;

import pokecube.modelloader.client.render.smd.Triangles.Triangle;

public class SMDModel
{
    public Skeleton                           skeleton;
    public Triangles                          triangles;
    public HashMap<String, SkeletonAnimation> poses = new HashMap<>();

    private int vertexID = 0;

    public int getNextVertexID()
    {
        return vertexID++;
    }

    public void setAnimation(String animation)
    {
        if (poses.containsKey(animation))
        {
            skeleton.setPose(poses.get(animation));
        }
    }

    public void render()
    {
        animate();
        triangles.render();
    }

    public void checkTrianges()
    {
        for (Triangle triangle : triangles.triangles)
        {
            System.out.println(triangle.material + " " + Arrays.toString(triangle.vertices));
        }
    }

    public void animate()
    {
        resetVerts();

        if (skeleton.pose == null) return;

        skeleton.pose.precalculateAnimation();
        skeleton.pose.nextFrame();
        skeleton.applyPose();

        applyVertChange();
    }

    private void resetVerts()
    {
        if (skeleton == null) { return; }
        skeleton.reset();
    }

    private void applyVertChange()
    {
        skeleton.applyChange();
    }

}

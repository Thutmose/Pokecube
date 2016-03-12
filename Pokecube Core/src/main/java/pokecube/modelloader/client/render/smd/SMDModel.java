package pokecube.modelloader.client.render.smd;

import java.util.HashMap;

public class SMDModel
{
    public Skeleton                           skeleton;
    public Triangles                          triangles;
    public HashMap<String, SkeletonAnimation> poses = new HashMap<>();

    private int vertexID = 0;

    public void animate()
    {
        if (skeleton.pose == null) return;
//        skeleton.pose.nextFrame();
        skeleton.applyPose();
    }

    public int getNextVertexID()
    {
        return vertexID++;
    }

    public void render()
    {
        animate();
        triangles.render();
    }

    public void setAnimation(String animation)
    {
        if (poses.containsKey(animation))
        {
            skeleton.setPose(poses.get(animation));
        }
    }

}

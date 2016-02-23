package pokecube.modelloader.client.render.smd;

import java.util.ArrayList;
import java.util.HashMap;

import pokecube.modelloader.client.render.model.Vector6f;

public class SkeletonAnimation
{
    ArrayList<SkeletonFrame> frames;

    static public class SkeletonFrame
    {
        int                        time;
        /** Map of Bone Id -> Position + Rotation. */
        HashMap<Integer, Vector6f> positions;
    }
}

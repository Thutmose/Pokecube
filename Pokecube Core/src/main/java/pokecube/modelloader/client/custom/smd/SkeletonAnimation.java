package pokecube.modelloader.client.custom.smd;

import java.util.ArrayList;
import java.util.HashMap;

import pokecube.modelloader.client.custom.model.Vector6f;

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

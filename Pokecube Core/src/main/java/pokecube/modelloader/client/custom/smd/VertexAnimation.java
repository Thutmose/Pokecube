package pokecube.modelloader.client.custom.smd;

import java.util.ArrayList;
import java.util.HashMap;

import pokecube.modelloader.client.custom.model.Vector6f;

public class VertexAnimation
{
    ArrayList<AnimationFrame> frames;

    static public class AnimationFrame
    {
        int                        time;
        /** Map of Bone Id -> Position + normal. */
        HashMap<Integer, Vector6f> positions;
    }
}

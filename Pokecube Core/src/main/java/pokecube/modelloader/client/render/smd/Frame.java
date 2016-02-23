package pokecube.modelloader.client.render.smd;

import java.util.HashMap;

import pokecube.modelloader.client.render.model.Vector6f;

public class Frame
{
    int                        time;
    /** Map of Bone Id -> transforms. */
    HashMap<Integer, Vector6f> positions;
}

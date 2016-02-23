package pokecube.modelloader.client.custom.smd;

import java.util.HashMap;

import pokecube.modelloader.client.custom.model.Vector6f;

public class Frame
{
    int                        time;
    /** Map of Bone Id -> transforms. */
    HashMap<Integer, Vector6f> positions;
}

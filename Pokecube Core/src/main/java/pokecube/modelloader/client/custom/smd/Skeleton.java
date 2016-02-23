package pokecube.modelloader.client.custom.smd;

import java.util.HashMap;

public class Skeleton
{
    HashMap<Integer, Bone> boneMap;

    public class Bone
    {
        final int    id;
        final int    parentId;
        final String name;

        public Bone(int id, int parentId, String name)
        {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }
    }

}

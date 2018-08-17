package pokecube.core.contributors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

public class Contributors
{
    public List<Contributor>         contributors = Lists.newArrayList();

    private Map<UUID, Contributor>   byUUID       = Maps.newHashMap();
    private Map<String, Contributor> byName       = Maps.newHashMap();

    public void init()
    {
        byUUID.clear();
        byName.clear();
        for (Contributor c : contributors)
        {
            if (c.uuid != null) byUUID.put(c.uuid, c);
            byName.put(c.name, c);
        }
        // TODO merge duplicates here somehow?
    }

    public Contributor getContributor(GameProfile profile)
    {
        if (byName.containsKey(profile.getName())) return byName.get(profile.getName());
        return byUUID.get(profile.getId());
    }
}

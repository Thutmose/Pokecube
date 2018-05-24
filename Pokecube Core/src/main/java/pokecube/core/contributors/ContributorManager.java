package pokecube.core.contributors;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;

public class ContributorManager
{
    private static final ExclusionStrategy EXCLUDEMAPS;
    static
    {
        EXCLUDEMAPS = new ExclusionStrategy()
        {

            @Override
            public boolean shouldSkipField(FieldAttributes f)
            {
                return f.getName().equals("byUUID") || f.getName().equals("byName") || f.getName().equals("cubeStack");
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz)
            {
                return false;
            }
        };
    }

    private static final Gson               GSON     = new GsonBuilder()
            .addDeserializationExclusionStrategy(EXCLUDEMAPS).create();

    private static final String             DEFAULTS = PokecubeMod.GIST + "contribs.json";

    private static final ContributorManager INSTANCE = new ContributorManager();

    public static ContributorManager instance()
    {
        return INSTANCE;
    }

    public Contributors contributors = new Contributors();

    public Contributor getContributor(GameProfile profile)
    {
        return contributors.getContributor(profile);
    }

    public List<ContributorType> getContributionTypes(GameProfile profile)
    {
        Contributor contrib;
        if ((contrib = contributors.getContributor(profile)) != null) return contrib.types;
        return Lists.newArrayList();
    }

    public void loadContributors()
    {
        contributors.contributors.clear();
        if (PokecubeCore.core.getConfig().default_contributors) loadContributors(DEFAULTS);
        if (!PokecubeCore.core.getConfig().extra_contributors.isEmpty())
            loadContributors(PokecubeCore.core.getConfig().extra_contributors);
        contributors.init();
    }

    private void loadContributors(String location)
    {
        URL url;
        URLConnection con;
        try
        {
            url = new URL(location);
            con = url.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            InputStream in = con.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            Contributors newContribs = GSON.fromJson(reader, Contributors.class);
            if (PokecubeMod.debug) PokecubeMod.log(newContribs + "");
            if (newContribs != null)
            {
                contributors.contributors.addAll(newContribs.contributors);
            }
        }
        catch (Exception e)
        {
            if (e instanceof UnknownHostException)
            {
                PokecubeMod.log(Level.WARNING, "Error loading contributors, unknown host " + location);
            }
            else PokecubeMod.log(Level.WARNING, "Error loading Contributors from " + location, e);
        }
    }
}

package pokecube.core.database;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.core.database.PokedexEntryLoader.BodyPart;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class PokemobBodies
{
    static Map<PokedexEntry, PokemobBody> bodyMap = Maps.newHashMap();

    public static void initBody(IPokemob pokemob)
    {
        PokemobBody body;
        if ((body = bodyMap.get(pokemob.getPokedexEntry())) != null)
        {
            pokemob.setSubParts(body.getParts(pokemob));
        }
    }

    public static class PokemobBody
    {
        List<PokemobPart> parts = Lists.newArrayList();

        public EntityPokemobPart[] getParts(IPokemob mob)
        {
            if (parts.isEmpty()) return null;
            EntityPokemobPart[] ret = new EntityPokemobPart[parts.size()];
            for (int i = 0; i < parts.size(); i++)
            {
                PokemobPart part = parts.get(i);
                ret[i] = new EntityPokemobPart(mob, part.name, part.offset, part.dimensions);
            }
            return ret;
        }
    }

    public static class PokemobPart
    {
        Vector3f   offset;
        Vector3f[] dimensions = new Vector3f[2];
        String     name;

        public PokemobPart(BodyPart node)
        {
            String[] args = node.offset.split(",");
            name = node.name;
            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);
            float z = Float.parseFloat(args[2]);
            offset = new Vector3f(x, y, z);
            String[] dims = node.dimensions.split("->");
            for (int i = 0; i < 2; i++)
            {
                args = dims[i].split(",");
                x = Float.parseFloat(args[0]);
                y = Float.parseFloat(args[1]);
                z = Float.parseFloat(args[2]);
                dimensions[i] = new Vector3f(x, y, z);
            }
        }
    }

    public static void initBodies()
    {
        for (XMLPokedexEntry xmlEntry : PokedexEntryLoader.database.pokemon)
        {
            PokedexEntry entry = Database.getEntry(xmlEntry.name);
            if (xmlEntry.body != null && !xmlEntry.body.parts.isEmpty() && entry != null)
            {
                PokemobBody body = new PokemobBody();
                for (BodyPart part : xmlEntry.body.parts)
                {
                    body.parts.add(new PokemobPart(part));
                }
                bodyMap.put(entry, body);
                if (PokecubeMod.debug) PokecubeMod.log(entry + " " + body);
            }
        }
    }
}

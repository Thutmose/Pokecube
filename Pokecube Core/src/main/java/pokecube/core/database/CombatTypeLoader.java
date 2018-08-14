package pokecube.core.database;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraftforge.common.util.EnumHelper;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class CombatTypeLoader
{
    private static Gson gson = new Gson();

    public static class TypeEffect
    {
        public String type;
        public float  amount;
    }

    public static class JsonType
    {
        public String       name;
        public int          colour;
        public TypeEffect[] outgoing;
        Map<String, Float>  effects = Maps.newHashMap();

        void init()
        {
            for (TypeEffect e : outgoing)
            {
                effects.put(e.type, e.amount);
            }
        }

        float effect(String type)
        {
            if (!effects.containsKey(type)) return 1;
            return effects.get(type);
        }
    }

    public static class CombatTypes
    {
        public JsonType[] types;

        public void init()
        {
            PokeType.typeTable = new float[types.length][types.length];

            // First add them all in as enums.
            for (int i = 0; i < types.length; i++)
            {
                JsonType type = types[i];
                type.init();
                if (PokeType.getType(type.name) == PokeType.unknown && !type.name.equals("???"))
                {
                    EnumHelper.addEnum(PokeType.class, type.name, new Class<?>[] { int.class, String.class },
                            type.colour, type.name);
                }
            }

            for (int i = 0; i < types.length; i++)
            {
                float[] arr = new float[types.length];
                PokeType.typeTable[i] = arr;
                JsonType current = types[i];
                for (int j = 0; j < types.length; j++)
                {
                    arr[j] = current.effect(types[j].name);
                }
            }
        }
    }

    public static void loadTypes()
    {
        Database.copyDatabaseFile("types.json");
        File file = new File(Database.CONFIGLOC + "types.json");
        try
        {
            FileReader reader = new FileReader(file);
            CombatTypes types = gson.fromJson(reader, CombatTypes.class);
            types.init();
            reader.close();
        }
        catch (JsonSyntaxException | JsonIOException | IOException e)
        {
            PokecubeMod.log(Level.SEVERE, "Error loading types.json", e);
        }
    }
}

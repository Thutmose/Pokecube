package pokecube.core.handlers.playerdata.advancements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.advancements.AdvancementRewards;
import pokecube.core.database.PokedexEntry;

public class AdvancementGenerator
{
    static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    public static AdvancementRewards makeReward(PokedexEntry entry)
    {
        return null;
    }

    public static JsonObject fromInfo(PokedexEntry entry, String id)
    {
        JsonObject displayJson = new JsonObject();
        JsonObject icon = new JsonObject();
        icon.addProperty("item", "pokecube:pokecube");
        JsonObject title = new JsonObject();
        title.addProperty("translate", "achievement.pokecube." + id);
        JsonArray item = new JsonArray();
        JsonObject pokemobName = new JsonObject();
        pokemobName.addProperty("translate", entry.getUnlocalizedName());
        item.add(pokemobName);
        title.add("with", item);
        JsonObject description = new JsonObject();
        description.addProperty("translate", "achievement.pokecube." + id + ".desc");
        description.add("with", item);
        displayJson.add("icon", icon);
        displayJson.add("title", title);
        displayJson.add("description", description);
        // if (entry.legendary) displayJson.addProperty("frame", "challenge");
        return displayJson;
    }

    public static String[][] makeRequirements(PokedexEntry entry)
    {
        return new String[][] { { entry.getName() } };
    }

    public static JsonObject fromCriteria(PokedexEntry entry, String id)
    {
        JsonObject critmap = new JsonObject();
        JsonObject sub = new JsonObject();
        sub.addProperty("trigger", "pokecube:" + id);
        JsonObject conditions = new JsonObject();
        if (id.equals("catch") || id.equals("kill")) conditions.addProperty("lenient", true);
        conditions.addProperty("entry", entry.getName());
        sub.add("conditions", conditions);
        critmap.add(id + "_" + entry.getName(), sub);
        return critmap;
    }

    public static String makeJson(PokedexEntry entry, String id, String parent)
    {
        JsonObject json = new JsonObject();
        json.add("display", fromInfo(entry, id));
        json.add("criteria", fromCriteria(entry, id));
        if (parent != null)
        {
            json.addProperty("parent", parent);
        }
        return GSON.toJson(json);
    }
}

package pokecube.modelloader.client.render.tabula.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author iLexiconn
 * @since 0.1.0
 */
public class JsonFactory {
    private static Gson gson = new Gson();
    private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    public static Gson getPrettyGson() {
        return prettyGson;
    }
}

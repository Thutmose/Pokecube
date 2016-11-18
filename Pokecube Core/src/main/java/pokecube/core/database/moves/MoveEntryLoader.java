package pokecube.core.database.moves;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.core.interfaces.IMoveConstants;

public class MoveEntryLoader implements IMoveConstants
{

    public static class MoveJsonEntry
    {
        int     num;
        String  name;
        String  type;
        String  category;
        int     pp;
        String  pwr;
        String  acc;
        String  status;
        String  stats;
        String  changes;
        String  heal;
        String  multi;
        boolean protect;
        boolean magiccoat;
        boolean snatch;
        boolean kingsrock;
        int     crit;
        String  selfDamage;
        String  defaultanimation;
    }

    public static class MovesJson
    {
        List<MoveJsonEntry> moves = Lists.newArrayList();
    }

    public static void loadMoves(String path)
    {
        File file = new File(path);
        try
        {
            MovesParser.load(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

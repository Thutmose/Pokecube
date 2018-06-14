package pokecube.core.database.moves.json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pokecube.core.database.moves.MovesParser;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.implementations.MovesAdder;

public class JsonMoves
{
    private static Gson                       gson       = new Gson();
    private static Gson                       prettyGson = new GsonBuilder().setPrettyPrinting().create();
    private static MovesJson                  moves;
    private static Map<String, MoveJsonEntry> movesMap   = new HashMap<>();

    public static class AnimationsJson
    {
        public String              name;
        public String              defaultanimation;
        public List<AnimationJson> animations = new ArrayList<>();
    }

    public static class AnimsJson
    {
        public List<AnimationsJson> moves = new ArrayList<>();
    }

    public static class AnimationJson
    {
        public String  preset;
        public String  duration  = "5";
        public String  starttick = "0";
        public String  sound;
        public Boolean soundSource;
        public Boolean soundTarget;
        public Float   volume;
        public Float   pitch;
        public boolean applyAfter;

        @Override
        public String toString()
        {
            return "preset: " + preset + " duration:" + duration + " starttick:" + starttick + " applyAfter:"
                    + applyAfter;
        }
    }

    public static class MoveJsonEntry
    {
        public String              name;
        public String              readableName;
        public String              type;
        public String              category;

        public String              pp;
        public String              pwr;
        public String              acc;

        public String              battleEffect;
        public String              secondaryEffect;
        public String              inDepthEffect;
        public String              detailedEffect;

        public String              effectRate;

        public String              zMovesTo;
        public String              zMovePower;
        public String              zEffect;

        public String              tmNum;
        public String              speedPriority;
        public String              target;

        public String              contact;
        public String              soundType;
        public String              punchType;
        public String              snatchable;
        public String              zMove;

        public String              defrosts;
        public String              wideArea;
        public String              magiccoat;
        public String              protect;
        public String              mirrormove;

        public String              zVersion;

        public String              defaultanimation;
        public String              soundEffectSource;
        public String              soundEffectTarget;

        public boolean             multiTarget     = false;
        public boolean             interceptable   = true;
        public String              preset;
        public boolean             ohko            = false;
        public boolean             protectionMoves = false;
        public int                 extraInfo       = -1;
        public List<AnimationJson> animations;
    }

    public static class MovesJson
    {
        public List<MoveJsonEntry> moves = new ArrayList<>();

        public MoveJsonEntry getEntry(String name, boolean create)
        {
            name = convertMoveName(name);
            MoveJsonEntry ret = movesMap.get(name);
            if (create && ret == null)
            {
                ret = new MoveJsonEntry();
                ret.name = name;
                moves.add(ret);
                init();
            }
            return ret;
        }

        public void init()
        {
            movesMap.clear();
            for (MoveJsonEntry e : moves)
            {
                e.name = convertMoveName(e.name);
                movesMap.put(e.name, e);
            }
            moves.sort(new Comparator<MoveJsonEntry>()
            {
                @Override
                public int compare(MoveJsonEntry o1, MoveJsonEntry o2)
                {
                    return o1.name.compareTo(o2.name);
                }
            });
        }
    }

    public static interface IValueFixer
    {
        String fix(String input);
    }

    public static class FieldApplicator
    {
        Field       field;
        IValueFixer fixer = new IValueFixer()
                          {
                              @Override
                              public String fix(String input)
                              {
                                  return input;
                              }
                          };

        public FieldApplicator(Field field, IValueFixer ValueFixer)
        {
            this.field = field;
            this.fixer = ValueFixer;
        }

        public FieldApplicator(Field field)
        {
            this.field = field;
        }

        public void apply(String value, Object obj)
        {
            try
            {
                field.set(obj, fixer.fix(value));
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void loadMoves(File file)
    {
        try
        {
            FileReader reader = new FileReader(file);
            moves = gson.fromJson(reader, MovesJson.class);
            moves.init();
            reader.close();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with read " + file, e);
            moves = new MovesJson();
            try
            {
                write(file);
            }
            catch (Exception e1)
            {
                PokecubeMod.log(Level.WARNING, "Error with write " + file, e);
            }
        }
    }

    public static void merge(File animationFile, File movesFile)
    {
        loadMoves(movesFile);
        try
        {
            FileReader reader = new FileReader(animationFile);
            AnimsJson animations = gson.fromJson(reader, AnimsJson.class);
            reader.close();
            List<AnimationsJson> movesList = new ArrayList<>(animations.moves);
            for (AnimationsJson anim : movesList)
            {
                if (anim.defaultanimation == null && anim.animations.isEmpty()) animations.moves.remove(anim);
                else if (anim.defaultanimation != null)
                {
                    AnimationJson animation = new AnimationJson();
                    animation.preset = anim.defaultanimation;
                    anim.defaultanimation = null;
                    anim.animations.add(animation);
                }
            }

            animations.moves.sort(new Comparator<AnimationsJson>()
            {
                @Override
                public int compare(AnimationsJson arg0, AnimationsJson arg1)
                {
                    return arg0.name.compareTo(arg1.name);
                }
            });
            Map<String, MoveJsonEntry> entryMap = Maps.newHashMap();
            for (MoveJsonEntry entry : moves.moves)
            {
                entryMap.put(entry.name, entry);
                for (AnimationsJson anims : animations.moves)
                {
                    if (convertMoveName(anims.name).equals(convertMoveName(entry.name)))
                    {
                        entry.defaultanimation = anims.defaultanimation;
                        entry.animations = anims.animations;
                        anims.name = entry.name;
                        break;
                    }
                }
            }
            MovesParser.load(moves);
            for (Move_Base move : MovesUtils.moves.values())
            {
                MoveJsonEntry entry = entryMap.get(move.name);
                if (entry != null) move.move.baseEntry = entry;
                else if (!move.name.startsWith("pokemob.status"))
                    PokecubeMod.log(Level.SEVERE, "No Entry for " + move.name);
            }
            if (PokecubeMod.debug) PokecubeMod.log("Processed " + MovesUtils.moves.size() + " Moves.");
            MovesAdder.postInitMoves();
            write(movesFile);
            String output = prettyGson.toJson(animations);
            FileWriter writer = new FileWriter(animationFile);
            writer.append(output);
            writer.close();

            MovesJson cleaned = new MovesJson();
            for (MoveJsonEntry entry : moves.moves)
            {
                MoveJsonEntry newEntry = new MoveJsonEntry();
                for (Field f : MoveJsonEntry.class.getFields())
                {
                    if (!f.getName().equals("animations"))
                    {
                        try
                        {
                            f.set(newEntry, f.get(entry));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                cleaned.moves.add(newEntry);
            }

            output = prettyGson.toJson(cleaned);
            writer = new FileWriter(movesFile);
            writer.append(output);
            writer.close();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error with animation parsing.", e);
        }
    }

    public static MovesJson getMoves(File file)
    {
        if (moves == null) loadMoves(file);
        return moves;
    }

    public static void write(File file) throws IOException
    {
        String output = prettyGson.toJson(moves);
        FileWriter writer = new FileWriter(file);
        writer.append(output);
        writer.close();
    }

    public static String convertMoveName(String old)
    {
        String ret = "";
        String name = old.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "");
        String[] args = name.split(" ");
        for (int i = 0; i < args.length; i++)
        {
            ret += args[i];
        }
        return ret;
    }
}

package pokecube.adventures.utils;

import static pokecube.adventures.entity.trainers.TypeTrainer.addTrainerSpawn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.utils.PokeType;

public class DBLoader
{
    private final static String DBLOCATION = "/assets/pokecube_adventures/database/";

    private static PrintWriter  out;

    private static FileWriter   fwriter;

    static String               header     = "Trainer Type,Trainer Pokemon,Biomes,Occurance,Trainer Gender (Male/Female/Both)";

    static String               example    = "Red,omanyte pidgey lapras nidoranm venonat zapdos,all,1,Male";

    static String               female     = "female:,Alice,Bridget,Carrie,Connie,Dana,Ellen,Krise,Laura,Linda,Michelle,Shannon,Gina,Brooke,Cindy,Debra,Edna,Erin,Heidi,Hope,Liz,Sharon,Tanya,Tiffany,Beth,Carol,Emma,Fran,Cara,Jenn,Kate,Cybil,Gwen,Irene,Kelly,Joyce,Lola,Megan,Quinn,Reena,Valerie";

    static String               male       = "male:,Anthony,Bailey,Benjamin,Daniel,Erik,Jim,Kenny,Leonard,Michael,Parry,Philip,Russell,Sidney,Tim,Timothy,Wade,Al,Arnie,Benny,Don,Doug,Ed,Josh,Ken,Rob,Joey,Mikey,Albert,Gordon,Ian,Jason,Jimmy,Owen,Samuel,Warren,Aaron,Allen,Blake,Brian,Abe";

    private static ArrayList<ArrayList<String>> getRows(String file)
    {
        InputStream res = (net.minecraft.util.StringTranslate.class).getResourceAsStream(file);
        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try
        {

            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {

                String[] row = line.split(cvsSplitBy);
                rows.add(new ArrayList<String>());
                for (int i = 0; i < row.length; i++)
                {
                    rows.get(n).add(row[i]);
                }
                n++;
            }

        }
        catch (FileNotFoundException e)
        {
        }
        catch (NullPointerException e)
        {

            try
            {
                FileReader temp = new FileReader(new File(file));
                br = new BufferedReader(temp);
                int n = 0;
                while ((line = br.readLine()) != null)
                {

                    String[] row = line.split(cvsSplitBy);
                    rows.add(new ArrayList<String>());
                    for (int i = 0; i < row.length; i++)
                    {
                        rows.get(n).add(row[i]);
                    }
                    n++;
                }
                temp.close();
            }
            catch (FileNotFoundException e1)
            {
                System.err.println("Missing a Database file " + file);
                writeDefaultConfig(file);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }

            // e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }

    public static void load()
    {
        loadInfo(DBLOCATION + "types.csv");
        loadInfo(PokecubeAdv.CUSTOMTRAINERFILE);
        loadNames();
        TypeTrainer.postInitTrainers();
    }

    public static void loadInfo(String s2)
    {
        ArrayList<ArrayList<String>> rows = getRows(s2);

        rows:
        for (ArrayList<String> row : rows)
        {
            if (row.isEmpty()) continue;

            String name = row.get(0);

            if (name.equals("Trainer Type") || name.equals("TrainerName") || name.trim().isEmpty()) continue;

            String pokemon = row.get(1);
            String b = row.get(2);

            int weight = 0;
            try
            {
                weight = Integer.parseInt(row.get(3));
            }
            catch (NumberFormatException e1)
            {
            }

            String[] pokeList = pokemon.split(" ");
            String[] biomeList = b.split(" ");
            String gender = row.get(4).trim();
            TypeTrainer type = TypeTrainer.typeMap.get(name);
            if (type == null) type = new TypeTrainer(name);

            byte male = 1;
            byte female = 2;

            if (row.size() > 5)
            {
                type.drops = row.get(5);
            }

            type.genders = (byte) (gender.equalsIgnoreCase("male") ? male
                    : gender.equalsIgnoreCase("female") ? female : male + female);

            if (!pokeList[0].contains("-"))
            {
                for (String s : pokeList)
                {
                    PokedexEntry e = Database.getEntry(s);
                    if (s != null && !type.pokemon.contains(e) && e != null)
                    {
                        type.pokemon.add(e);
                    }
                    else if (e == null)
                    {
                        // System.err.println("Error in reading of "+s);
                    }
                }
            }
            else
            {
                String[] types = pokeList[0].replace("-", "").split(":");
                if (types[0].equalsIgnoreCase("all"))
                {
                    for (PokedexEntry s : Database.spawnables)
                    {
                        if (!s.getSpawnData().types[SpawnData.LEGENDARY] && s.getPokedexNb() != 151 && s != null)
                        {
                            type.pokemon.add(s);
                        }
                    }
                }
                else
                {
                    for (int i = 0; i < types.length; i++)
                    {
                        PokeType pokeType = PokeType.getType(types[i]);
                        if (pokeType != PokeType.unknown)
                        {
                            for (PokedexEntry s : Database.spawnables)
                            {
                                if (s.isType(pokeType) && !s.getSpawnData().types[SpawnData.LEGENDARY]
                                        && s.getPokedexNb() != 151 && s != null)
                                {
                                    type.pokemon.add(s);
                                }
                            }
                        }
                    }
                }

            }
            for (String s : biomeList)
            {
                if (s.equalsIgnoreCase("none")) continue rows;
            }
            for (String s : biomeList)
            {
                for (int i = 0; i < weight; i++)
                    addTrainerSpawn(s, type);
            }

        }

    }

    public static void loadNames()
    {
        String s = PokecubeAdv.CUSTOMTRAINERFILE.replace("trainers.csv", "names.csv");

        File file = new File(s);
        if (!file.exists())
        {
            try
            {
                writeDefaultNames(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        ArrayList<ArrayList<String>> rows = getRows(s);

        for (ArrayList<String> row : rows)
        {
            if (row.isEmpty()) continue;
            String name = row.get(0);
            if (name.equalsIgnoreCase("female:"))
            {
                for (int i = 1; i < row.size(); i++)
                {
                    TypeTrainer.femaleNames.add(row.get(i));
                }
                continue;
            }
            if (name.equalsIgnoreCase("male:"))
            {
                for (int i = 1; i < row.size(); i++)
                {
                    TypeTrainer.maleNames.add(row.get(i));
                }
                continue;
            }
        }
    }

    private static void writeDefaultConfig(String file)
    {
        try
        {
            File temp = new File(file.replace("trainers.csv", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            fwriter = new FileWriter(file);
            out = new PrintWriter(fwriter);
            out.println(header);
            out.println(example);
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeDefaultNames(File file) throws IOException
    {
        fwriter = new FileWriter(file);
        out = new PrintWriter(fwriter);
        out.println(female);
        out.println(male);
        out.close();
        fwriter.close();
    }

}

package pokecube.core.database.abilities;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Maps;

import pokecube.core.interfaces.IPokemob;

@SuppressWarnings("unchecked")
public class AbilityManager
{
    public static class ClassFinder
    {

        private static final char   DOT               = '.';

        private static final char   SLASH             = '/';

        private static final String CLASS_SUFFIX      = ".class";

        private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        public static List<Class<?>> find(String scannedPackage) throws UnsupportedEncodingException
        {
            String scannedPath = scannedPackage.replace(DOT, SLASH);
            URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) { throw new IllegalArgumentException(
                    String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage)); }
            File scannedDir = new File(
                    java.net.URLDecoder.decode(scannedUrl.getFile(), Charset.defaultCharset().name()));

            List<Class<?>> classes = new ArrayList<Class<?>>();
            if (scannedDir.exists()) for (File file : scannedDir.listFiles())
            {
                classes.addAll(findInFolder(file, scannedPackage));
            }
            else if (scannedDir.toString().contains("file:") && scannedDir.toString().contains(".jar"))
            {
                String name = scannedDir.toString();
                String pack = name.split("!")[1].replace(File.separatorChar, SLASH).substring(1) + SLASH;
                name = name.replace("file:", "");
                name = name.replaceAll("(.jar)(.*)", ".jar");
                scannedDir = new File(name);
                try
                {
                    ZipFile zip = new ZipFile(scannedDir);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    int n = 0;
                    while (entries.hasMoreElements() && n < 10)
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(pack) && s.endsWith(CLASS_SUFFIX))
                        {
                            try
                            {
                                classes.add(Class.forName(s.replace(CLASS_SUFFIX, "").replace(SLASH, DOT)));
                            }
                            catch (ClassNotFoundException ignore)
                            {
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return classes;
        }

        private static List<Class<?>> findInFolder(File file, String scannedPackage)
        {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            String resource = scannedPackage + DOT + file.getName();
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    classes.addAll(findInFolder(child, resource));
                }
            }
            else if (resource.endsWith(CLASS_SUFFIX))
            {
                int endIndex = resource.length() - CLASS_SUFFIX.length();
                String className = resource.substring(0, endIndex);
                try
                {
                    classes.add(Class.forName(className));
                }
                catch (ClassNotFoundException ignore)
                {
                }
            }
            return classes;
        }

    }
    private static HashMap<String, Class<? extends Ability>>  nameMap  = Maps.newHashMap();
    private static HashMap<Class<? extends Ability>, String>  nameMap2 = Maps.newHashMap();
    private static HashMap<Class<? extends Ability>, Integer> idMap    = Maps.newHashMap();
    private static HashMap<Integer, Class<? extends Ability>> idMap2   = Maps.newHashMap();

    static int                                                nextID   = 0;

    static
    {
        List<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(AbilityManager.class.getPackage().getName());
            for (Class<?> candidateClass : foundClasses)
            {
                if (Ability.class.isAssignableFrom(candidateClass) && candidateClass != Ability.class)
                {
                    addAbility((Class<? extends Ability>) candidateClass);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean abilityExists(String name)
    {
        if (name == null) return false;
        name = name.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", "");
        return nameMap.containsKey(name);
    }

    public static void addAbility(Class<? extends Ability> ability)
    {
        addAbility(ability, ability.getSimpleName());
    }

    public static void addAbility(Class<? extends Ability> ability, String name)
    {
        name = name.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", "");
        nameMap.put(name, ability);
        nameMap2.put(ability, name);
        idMap.put(ability, nextID);
        idMap2.put(nextID, ability);
        nextID++;
    }

    public static Ability getAbility(Integer id, Object... args)
    {
        return makeAbility(id, args);
    }

    public static Ability getAbility(String name, Object... args)
    {
        if (name == null) return null;
        return makeAbility(name.toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", ""), args);
    }

    public static int getIdForAbility(Ability ability)
    {
        return idMap.get(ability.getClass());
    }

    public static String getNameForAbility(Ability ability)
    {
        return nameMap2.get(ability.getClass());
    }

    public static boolean hasAbility(String abilityName, IPokemob pokemob)
    {
        Ability ability = pokemob.getAbility();
        if (ability == null) { return false; }
        return ability.toString()
                .equalsIgnoreCase(abilityName.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", ""));
    }

    public static Ability makeAbility(Object val, Object... args)
    {
        Class<? extends Ability> abil = null;
        if (val instanceof String) abil = nameMap.get(val);
        else if (val instanceof Class) abil = (Class<? extends Ability>) val;
        else abil = idMap2.get(val);
        if (abil == null) return null;
        Ability ret = null;
        try
        {
            ret = abil.newInstance().init(args);
            ret.init(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }
}

package pokecube.core.handlers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public abstract class ConfigBase extends Configuration
{
    private final ConfigBase defaults;

    public ConfigBase(ConfigBase defaults)
    {
        this.defaults = defaults;
    }

    public ConfigBase(File path, ConfigBase defaults)
    {
        super(path);
        this.defaults = defaults;
    }

    protected void populateSettings()
    {
        populateSettings(false);
    }

    protected void populateSettings(boolean writing)
    {
        Class<?> me = getClass();
        Set<Property> fields = new HashSet<>();
        Configure c;
        for (Field f : me.getDeclaredFields())
        {
            c = (Configure) f.getAnnotation(Configure.class);
            if (c != null)
            {
                try
                {
                    Property p = null;
                    f.setAccessible(true);
                    if ((f.getType() == Long.TYPE) || (f.getType() == Long.class))
                    {
                        long defaultValue = f.getLong(defaults);
                        p = get(c.category(), f.getName(), (int) defaultValue);
                        if (writing)
                        {
                            defaultValue = f.getLong(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            long value = p.getInt();
                            f.set(this, value);
                        }
                    }
                    else if (f.getType() == String.class)
                    {
                        String defaultValue = (String) f.get(defaults);
                        p = get(c.category(), f.getName(), defaultValue);
                        if (writing)
                        {
                            defaultValue = (String) f.get(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            String value = p.getString();
                            f.set(this, value);
                        }
                    }
                    else if ((f.getType() == Integer.TYPE) || (f.getType() == Integer.class))
                    {
                        int defaultValue = f.getInt(defaults);
                        p = get(c.category(), f.getName(), defaultValue);
                        if (writing)
                        {
                            defaultValue = f.getInt(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            int value = p.getInt();
                            f.set(this, Integer.valueOf(value));
                        }
                    }
                    else if ((f.getType() == Float.TYPE) || (f.getType() == Float.class))
                    {
                        float defaultValue = f.getFloat(defaults);
                        p = get(c.category(), f.getName(), defaultValue);
                        if (writing)
                        {
                            defaultValue = f.getFloat(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            float value = (float) p.getDouble();
                            f.set(this, Float.valueOf(value));
                        }
                    }
                    else if ((f.getType() == Double.TYPE) || (f.getType() == Double.class))
                    {
                        double defaultValue = f.getDouble(defaults);
                        p = get(c.category(), f.getName(), defaultValue);
                        if (writing)
                        {
                            defaultValue = f.getDouble(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            double value = p.getDouble();
                            f.set(this, Double.valueOf(value));
                        }
                    }
                    else if ((f.getType() == Boolean.TYPE) || (f.getType() == Boolean.class))
                    {
                        boolean defaultValue = f.getBoolean(defaults);
                        p = get(c.category(), f.getName(), defaultValue);
                        if (writing)
                        {
                            defaultValue = f.getBoolean(this);
                            p.set(defaultValue);
                        }
                        else
                        {
                            boolean value = p.getBoolean();
                            f.set(this, Boolean.valueOf(value));
                        }
                    }
                    else
                    {
                        Object o = f.get(defaults);
                        if (o instanceof String[])
                        {
                            String[] defaultValue = (String[]) o;
                            p = get(c.category(), f.getName(), defaultValue);
                            if (writing)
                            {
                                o = f.get(this);
                                defaultValue = (String[]) o;
                                p.set(defaultValue);
                            }
                            else
                            {
                                String[] value = p.getStringList();
                                f.set(this, value);
                            }
                        }
                        else if (o instanceof int[])
                        {
                            int[] defaultValue = (int[]) o;
                            p = get(c.category(), f.getName(), defaultValue);
                            if (writing)
                            {
                                o = f.get(this);
                                defaultValue = (int[]) o;
                                p.set(defaultValue);
                            }
                            else
                            {
                                int[] value = p.getIntList();
                                f.set(this, value);
                            }
                        }
                        else System.err.println("Unknown Type " + f.getType() + " " + f.getName() + " " + o.getClass());
                    }
                    if (p != null)
                    {
                        p.setLanguageKey("pokecube.config." + f.getName());
                        fields.add(p);
                    }
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        ConfigCategory cc;
        List<ConfigCategory> empty = new ArrayList<>();
        for (String s : getCategoryNames())
        {
            cc = getCategory(s);
            cc.setLanguageKey("pokecube.config." + cc.getName());
            List<String> removeThis = new ArrayList<>();
            if (cc.entrySet().isEmpty()) empty.add(cc);
            for (Map.Entry<String, Property> e : cc.entrySet())
            {
                if (!fields.contains(e.getValue()))
                {
                    removeThis.add(e.getKey());
                }
            }
            for (String g : removeThis)
            {
                cc.remove(g);
            }
        }
        for (ConfigCategory cat : empty)
            removeCategory(cat);
    }

    public void updateField(Field field, String update) throws Exception
    {
        load();
        Property p = null;
        Configure c = (Configure) field.getAnnotation(Configure.class);
        if ((field.getType() == Long.TYPE) || (field.getType() == Long.class))
        {
            long defaultValue = field.getLong(defaults);
            field.set(this, Long.parseLong(update));
            p = get(c.category(), field.getName(), (int) defaultValue);
            defaultValue = field.getLong(this);
            p.set(defaultValue);
        }
        else if (field.getType() == String.class)
        {
            String defaultValue = (String) field.get(defaults);
            field.set(this, update);
            p = get(c.category(), field.getName(), defaultValue);
            defaultValue = (String) field.get(this);
            p.set(defaultValue);
        }
        else if ((field.getType() == Integer.TYPE) || (field.getType() == Integer.class))
        {
            int defaultValue = field.getInt(defaults);
            field.set(this, Integer.parseInt(update));
            p = get(c.category(), field.getName(), defaultValue);
            defaultValue = field.getInt(this);
            p.set(defaultValue);
        }
        else if ((field.getType() == Float.TYPE) || (field.getType() == Float.class))
        {
            float defaultValue = field.getFloat(defaults);
            field.set(this, Float.parseFloat(update));
            p = get(c.category(), field.getName(), defaultValue);
            defaultValue = field.getFloat(this);
            p.set(defaultValue);
        }
        else if ((field.getType() == Double.TYPE) || (field.getType() == Double.class))
        {
            double defaultValue = field.getDouble(defaults);
            field.set(this, Double.parseDouble(update));
            p = get(c.category(), field.getName(), defaultValue);
            defaultValue = field.getDouble(this);
            p.set(defaultValue);
        }
        else if ((field.getType() == Boolean.TYPE) || (field.getType() == Boolean.class))
        {
            boolean defaultValue = field.getBoolean(defaults);
            field.set(this, Boolean.parseBoolean(update));
            p = get(c.category(), field.getName(), defaultValue);
            defaultValue = field.getBoolean(this);
            p.set(defaultValue);
        }
        else
        {
            // TODO handle these properly.
            Object o = field.get(defaults);
            if (o instanceof String[])
            {
                String[] defaultValue = (String[]) o;
                p = get(c.category(), field.getName(), defaultValue);
                o = field.get(this);
                defaultValue = (String[]) o;
                p.set(defaultValue);
            }
            else if (o instanceof int[])
            {
                int[] defaultValue = (int[]) o;
                p = get(c.category(), field.getName(), defaultValue);
                o = field.get(this);
                defaultValue = (int[]) o;
                p.set(defaultValue);
            }
            else System.err.println("Unknown Type " + field.getType() + " " + field.getName() + " " + o.getClass());
        }
        applySettings();
        save();
    }

    protected abstract void applySettings();
}

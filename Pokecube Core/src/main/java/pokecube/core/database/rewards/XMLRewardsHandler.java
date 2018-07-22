package pokecube.core.database.rewards;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.LanguageMap;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;

public class XMLRewardsHandler
{
    private static int                       num           = 0;
    public static Set<String>                recipeFiles   = Sets.newHashSet();
    public static Map<String, IRewardParser> recipeParsers = Maps.newHashMap();

    public static class CaptureParser implements IRewardParser
    {
        public static class InspectCapturesReward implements IInspectReward
        {
            final ItemStack reward;
            final boolean   percent;
            final double    num;
            final String    message;
            final String    tagString;

            public InspectCapturesReward(ItemStack reward, double num, boolean percent, String message,
                    String tagString)
            {
                this.reward = reward;
                this.message = message;
                this.tagString = tagString;
                this.num = num;
                this.percent = percent;
            }

            private boolean check(Entity entity, NBTTagCompound tag, ItemStack reward, int num, boolean giveReward)
            {
                if (reward == null || tag.getBoolean(tagString)) return false;
                if (matches(num))
                {
                    if (giveReward)
                    {
                        tag.setBoolean(tagString, true);
                        entity.sendMessage(new TextComponentTranslation(message));
                        EntityPlayer entityPlayer = (EntityPlayer) entity;
                        Tools.giveItem(entityPlayer, reward.copy());
                        PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
                    }
                    return true;
                }
                return false;
            }

            private boolean matches(int num)
            {
                int required = 0;
                if (percent)
                {
                    required = (int) (this.num * Database.spawnables.size() / 100d);
                }
                else
                {
                    required = (int) (this.num);
                }
                return required <= num;
            }

            @Override
            public boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward)
            {
                int num = CaptureStats.getNumberUniqueCaughtBy(entity.getUniqueID());
                try
                {
                    return check(entity, data.tag, reward, num, giveReward);
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                return false;
            }
        }

        static final QName KEY     = new QName("key");
        static final QName NUM     = new QName("num");
        static final QName MESS    = new QName("mess");
        static final QName PERCENT = new QName("percent");

        @Override
        public void process(XMLReward reward)
        {
            String key = reward.condition.values.get(KEY);
            String mess = reward.condition.values.get(MESS);
            double num = Double.parseDouble(reward.condition.values.get(NUM));
            boolean percent = false;
            if (reward.condition.values.containsKey(PERCENT))
                percent = Boolean.parseBoolean(reward.condition.values.get(PERCENT));
            ItemStack give = getStack(reward.output);
            if (give == null || key == null || mess == null)
                throw new NullPointerException(key + " " + mess + " " + give);
            PokedexInspector.rewards.add(new InspectCapturesReward(give, num, percent, mess, key));
        }

    }

    public static class FreeBookParser implements IRewardParser
    {

        public static class FreeTranslatedReward implements IInspectReward
        {
            public final String  key;
            public final boolean watch_only;
            final String         message;
            final String         tagKey;
            final String         langFile;

            public FreeTranslatedReward(String key, String message, String tagKey, String langFile, boolean watch_only)
            {
                this.key = key;
                this.message = message;
                this.tagKey = tagKey;
                this.langFile = langFile;
                this.watch_only = watch_only;
            }

            @Override
            public boolean inspect(PokecubePlayerCustomData data, Entity entity, boolean giveReward)
            {
                if (watch_only) return false;
                String lang = data.tag.getString("lang");
                if (lang.isEmpty()) lang = "en_US";
                if (data.tag.getBoolean(key)) return false;
                if (giveReward)
                {
                    ItemStack book = getInfoBook(lang);
                    data.tag.setBoolean(key, true);
                    entity.sendMessage(new TextComponentTranslation(message));
                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                    Tools.giveItem(entityPlayer, book);
                    PokecubePlayerDataHandler.saveCustomData(entity.getCachedUniqueIdString());
                }
                return true;
            }

            public ItemStack getInfoBook(String lang)
            {
                String name = "";
                InputStream inputstream;
                Map<String, String> table = Maps.newHashMap();
                try
                {
                    inputstream = LanguageMap.class.getResourceAsStream(String.format(langFile, lang));
                    table = LanguageMap.parseLangFile(inputstream);
                    try
                    {
                        inputstream.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
                catch (Exception e1)
                {
                    PokecubeMod.log(Level.WARNING, "Error loading book for " + lang, e1);
                }
                name = table.get(tagKey);
                if (name == null)
                {
                    inputstream = LanguageMap.class.getResourceAsStream(String.format(langFile, "en_US"));
                    table = LanguageMap.parseLangFile(inputstream);
                    try
                    {
                        inputstream.close();
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    name = table.get(tagKey);
                }

                ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
                try
                {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(name));
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error with book for " + tagKey + " " + name, e);
                }
                return stack;
            }
        }

        static final QName KEY       = new QName("key");
        static final QName MESS      = new QName("mess");
        static final QName LANG      = new QName("file");
        static final QName TAG       = new QName("tag");
        static final QName WATCHONLY = new QName("watch_only");

        @Override
        public void process(XMLReward reward)
        {
            String key = reward.condition.values.get(KEY);
            String mess = reward.condition.values.get(MESS);
            String lang = reward.condition.values.get(LANG);
            String tag = reward.condition.values.get(TAG);
            boolean watch_only = false;
            if (reward.condition.values.containsKey(WATCHONLY))
            {
                watch_only = Boolean.parseBoolean(reward.condition.values.get(WATCHONLY));
            }
            if (key == null || mess == null || lang == null || tag == null)
                throw new NullPointerException(key + " " + mess + " " + lang + " " + tag);
            PokedexInspector.rewards.add(new FreeTranslatedReward(key, mess, tag, lang, watch_only));
        }
    }

    static
    {
        recipeParsers.put("default", new CaptureParser());
        recipeParsers.put("freebook", new FreeBookParser());
    }

    @XmlRootElement(name = "Rewards")
    public static class XMLRewards
    {
        @XmlElement(name = "Reward")
        public List<XMLReward> recipes = Lists.newArrayList();
    }

    @XmlRootElement(name = "Reward")
    public static class XMLReward
    {
        @XmlAttribute
        String                    handler = "default";
        @XmlAttribute
        String                    key     = "default";
        @XmlElement(name = "Item")
        public XMLRewardOutput    output;
        @XmlElement(name = "Condition")
        public XMLRewardCondition condition;
        @XmlAnyAttribute
        public Map<QName, String> values  = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "output: " + output + " condition: " + condition + " key: " + key;
        }
    }

    @XmlRootElement(name = "Item")
    public static class XMLRewardOutput extends Drop
    {
        @Override
        public String toString()
        {
            return "values: " + values + " tag: " + tag;
        }
    }

    @XmlRootElement(name = "Condition")
    public static class XMLRewardCondition
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "values: " + values;
        }
    }

    public static ItemStack getStack(Drop drop)
    {
        Map<QName, String> values = drop.values;
        if (drop.tag != null)
        {
            QName name = new QName("tag");
            values.put(name, drop.tag);
        }
        return Tools.getStack(drop.values);
    }

    public static void addReward(XMLReward recipe)
    {
        IRewardParser parser = recipeParsers.get(recipe.handler);
        try
        {
            parser.process(recipe);
            if (PokecubeMod.debug) try
            {
                File dir = new File(Database.CONFIGLOC + "rewards");
                dir.mkdirs();
                File outputFile = new File(dir, File.separator + "autoloaded" + (num++) + ".json");
                String json = parser.serialize(recipe);
                FileWriter writer = new FileWriter(outputFile);
                writer.append(json);
                writer.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (NullPointerException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with a recipe, Error for: " + recipe, e);
        }
    }
}

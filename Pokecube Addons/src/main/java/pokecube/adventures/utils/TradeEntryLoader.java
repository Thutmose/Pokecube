package pokecube.adventures.utils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer.TrainerTrade;
import pokecube.adventures.entity.trainers.TypeTrainer.TrainerTrades;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.ItemTM;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class TradeEntryLoader
{
    static final QName MIN    = new QName("min");
    static final QName MAX    = new QName("max");
    static final QName CHANCE = new QName("chance");

    static XMLDatabase database;

    @XmlRootElement(name = "AllTrades")
    public static class XMLDatabase
    {
        @XmlElement(name = "Trades")
        private List<TradeEntry> trades = Lists.newArrayList();
    }

    @XmlRootElement(name = "Trades")
    public static class TradeEntry
    {
        @XmlAttribute
        String              template = "default";
        @XmlElement(name = "Trade")
        private List<Trade> trades   = Lists.newArrayList();
    }

    @XmlRootElement(name = "Trade")
    public static class Trade
    {
        @XmlAttribute
        String             custom;
        @XmlElement(name = "Sell")
        Sell               sell;
        @XmlElement(name = "Buy")
        private List<Buy>  buys = Lists.newArrayList();
        @XmlAnyAttribute
        Map<QName, String> values;
    }

    @XmlRootElement(name = "Sell")
    public static class Sell
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;

        @Override
        public String toString()
        {
            return values + " " + tag;
        }
    }

    @XmlRootElement(name = "Buy")
    public static class Buy
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;

        @Override
        public String toString()
        {
            return values + " " + tag;
        }
    }

    public static XMLDatabase loadDatabase(File file) throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileReader reader = new FileReader(file);
        XMLDatabase database = (XMLDatabase) unmarshaller.unmarshal(reader);
        reader.close();
        return database;
    }

    public static void makeEntries(File file) throws Exception
    {
        if (database == null)
        {
            database = loadDatabase(file);
        }
        for (TradeEntry entry : database.trades)
        {
            TrainerTrades trades = new TrainerTrades();
            inner:
            for (Trade trade : entry.trades)
            {
                if (trade.custom != null)
                {
                    addTemplatedTrades(trade, trades);
                    continue inner;
                }
                Map<QName, String> values = trade.sell.values;
                TrainerTrade recipe;
                ItemStack sell = ItemStack.EMPTY;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                if (trade.sell.tag != null) values.put(new QName("tag"), trade.sell.tag);
                sell = Tools.getStack(values);
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                    buy2 = Tools.getStack(values);
                }
                if (!CompatWrapper.isValid(sell))
                {
                    System.err.println("No Sell:" + trade.sell + " " + trade.buys);
                    continue;
                }

                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                trades.tradesList.add(recipe);
            }
            TypeTrainer.tradesMap.put(entry.template, trades);
        }
    }

    private static void addTemplatedTrades(Trade trade, TrainerTrades trades)
    {
        String custom = trade.custom;
        if (custom.equals("allMegas"))
        {
            for (String s : ItemGenerator.variants)
            {
                if ((s.contains("mega") && !s.equals("megastone")) || s.contains("orb"))
                {
                    ItemStack sell = PokecubeItems.getStack(s);
                    sell.setItemDamage(0);
                    Map<QName, String> values;
                    TrainerTrade recipe;
                    ItemStack buy1 = ItemStack.EMPTY;
                    ItemStack buy2 = ItemStack.EMPTY;
                    values = trade.buys.get(0).values;
                    if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                    buy1 = Tools.getStack(values);
                    if (trade.buys.size() > 1)
                    {
                        values = trade.buys.get(1).values;
                        if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                        buy2 = Tools.getStack(values);
                    }
                    recipe = new TrainerTrade(buy1, buy2, sell);
                    values = trade.values;
                    if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                    if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                    if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                    trades.tradesList.add(recipe);
                }
            }
        }
        else if (custom.equals("allVitamins"))
        {
            for (String s : ItemVitamin.vitamins)
            {
                ItemStack sell = PokecubeItems.getStack(s);
                sell.setItemDamage(0);
                Map<QName, String> values;
                TrainerTrade recipe;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                    buy2 = Tools.getStack(values);
                }
                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                trades.tradesList.add(recipe);
            }
        }
        else if (custom.equals("allGenericHeld"))
        {
            for (String s : ItemGenerator.variants)
            {
                if ((s.contains("mega") && !s.equals("megastone")) || s.contains("orb") || s.equals("shiny_charm"))
                    continue;

                ItemStack sell = PokecubeItems.getStack(s);
                sell.setItemDamage(0);
                Map<QName, String> values;
                TrainerTrade recipe;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                    buy2 = Tools.getStack(values);
                }
                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                trades.tradesList.add(recipe);
            }
        }
        else if (custom.equals("allTMs"))
        {
            ArrayList<String> moves = Lists.newArrayList(MovesUtils.moves.keySet());
            Collections.sort(moves);
            for (int i = 0; i < moves.size(); i++)
            {
                int index = i;
                String name = moves.get(index);
                ItemStack sell = ItemTM.getTM(name);
                Map<QName, String> values;
                TrainerTrade recipe;
                ItemStack buy1 = ItemStack.EMPTY;
                ItemStack buy2 = ItemStack.EMPTY;
                values = trade.buys.get(0).values;
                if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                buy1 = Tools.getStack(values);
                if (trade.buys.size() > 1)
                {
                    values = trade.buys.get(1).values;
                    if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                    buy2 = Tools.getStack(values);
                }
                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                trades.tradesList.add(recipe);
            }
        }
        else if (custom.equals("sellRandomBadge"))
        {
            for (PokeType type : PokeType.values())
                if (type != PokeType.unknown)
                {
                    ItemStack badge = PokecubeItems.getStack("badge_" + type);
                    if (CompatWrapper.isValid(badge))
                    {
                        badge.setItemDamage(0);
                        Map<QName, String> values;
                        TrainerTrade recipe;
                        ItemStack buy1 = ItemStack.EMPTY;
                        ItemStack buy2 = ItemStack.EMPTY;
                        values = trade.buys.get(0).values;
                        if (trade.buys.get(0).tag != null) values.put(new QName("tag"), trade.buys.get(0).tag);
                        buy1 = Tools.getStack(values);
                        if (trade.buys.size() > 1)
                        {
                            values = trade.buys.get(1).values;
                            if (trade.buys.get(1).tag != null) values.put(new QName("tag"), trade.buys.get(1).tag);
                            buy2 = Tools.getStack(values);
                        }
                        recipe = new TrainerTrade(buy1, buy2, badge);
                        values = trade.values;
                        if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                        if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                        if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                        trades.tradesList.add(recipe);
                    }
                }
        }
        else if (custom.equals("buyRandomBadge"))
        {
            for (PokeType type : PokeType.values())
                if (type != PokeType.unknown)
                {
                    ItemStack badge = PokecubeItems.getStack("badge_" + type);
                    if (CompatWrapper.isValid(badge))
                    {
                        badge.setItemDamage(0);
                        Map<QName, String> values = trade.sell.values;
                        TrainerTrade recipe;
                        if (trade.sell.tag != null) values.put(new QName("tag"), trade.sell.tag);
                        ItemStack sell = Tools.getStack(values);
                        recipe = new TrainerTrade(badge, ItemStack.EMPTY, sell);
                        values = trade.values;
                        if (values.containsKey(CHANCE)) recipe.chance = Float.parseFloat(values.get(CHANCE));
                        if (values.containsKey(MIN)) recipe.min = Integer.parseInt(values.get(MIN));
                        if (values.containsKey(MAX)) recipe.max = Integer.parseInt(values.get(MAX));
                        trades.tradesList.add(recipe);
                    }
                }
        }
    }

}

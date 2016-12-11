/** The MIT License (MIT) Copyright (c) 2015 Cyclops Permission is hereby
 * granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this
 * permission notice shall be included in all copies or substantial portions of
 * the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE. */

package pokecube.adventures.handlers.loot;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Helpers related to loot stuff.
 * 
 * @author rubensworks */
public class LootHelpers
{

    public static List<LootPool> getLootPools(LootTable lootTable)
    {
        return ReflectionHelper.getPrivateValue(LootTable.class, lootTable, "pools", "field_186466_c", "c");
    }

    static final LootHelpers                                                 INSTANCE                  = new LootHelpers();
    private static final Multimap<ResourceLocation, LootPool>                INJECT_LOOTPOOLS          = MultimapBuilder.ListMultimapBuilder
            .hashKeys().arrayListValues().build();
    private static final Multimap<Pair<ResourceLocation, String>, LootEntry> INJECT_LOOTENTRIES        = MultimapBuilder.ListMultimapBuilder
            .hashKeys().arrayListValues().build();

    public static List<ResourceLocation>                                     VANILLA_LOOT_CHEST_TABLES = Lists
            .newArrayList(LootTableList.CHESTS_ABANDONED_MINESHAFT, LootTableList.CHESTS_DESERT_PYRAMID,
                    LootTableList.CHESTS_END_CITY_TREASURE, LootTableList.CHESTS_IGLOO_CHEST,
                    LootTableList.CHESTS_JUNGLE_TEMPLE, LootTableList.CHESTS_NETHER_BRIDGE,
                    LootTableList.CHESTS_SIMPLE_DUNGEON, LootTableList.CHESTS_SPAWN_BONUS_CHEST,
                    LootTableList.CHESTS_STRONGHOLD_CORRIDOR, LootTableList.CHESTS_STRONGHOLD_CROSSING,
                    LootTableList.CHESTS_STRONGHOLD_LIBRARY, LootTableList.CHESTS_VILLAGE_BLACKSMITH);

    private LootHelpers()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        // Inject pools into tables
        for (Map.Entry<ResourceLocation, LootPool> poolEntry : INJECT_LOOTPOOLS.entries())
        {
            ResourceLocation resourceLocation = poolEntry.getKey();
            LootPool pool = poolEntry.getValue();
            if (event.getName().equals(resourceLocation))
            {
                event.getTable().addPool(pool);
            }
        }

        // Inject entries into pools
        for (Map.Entry<Pair<ResourceLocation, String>, LootEntry> entryItemEntry : INJECT_LOOTENTRIES.entries())
        {
            ResourceLocation resourceLocation = entryItemEntry.getKey().getKey();
            String poolName = entryItemEntry.getKey().getValue();
            LootEntry entryItem = entryItemEntry.getValue();
            if (event.getName().equals(resourceLocation))
            {
                LootTable lootTable = event.getTable();
                if (poolName == null)
                {
                    List<LootPool> pools = getLootPools(lootTable);
                    for (LootPool pool : pools)
                    {
                        if (!pool.isFrozen())
                        {
                            pool.addEntry(entryItem);
                        }
                    }
                }
                else
                {
                    LootPool lootPool = lootTable.getPool(poolName);
                    if (lootPool == null) { throw new RuntimeException(String
                            .format("Could not find loot pool %s in loot table %s.", poolName, resourceLocation)); }
                    if (!lootPool.isFrozen())
                    {
                        lootPool.addEntry(entryItem);
                    }
                }
            }
        }
    }

    /** Add the given entries to all vanilla loot chests.
     * 
     * @param lootEntryItems
     *            The new entries. */
    public static void addVanillaLootChestLootEntry(LootEntry... lootEntryItems)
    {
        for (ResourceLocation lootTable : VANILLA_LOOT_CHEST_TABLES)
        {
            addLootEntry(lootTable, null, lootEntryItems);
        }
    }

    /** Add the given entries to all vanilla loot chests.
     * 
     * @param poolName
     *            The name, or null for all pools.
     * @param lootEntryItems
     *            The new entries. */
    public static void addVanillaLootChestLootEntry(@Nullable String poolName, LootEntry... lootEntryItems)
    {
        for (ResourceLocation lootTable : VANILLA_LOOT_CHEST_TABLES)
        {
            addLootEntry(lootTable, poolName, lootEntryItems);
        }
    }

    /** Add the given loot pool to all vanilla loot chests.
     * 
     * @param lootPool
     *            The new loot pool. */
    public static void addVanillaLootChestLootPool(LootPool lootPool)
    {
        for (ResourceLocation lootTable : VANILLA_LOOT_CHEST_TABLES)
        {
            addLootPool(lootTable, lootPool);
        }
    }

    /** Add entries to the given loot table.
     * 
     * @param lootTable
     *            The loot table location.
     * @param poolName
     *            The name, or null for all pools.
     * @param lootEntryItems
     *            The new entries. */
    public static void addLootEntry(ResourceLocation lootTable, @Nullable String poolName, LootEntry... lootEntryItems)
    {
        for (LootEntry lootEntryItem : lootEntryItems)
        {
            INJECT_LOOTENTRIES.put(Pair.of(lootTable, poolName), lootEntryItem);
        }
    }

    /** Add a loot pool to the given loot table.
     * 
     * @param lootTable
     *            The loot table location.
     * @param lootPool
     *            The new pool. */
    public static void addLootPool(ResourceLocation lootTable, LootPool lootPool)
    {
        INJECT_LOOTPOOLS.put(lootTable, lootPool);
    }

}
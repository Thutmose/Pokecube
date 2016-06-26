package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.getItem;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;

import java.util.Collection;
import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.comands.Config;
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.ItemExpShare;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.items.ItemTM;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class ItemHandler
{
    public static Item badges;

    public static void addBadges()
    {
        badges = new ItemBadge().setRegistryName(PokecubeAdv.ID, "badge");
        PokecubeItems.register(badges);
        for (String s : ItemBadge.variants)
        {
            ItemStack stack = new ItemStack(badges);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
            PokecubeItems.addSpecificItemStack(s, stack);
        }
    }

    public static void initBadges()
    {
        for (PokeType type : PokeType.values())
        {
            if (type != PokeType.unknown)
            {
                PokecubeItems.addToHoldables("badge" + type);
            }
        }
    }

    private boolean isValidTable(ResourceLocation input, ItemStack stack)
    {
        if (Tools.isSameStack(stack, PokecubeItems.getStack("exp_share")))
            return input.equals(LootTableList.CHESTS_SPAWN_BONUS_CHEST)
                    || input.equals(LootTableList.CHESTS_SIMPLE_DUNGEON)
                    || input.equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)
                    || input.equals(LootTableList.CHESTS_JUNGLE_TEMPLE)
                    || input.equals(LootTableList.CHESTS_ABANDONED_MINESHAFT);
        return input.equals(LootTableList.CHESTS_SPAWN_BONUS_CHEST) || input.equals(LootTableList.CHESTS_SIMPLE_DUNGEON)
                || input.equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)
                || input.equals(LootTableList.CHESTS_JUNGLE_TEMPLE)
                || input.equals(LootTableList.CHESTS_ABANDONED_MINESHAFT);
    }

    @SubscribeEvent
    public void initLootTables(LootTableLoadEvent event)
    {
        final ItemStack share = PokecubeItems.getStack("exp_share");
        if (Config.instance.exp_shareLoot && share != null && isValidTable(event.getName(), share))
            addLoot(share, "pokecube_adventures:exp_share", event.getTable(), 100, 10);
        if (Config.instance.HMLoot)
        {
            ItemStack cut = new ItemStack(getItem("tm"));
            ItemTM.addMoveToStack(IMoveNames.MOVE_CUT, cut);
            ItemStack flash = new ItemStack(getItem("tm"));
            ItemTM.addMoveToStack(IMoveNames.MOVE_FLASH, flash);
            ItemStack dig = new ItemStack(getItem("tm"));
            ItemTM.addMoveToStack(IMoveNames.MOVE_DIG, dig);
            ItemStack rocksmash = new ItemStack(getItem("tm"));
            ItemTM.addMoveToStack(IMoveNames.MOVE_ROCKSMASH, rocksmash);
            if (event.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE))
                addLoot(cut, "pokecube_adventures:cut", event.getTable(), 100, 10);
            if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT))
                addLoot(dig, "pokecube_adventures:dig", event.getTable(), 100, 10);
            if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT))
                addLoot(rocksmash, "pokecube_adventures:rocksmash", event.getTable(), 100, 10);
            if (event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON))
                addLoot(flash, "pokecube_adventures:flash", event.getTable(), 100, 10);
        }
    }

    public void addLoot(final ItemStack loot, final String poolName, LootTable table, int weight, int num)
    {
        LootEntry[] entries = new LootEntry[1];
        LootCondition[] conditions = new LootCondition[1];
        final LootFunction[] functions = new LootFunction[1];

        conditions[0] = new LootCondition()
        {
            @Override
            public boolean testCondition(Random rand, LootContext context)
            {
                return true;
            }
        };

        functions[0] = new LootFunction(conditions)
        {
            @Override
            public ItemStack apply(ItemStack stack, Random rand, LootContext context)
            {
                return stack;
            }
        };

        entries[0] = new LootEntry(weight, num, conditions, poolName)
        {
            @Override
            protected void serialize(JsonObject json, JsonSerializationContext context)
            {
                if (functions != null && functions.length > 0)
                {
                    json.add("functions", context.serialize(functions));
                }

                ResourceLocation resourcelocation = new ResourceLocation(poolName);
                json.addProperty("name", resourcelocation.toString());
            }

            @Override
            public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
            {
                int i = 0;
                ItemStack itemstack = loot;
                for (int j = functions.length; i < j; ++i)
                {
                    LootFunction lootfunction = functions[i];

                    if (LootConditionManager.testAllConditions(lootfunction.getConditions(), rand, context))
                    {
                        itemstack = lootfunction.apply(itemstack, rand, context);
                    }
                }

                if (itemstack.stackSize > 0)
                {
                    if (itemstack.stackSize < itemstack.getItem().getItemStackLimit(itemstack))
                    {
                        stacks.add(itemstack);
                    }
                    else
                    {
                        i = itemstack.stackSize;

                        while (i > 0)
                        {
                            ItemStack itemstack1 = itemstack.copy();
                            itemstack1.stackSize = Math.min(itemstack.getMaxStackSize(), i);
                            i -= itemstack1.stackSize;
                            stacks.add(itemstack1);
                        }
                    }
                }
            }
        };

        RandomValueRange rollsIn = new RandomValueRange(1);
        RandomValueRange bonusRollsIn = new RandomValueRange(1);

        LootPool pool = new LootPool(entries, conditions, rollsIn, bonusRollsIn, poolName);
        table.addPool(pool);
    }

    public static void registerItems()
    {
        Item expshare = (new ItemExpShare()).setUnlocalizedName("exp_share");
        expshare.setCreativeTab(creativeTabPokecube);
        register(expshare, "exp_share");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(expshare, 0,
                    new ModelResourceLocation("pokecube_adventures:exp_share", "inventory"));
        }

        PokecubeItems.addToHoldables("exp_share");

        Item mewHair = (new Item()).setUnlocalizedName("silkyhair").setRegistryName(PokecubeAdv.ID, "mewHair");
        GameRegistry.register(mewHair);
        PokecubeItems.addGeneric("mewHair", mewHair);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(mewHair, 0,
                    new ModelResourceLocation("pokecube_adventures:mewHair", "inventory"));
        }

        Item target = new ItemTarget().setUnlocalizedName("pokemobTarget").setCreativeTab(creativeTabPokecube);
        register(target);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ModelBakery.registerItemVariants(target, new ResourceLocation("pokecube_adventures:spawner"));
            PokecubeItems.registerItemTexture(target, 0,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(target, 1,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(target, 2,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(target, 3,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
        }
        Item trainer = new ItemTrainer().setUnlocalizedName("trainerSpawner").setCreativeTab(creativeTabPokecube);
        register(trainer);
        addSpecificItemStack("traderSpawner", new ItemStack(trainer, 1, 2));
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ModelBakery.registerItemVariants(trainer, new ResourceLocation("pokecube_adventures:spawner"));
            PokecubeItems.registerItemTexture(trainer, 0,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(trainer, 1,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(trainer, 2,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
            PokecubeItems.registerItemTexture(trainer, 3,
                    new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
        }
        Item bag = new ItemBag().setUnlocalizedName("pokecubebag").setCreativeTab(creativeTabPokecube);
        register(bag);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ModelBakery.registerItemVariants(bag, new ResourceLocation("pokecube_adventures:bag"));
            PokecubeItems.registerItemTexture(bag, 0,
                    new ModelResourceLocation("pokecube_adventures:bag", "inventory"));
        }
        addSpecificItemStack("warplinker", new ItemStack(target, 1, 1));

        addBadges();
    }
}

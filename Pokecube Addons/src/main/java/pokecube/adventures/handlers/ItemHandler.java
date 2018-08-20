package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.register;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.adventures.handlers.loot.Loot;
import pokecube.adventures.handlers.loot.LootHelpers;
import pokecube.adventures.handlers.loot.MakeDnaBottle;
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.ItemExpShare;
import pokecube.adventures.items.ItemLinker;
import pokecube.adventures.items.ItemSubbiomeSetter;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.ItemBlockGeneric;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.ItemGenerator.IMoveModifier;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;

public class ItemHandler
{
    static
    {
        LootFunctionManager.registerFunction(new MakeDnaBottle.Serializer());
    }

    public static void addBadges(Object registry)
    {
        for (PokeType type : PokeType.values())
        {
            Item badge = new ItemBadge(type);
            PokecubeItems.register(badge, registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                String name = type.name.equals("???") ? "unknown" : type.name;
                registerItemTexture(badge, 0,
                        new ModelResourceLocation("pokecube_adventures:" + "badge_" + name, "inventory"));
            }
            ItemStack stack = new ItemStack(badge, 1, 0);
            PokecubeItems.addToHoldables(stack);
        }
    }

    public static void registerItems(Object registry)
    {
        Item expshare = (new ItemExpShare()).setUnlocalizedName("exp_share").setRegistryName(PokecubeAdv.ID,
                "exp_share");
        expshare.setHasSubtypes(true);
        expshare.setCreativeTab(creativeTabPokecube);
        register(expshare, registry);
        PokecubeItems.addSpecificItemStack("exp_share", new ItemStack(expshare));
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(expshare, 0,
                    new ModelResourceLocation("pokecube_adventures:exp_share", "inventory"));
        }

        PokecubeItems.addToHoldables("exp_share");

        Item mewHair = (new Item()).setUnlocalizedName("silkyhair").setRegistryName(PokecubeAdv.ID, "mewhair");
        register(mewHair, registry);
        PokecubeItems.addGeneric("mewhair", mewHair);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(mewHair, 0,
                    new ModelResourceLocation("pokecube_adventures:mewhair", "inventory"));
        }

        Item target = new ItemTarget().setRegistryName(PokecubeAdv.ID, "target").setUnlocalizedName("target")
                .setCreativeTab(creativeTabPokecube);
        register(target, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(target, 0,
                    new ModelResourceLocation(target.getRegistryName().toString(), "inventory"));
        }
        target = new ItemLinker().setRegistryName(PokecubeAdv.ID, "linker").setUnlocalizedName("linker")
                .setCreativeTab(creativeTabPokecube);
        register(target, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(target, 0,
                    new ModelResourceLocation(target.getRegistryName().toString(), "inventory"));
        }
        target = new ItemSubbiomeSetter().setRegistryName(PokecubeAdv.ID, "biome_setter")
                .setUnlocalizedName("biome_setter").setCreativeTab(creativeTabPokecube);
        register(target, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(target, 0,
                    new ModelResourceLocation(target.getRegistryName().toString(), "inventory"));
        }
        Item trainer = new ItemTrainer().setUnlocalizedName("trainerspawner")
                .setRegistryName(PokecubeAdv.ID, "trainerspawner").setCreativeTab(creativeTabPokecube);
        register(trainer, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(trainer, 0,
                    new ModelResourceLocation(trainer.getRegistryName().toString(), "inventory"));
        }
        Item bag = new ItemBag().setUnlocalizedName("pokecubebag").setRegistryName(PokecubeAdv.ID, "pokecubebag")
                .setCreativeTab(creativeTabPokecube);
        register(bag, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(bag, 0,
                    new ModelResourceLocation(bag.getRegistryName().toString(), "inventory"));
        }
        addBadges(registry);

        ContainerPC.CUSTOMPCWHILTELIST.add(new Predicate<ItemStack>()
        {
            @Override
            public boolean test(ItemStack t)
            {
                return t.getItem() instanceof ItemBag || t.getItem() instanceof ItemBadge;
            }
        });

        ItemGenerator.ITEMMODIFIERS.put(new Predicate<ItemStack>()
        {
            @Override
            public boolean test(ItemStack t)
            {
                return ItemBadge.isBadge(t);
            }
        }, new IMoveModifier()
        {
            @Override
            public void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held)
            {
                if (mob != moveUse.attacker) return;
                PokeType type = PokeType.values()[held.getItemDamage()];
                if (type == moveUse.attackType) moveUse.PWR *= 1.2;
            }
        });

        for (Block block : BlockHandler.blocks)
        {
            ItemBlock item = new ItemBlockGeneric(block);
            register(item, registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                registerItemTexture(item, 0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
        }
    }

    public static void handleLoot()
    {
        if (Config.instance.loot_exp_share)
        {
            ItemStack share = PokecubeItems.getStack("exp_share");
            LootHelpers.addLootEntry(LootTableList.CHESTS_SIMPLE_DUNGEON, null,
                    Loot.getEntryItem(share, 10, 1, "pokecube_adventures:exp_share"));
        }
        if (Config.instance.loot_larvesta)
        {
            PokedexEntry larvesta = Database.getEntry("larvesta");
            if (larvesta != null)
            {
                ItemStack stack = ItemPokemobEgg.getEggStack(larvesta);
                LootHelpers.addLootEntry(LootTableList.CHESTS_DESERT_PYRAMID, null,
                        Loot.getEntryItem(stack, 10, 1, "pokecube_adventures:exp_share"));
            }
            else PokecubeMod.log("No Larvesta Found, not adding loot for it :(");
        }
        if (Config.instance.loot_hms)
        {
            ItemStack cut = ItemTM.getTM(IMoveNames.MOVE_CUT);
            ItemStack flash = ItemTM.getTM(IMoveNames.MOVE_FLASH);
            ItemStack dig = ItemTM.getTM(IMoveNames.MOVE_DIG);
            ItemStack rocksmash = ItemTM.getTM(IMoveNames.MOVE_ROCKSMASH);

            LootHelpers.addLootEntry(LootTableList.CHESTS_JUNGLE_TEMPLE, null,
                    Loot.getEntryItem(cut, 10, 1, "pokecube_adventures:cut"));
            LootHelpers.addLootEntry(LootTableList.CHESTS_ABANDONED_MINESHAFT, null,
                    Loot.getEntryItem(dig, 10, 1, "pokecube_adventures:dig"));
            LootHelpers.addLootEntry(LootTableList.CHESTS_ABANDONED_MINESHAFT, null,
                    Loot.getEntryItem(rocksmash, 10, 1, "pokecube_adventures:rocksmash"));
            LootHelpers.addLootEntry(LootTableList.CHESTS_SIMPLE_DUNGEON, null,
                    Loot.getEntryItem(flash, 10, 1, "pokecube_adventures:flash"));
        }
    }
}

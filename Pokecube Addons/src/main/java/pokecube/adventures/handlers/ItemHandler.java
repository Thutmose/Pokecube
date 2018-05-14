package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.getItem;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;

import java.util.function.Predicate;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.ItemBlockAFA;
import pokecube.adventures.blocks.cloner.block.ItemBlockCloner;
import pokecube.adventures.commands.Config;
import pokecube.adventures.handlers.loot.Loot;
import pokecube.adventures.handlers.loot.LootHelpers;
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.ItemExpShare;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.handlers.HeldItemHandler.IMoveModifier;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.items.ItemTM;
import pokecube.core.utils.PokeType;

public class ItemHandler
{
    public static Item badges = new ItemBadge().setRegistryName(PokecubeAdv.ID, "badge");

    public static void addBadges(Object registry)
    {
        PokecubeItems.register(badges, registry);
        for (PokeType type : PokeType.values())
        {
            ItemStack stack = new ItemStack(badges, 1, type.ordinal());
            PokecubeItems.addSpecificItemStack("badge_" + type.name, stack);
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

        Item target = new ItemTarget().setUnlocalizedName("pokemobTarget")
                .setRegistryName(PokecubeAdv.ID, "pokemobTarget").setCreativeTab(creativeTabPokecube);
        register(target, registry);
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
        Item trainer = new ItemTrainer().setUnlocalizedName("trainerspawner")
                .setRegistryName(PokecubeAdv.ID, "trainerspawner").setCreativeTab(creativeTabPokecube);
        register(trainer, registry);
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
        Item bag = new ItemBag().setUnlocalizedName("pokecubebag").setRegistryName(PokecubeAdv.ID, "pokecubebag")
                .setCreativeTab(creativeTabPokecube);
        register(bag, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ModelBakery.registerItemVariants(bag, new ResourceLocation("pokecube_adventures:bag"));
            PokecubeItems.registerItemTexture(bag, 0,
                    new ModelResourceLocation("pokecube_adventures:bag", "inventory"));
        }
        addSpecificItemStack("warplinker", new ItemStack(target, 1, 1));
        addBadges(registry);

        ItemBlock item = new ItemBlockCloner(BlockHandler.cloner);
        item.setRegistryName(BlockHandler.cloner.getRegistryName());
        register(item, registry);
        PokecubeItems.addSpecificItemStack("extractor", new ItemStack(BlockHandler.cloner, 1, 2));
        PokecubeItems.addSpecificItemStack("splicer", new ItemStack(BlockHandler.cloner, 1, 1));
        PokecubeItems.addSpecificItemStack("reanimator", new ItemStack(BlockHandler.cloner, 1, 0));

        item = new ItemBlockAFA(BlockHandler.afa);
        item.setRegistryName(BlockHandler.afa.getRegistryName());
        register(item, registry);
        PokecubeItems.addSpecificItemStack("daycare", new ItemStack(BlockHandler.afa, 1, 1));
        PokecubeItems.addSpecificItemStack("commander", new ItemStack(BlockHandler.afa, 1, 2));
        PokecubeItems.addSpecificItemStack("afa", new ItemStack(BlockHandler.afa, 1, 0));

        item = new ItemBlock(BlockHandler.siphon);
        item.setRegistryName(BlockHandler.siphon.getRegistryName());
        register(item, registry);

        item = new ItemBlock(BlockHandler.warppad);
        item.setRegistryName(BlockHandler.warppad.getRegistryName());
        register(item, registry);

        ContainerPC.CUSTOMPCWHILTELIST.add(new Predicate<ItemStack>()
        {
            @Override
            public boolean test(ItemStack t)
            {
                return t.getItem() instanceof ItemBag || t.getItem() instanceof ItemBadge;
            }
        });

        HeldItemHandler.ITEMMODIFIERS.put(new Predicate<ItemStack>()
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
    }

    public static void handleLoot()
    {
        ItemStack share = PokecubeItems.getStack("exp_share");
        if (Config.instance.exp_shareLoot) LootHelpers.addLootEntry(LootTableList.CHESTS_SIMPLE_DUNGEON, null,
                Loot.getEntryItem(share, 10, 1, "pokecube_adventures:exp_share"));
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

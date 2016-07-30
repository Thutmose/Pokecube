package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.ItemExpShare;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;

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

    public static void postInitItems()
    {
        ItemStack share = PokecubeItems.getStack("exp_share");
        WeightedRandomChestContent shareContent = new WeightedRandomChestContent(share, 1, 1, 2);
        ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, shareContent);
        ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, shareContent);
        ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, shareContent);
        ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, shareContent);

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

        Item mewHair = (new Item()).setUnlocalizedName("silkyhair");
        GameRegistry.registerItem(mewHair, "mewHair");
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

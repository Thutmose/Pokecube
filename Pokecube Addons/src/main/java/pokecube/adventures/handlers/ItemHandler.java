package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.ItemExpShare;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.core.PokecubeItems;
import pokecube.core.items.ItemTranslated;

public class ItemHandler 
{
	public static void registerItems()
	{
        Item expshare = (new ItemExpShare()).setUnlocalizedName("exp_share");
        expshare.setCreativeTab(creativeTabPokecube);
        register(expshare, "exp_share");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
        	PokecubeItems.registerItemTexture(expshare, 0, new ModelResourceLocation("pokecube_adventures:exp_share", "inventory"));
        }
        
        PokecubeItems.addToHoldables("exp_share");
//        ItemStack share = PokecubeItems.getStack("exp_share");
        
        Item mewHair = (new ItemTranslated()).setModId(
                PokecubeAdv.ID).setUnlocalizedName("silkyhair");
        GameRegistry.registerItem(mewHair, "mewHair");
        PokecubeItems.addGeneric("mewHair", mewHair);

        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(mewHair, 0, new ModelResourceLocation("pokecube_adventures:mewHair", "inventory"));
        }
        
        Item target = new ItemTarget().setUnlocalizedName("pokemobTarget").setCreativeTab(creativeTabPokecube);
        register(target);
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
        	ModelBakery.addVariantName(target, "pokecube_adventures:spawner");
	    	PokecubeItems.registerItemTexture(target, 0, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(target, 1, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(target, 2, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(target, 3, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
        }
        Item trainer = new ItemTrainer().setUnlocalizedName("trainerSpawner").setCreativeTab(creativeTabPokecube);
        register(trainer);
        addSpecificItemStack("traderSpawner", new ItemStack(trainer, 1, 2));
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
        	ModelBakery.addVariantName(trainer, "pokecube_adventures:spawner");
	    	PokecubeItems.registerItemTexture(trainer, 0, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(trainer, 1, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(trainer, 2, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
	    	PokecubeItems.registerItemTexture(trainer, 3, new ModelResourceLocation("pokecube_adventures:spawner", "inventory"));
        }
        Item bag = new ItemBag().setUnlocalizedName("pokecubebag").setCreativeTab(creativeTabPokecube);
        register(bag);
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
        	ModelBakery.addVariantName(bag, "pokecube_adventures:bag");
        	PokecubeItems.registerItemTexture(bag, 0, new ModelResourceLocation("pokecube_adventures:bag", "inventory"));
        }
        addSpecificItemStack("warplinker", new ItemStack(target,1,1));
        
        addBadges();
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
	
	public static void addBadges() {
		Item temp;
		
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgenormal"), "badgenormal");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgenormal", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgefighting"), "badgefighting");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgefighting", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgeflying"), "badgeflying");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgeflying", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgesteel"), "badgesteel");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgesteel", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgefire"), "badgefire");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgefire", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgewater"), "badgewater");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgewater", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
        					.setUnlocalizedName("badgegrass"), "badgegrass");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgegrass", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
        					.setUnlocalizedName("badgeelectric"), "badgeelectric");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgeelectric", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgepsychic"), "badgepsychic");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgepsychic", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgerock"), "badgerock");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgerock", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgeghost"), "badgeghost");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgeghost", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgeice"), "badgeice");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgeice", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgedragon"), "badgedragon");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgedragon", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgedark"), "badgedark");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgedark", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgefairy"), "badgefairy");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgefairy", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgeground"), "badgeground");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgeground", "inventory"));
		GameRegistry.registerItem(temp = new ItemTranslated().setModId(PokecubeAdv.ID).setCreativeTab(creativeTabPokecube)
							.setUnlocalizedName("badgebug"), "badgebug");
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	PokecubeItems.registerItemTexture(temp, 0, new ModelResourceLocation("pokecube_adventures:badgebug", "inventory"));

	}
}

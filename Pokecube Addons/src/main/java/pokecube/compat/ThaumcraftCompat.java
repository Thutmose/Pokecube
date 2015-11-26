package pokecube.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class ThaumcraftCompat {
	
	private static Map<PokeType, AspectList> pokeTypeToAspects = new HashMap<PokeType, AspectList>();
	
	static {
		addMapping(PokeType.unknown, new AspectList().add(Aspect.ELDRITCH, 2));
		addMapping(PokeType.normal, new AspectList().add(Aspect.ORDER, 2));
		
//		addMapping(PokeType.fighting, new AspectList().add(Aspect.WEAPON, 2));
		addMapping(PokeType.fighting, new AspectList().add(Aspect.BEAST, 2));
		
		addMapping(PokeType.flying, new AspectList().add(Aspect.FLIGHT, 2));
		
		addMapping(PokeType.poison, new AspectList().add(Aspect.TRAP, 2));
//		addMapping(PokeType.poison, new AspectList().add(Aspect.POISON, 2));
		
		addMapping(PokeType.ground, new AspectList().add(Aspect.EARTH, 2));
		
//		addMapping(PokeType.rock, new AspectList().add(Aspect.MINE, 2));
//		addMapping(PokeType.bug, new AspectList().add(Aspect.HUNGER, 2));
		addMapping(PokeType.rock, new AspectList().add(Aspect.CRYSTAL, 2));
		addMapping(PokeType.bug, new AspectList().add(Aspect.AVERSION, 2));
		
		addMapping(PokeType.ghost, new AspectList().add(Aspect.SOUL, 2));
		addMapping(PokeType.steel, new AspectList().add(Aspect.METAL, 2));
		addMapping(PokeType.fire, new AspectList().add(Aspect.FIRE, 2));
		addMapping(PokeType.water, new AspectList().add(Aspect.WATER, 2));
		addMapping(PokeType.grass, new AspectList().add(Aspect.PLANT, 2));
		addMapping(PokeType.electric, new AspectList().add(Aspect.ENERGY, 2));
		addMapping(PokeType.psychic, new AspectList().add(Aspect.MIND, 2));
		addMapping(PokeType.ice, new AspectList().add(Aspect.COLD, 2));

//		addMapping(PokeType.dragon, new AspectList().add(Aspect.WEATHER, 2));
		addMapping(PokeType.dragon, new AspectList().add(Aspect.PROTECT, 2));
		
		addMapping(PokeType.dark, new AspectList().add(Aspect.DARKNESS, 2));
		addMapping(PokeType.fairy, new AspectList().add(Aspect.AURA, 2));
//		addPage();
//		addResearch();
	}
	
	private static void addMapping(PokeType type, AspectList aspects) {
		if(pokeTypeToAspects.containsKey(type)) {
			throw new IllegalArgumentException("PokeType aspects already registered");
		}
		pokeTypeToAspects.put(type, aspects);
	}
	
	public AspectList getAspects(PokedexEntry entry) {
		
		PokeType type1 = entry.getType1();
		PokeType type2 = entry.getType2();
		
		AspectList aspects = new AspectList();
		
		aspects.add(Aspect.BEAST, 2);
		
		if(type2 != PokeType.unknown)
		{
			aspects.add(pokeTypeToAspects.get(type1));
			aspects.add(pokeTypeToAspects.get(type1));
			aspects.add(pokeTypeToAspects.get(type2));
		} else {
			aspects.add(pokeTypeToAspects.get(type1));
			aspects.add(pokeTypeToAspects.get(type1));
			aspects.add(pokeTypeToAspects.get(type1));
			aspects.add(pokeTypeToAspects.get(type1));
		}
		
		if(entry.swims())
		{
			aspects.add(new AspectList().add(Aspect.WATER, 2));
		}
		
		if(entry.flys())
		{
			aspects.add(new AspectList().add(Aspect.AIR, 2));
		}
		
		if(entry.getFoodDrop(0) != null)
		{
			aspects.add(new AspectList().add(Aspect.LIFE, 1));
		}
		
		return aspects;
	}
	
	@SubscribeEvent
	public void init(PostPostInit evt) {
		
		System.out.println("Attempting to register pokecube entities with Thaumcraft...");
		registerPokemobsThaumcraft();
		registerTrainersThaumcraft();
	}
	
	public void registerTrainersThaumcraft() {
		
		Class klass = pokecube.adventures.entity.trainers.EntityTrainer.class;
		
		String name = (String) EntityList.classToStringMapping.get(klass);
		
		AspectList aspects = new AspectList();
		
		aspects.add(Aspect.MAN, 4);
		aspects.add(Aspect.BEAST, 2);
		aspects.add(Aspect.ORDER, 2);
		aspects.add(Aspect.TRAP, 1);
		aspects.add(Aspect.EXCHANGE, 1);
		
		if(name != null) {
			ThaumcraftApi.registerEntityTag(name, aspects);
		}
	}
	
	public void registerPokemobsThaumcraft() {
		
		for(Integer i : Pokedex.getInstance().getEntries()) {
			PokedexEntry entry = Pokedex.getInstance().getEntry(i);
			Class klass = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
			
			if(klass != null) {
				String name = (String) EntityList.classToStringMapping.get(klass);
				
				if(name != null) {
					ThaumcraftApi.registerEntityTag(
								name,
								getAspects(entry),
								new ThaumcraftApi.EntityTagsNBT("pokedexNb", Integer.valueOf(entry.getPokedexNb()))
								);
				} else {
					Integer nb = entry.getPokedexNb();
					System.out.println("Error: name for pokemon with nb " + nb.toString() + " is null");
				}
			} else {
				Integer nb = entry.getPokedexNb();
				System.out.println("Pokemob " + nb.toString() + " is genericMob");
				
				ThaumcraftApi.registerEntityTag(
						"pokecube.pokecube:genericMob",
						getAspects(entry),
						new ThaumcraftApi.EntityTagsNBT("pokedexNb", Integer.valueOf(entry.getPokedexNb()))
						);
			}
		}
	}
//	//public static void addRecipes(){
//			static InfusionRecipe RecipeThaumiumPokecube(){
//				return ThaumcraftApi.addInfusionCraftingRecipe("THAUMIUMPOKECUBE",
//				new ItemStack(PokecubeItems.getEmptyCube(98)), 1, (new AspectList())
//				.add(Aspect.METAL, 8).add(Aspect.TRAP, 8).add(Aspect.MAGIC, 4),
//				new ItemStack(PokecubeItems.getEmptyCube(0)),
//				new ItemStack[] {ItemApi.getItem("itemResource", 2),
//				ItemApi.getItem("itemResource", 2),
//				ItemApi.getItem("itemResource", 2)});
//			}
//			
//			
//			
//			static ItemStack Infuse(){
//				Aspect aspect = Aspect.AIR;
//				String aspectString = aspect.toString();
//				NBTTagCompound tag;
//				tag = new NBTTagCompound();
//				tag.setString("Aspects", aspectString);
//				ItemStack item;
//				item = new ItemStack(PokecubeItems.getEmptyCube(98));
//				item.setTagCompound(tag);
//				
//				return item;
//				
//			}
//			
//			static InfusionRecipe InfusedThaumiumPokecube(){
//				
//				Aspect aspect = Aspect.AIR;
//				
//							return ThaumcraftApi.addInfusionCraftingRecipe("INFUSEDTHAUMIUMPOKECUBE",
//				Infuse(), 0, (new AspectList()).add(Aspect.TRAP, 4).add(aspect, 32),
//				new ItemStack(PokecubeItems.getEmptyCube(98)), new ItemStack[] {ItemApi.getItem("itemResource", 2)});
//			}
//	
//			
//		
//		

			
//	public static void addPage(){
//		ResearchCategories.registerCategory("POKECUBE", new ResourceLocation("pokecube", "textures/items/thaumiumpokecubefront.png"), new ResourceLocation("pokecube", "textures/items/pokedex.png"));
//	}
//	public static void addResearch(){
//		new ResearchItem("THAUMIUMPOKECUBE", "POKECUBE", (new AspectList())
//				.add(Aspect.METAL, 3).add(Aspect.MAGIC, 2).add(Aspect.TRAP, 2), 0, 3, 2, new 
//				ItemStack(PokecubeItems.getEmptyCube(98)))
//				.setPages(new ResearchPage[] {new ResearchPage ("tc.research_page.THAUMIUMPOKECUBE.1"),
//				new ResearchPage(RecipeThaumiumPokecube()),
//				new ResearchPage(InfusedThaumiumPokecube())
//				//(new ItemStack(PokecubeItems.getEmptyCube(98))))
//						}).registerResearchItem();
//	}
}

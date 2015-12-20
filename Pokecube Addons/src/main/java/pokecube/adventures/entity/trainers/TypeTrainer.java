package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;

public class TypeTrainer 
{
	public final String name;

    public static HashMap<String, TypeTrainer> typeMap = new HashMap<String, TypeTrainer>();
    public static HashMap<String, ArrayList<TypeTrainer>> biomes = new HashMap<String, ArrayList<TypeTrainer>>();

    public static ArrayList<String> maleNames = new ArrayList<String>();
    public static ArrayList<String> femaleNames = new ArrayList<String>();
    
	public boolean surfaceOnly = true;
	/**
	 * 1 = male, 2 = female, 3 = both
	 */
	public byte genders = 1;
	public Material material = Material.air;
	private ResourceLocation texture;
	private ResourceLocation femaleTexture;
	public List<PokedexEntry> pokemon = new ArrayList<PokedexEntry>();
	private ItemStack[] loot = new ItemStack[4];
	public String drops = "";
	
	public TypeTrainer(String name, boolean cave, Material material)
	{
		this(name, cave);
		this.material = material;
	}
	
	public TypeTrainer(String name, boolean cave)
	{
		this(name);
		surfaceOnly = !cave;
	}
	
	public ResourceLocation getTexture(EntityTrainer trainer)
	{
		if(texture==null && (genders ==1 || genders == 2))
		{
			try {
				texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+name+".png");
			} catch (Exception e) {
			}

			if(genders==2&&texture==null)
			{
				texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+"female.png");
			}
			if(genders==1&&texture==null)
			{
				texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+"male.png");
			}
		}
		else if(genders == 3)
		{
			
			if(femaleTexture == null)
			{
				femaleTexture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+name+"female.png");
			}
			if(texture==null)
			{
				texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+name+".png");
			}
			if(femaleTexture==null)
			{
				femaleTexture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+"female.png");
			}
			if(texture==null)
			{
				texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+"male.png");
			}
			return trainer.male?texture:femaleTexture;
		}
		return texture;
	}
	
	public TypeTrainer(String name)
	{
		this.name = name;
		typeMap.put(name, this);
	}
	
	public void initTrainerItems(EntityTrainer trainer)
	{
		initLoot();
		for(int i = 1; i<5; i++)
		{
			trainer.setCurrentItemOrArmor(i, loot[i-1]);
		}
	}
	
	private void initLoot()
	{
		if(loot[0]!=null)return;
		
		if(!drops.equals(""))
		{
			String[] args = drops.split(":");
			int num = 0;
			for(String s: args)
			{
				if(s==null) continue;
				String[] stackinfo = s.split("`");
				ItemStack stack = PokecubeItems.getStack(stackinfo[0]);
				if(stackinfo.length>1)
				{
					try {
						int count = Integer.parseInt(stackinfo[1]);
						stack.stackSize = count;
					} catch (NumberFormatException e) {
					}
				}
				if(stackinfo.length>2)
				{
					try {
						int count = Integer.parseInt(stackinfo[2]);
						stack.setItemDamage(count);
					} catch (NumberFormatException e) {
					}
				}
				loot[num] = stack;
				num++;
			}
		}
		if(loot[0]==null)
			loot[0] = new ItemStack(Items.emerald);
	}
	
	@Override
	public String toString()
	{
		return ""+name+" "+pokemon;
	}
	
	public boolean validMaterial(Material m)
	{
		if(this.material == Material.water)
			return m == Material.water;
		if(this.material == Material.air)
		{
			return m==material||!m.isLiquid()&&!m.isSolid()&&m.isReplaceable();
		}
			
		return false;
	}
	
	public static void postInitTrainers()
	{
		List<TypeTrainer> toRemove = new ArrayList<TypeTrainer>();
		for(TypeTrainer t: typeMap.values())
		{
			if(t.pokemon.size()==0)
			{
				toRemove.add(t);
			}
		}
		for(TypeTrainer t: toRemove)
		{
			typeMap.remove(t.name);
			List<String> blank = new ArrayList<String>();
			for(String b: biomes.keySet())
			{
				ArrayList<TypeTrainer> trainers = biomes.get(b);
				if(trainers.contains(t))
					trainers.remove(t);
				if(trainers.size()==0)
					blank.add(b);
			}
			for(String b: blank)
				biomes.remove(b);
		}
		//System.out.println(typeMap);
	}
	
	public static void addTrainer(String name, TypeTrainer type)
	{
		typeMap.put(name, type);
	}
	
	public static TypeTrainer getTrainer(String name)
	{
		TypeTrainer ret = typeMap.get(name);
		if(ret==null)
			for(TypeTrainer t: typeMap.values())
			{
				if(t!=null)
					return t;
			}
		return ret;
	}
	
	public static void getRandomTeam(EntityTrainer trainer, int maxXp, ItemStack[] team, World world)
	{
		TypeTrainer type = trainer.getType();
		
		for(int i = 0; i<6; i++)
			team[i] = null;
			
		int level = Tools.xpToLevel(0, maxXp);
		if(level==0) level = 4;
		int number = 1+new Random().nextInt(7);
		number = Math.min(number, 6);

		for(int i = 0; i<number; i++)
		{
			Collections.shuffle(type.pokemon);
			ItemStack item = null;
			for(PokedexEntry s: type.pokemon)
			{
				if(s!=null)
				{
					item = makeStack(s, trainer, world, level + new Random().nextInt(5));
				}
				if(item!=null)
					break;
			}
			team[i] = item;
		}
	}

    private static ItemStack makeStack(PokedexEntry entry, EntityLivingBase trainer, World world, int level)
    {
    	int num = entry.getPokedexNb();
    	if(Pokedex.getInstance().getEntry(num) == null)
    		return null;
    	
        IPokemob entity = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(entry.getPokedexNb(), world);
        if (entity != null)
        {
            for(int i = 1; i<level; i++)
            {
            	if(entity.getPokedexEntry().canEvolve(i))
            	{
            		for(EvolutionData d: entity.getPokedexEntry().getEvolutions())
            		{
            			if(d.shouldEvolve(entity))
            			{
                			Entity temp = d.getEvolution(world);
	            			if(temp!=null)
	            			{
	            				entity = (IPokemob) temp;
	            				break;
	            			}
            			}
            		}
            	}
            }
            ((EntityLivingBase) entity).setHealth(((EntityLivingBase) entity).getMaxHealth());

            entity.setPokemonOwner(trainer);
            entity.setPokecubeId(0);
            entity.setExp(Tools.levelToXp(entity.getExperienceMode(), level), true, false);
            ItemStack item = PokecubeManager.pokemobToItem(entity);
            
            ((Entity) entity).isDead = true;
            return item;
        }

        return null;
    }
    
    public static void addTrainerSpawn(String biome, TypeTrainer type)
    {
    	if(!biome.equalsIgnoreCase("all"))
			if(biomes.containsKey(biome))
			{
				biomes.get(biome).add(type);
			}
			else
			{
				ArrayList<TypeTrainer> trainers = new ArrayList<TypeTrainer>();
				trainers.add(type);
				biomes.put(biome, trainers);
			}
    	else
    		for(BiomeGenBase b: BiomeGenBase.getBiomeGenArray())
    		{
    			if(b==null)continue;
				if(biomes.containsKey(b.biomeName))
				{
					biomes.get(b.biomeName).add(type);
				}
				else
				{
					ArrayList<TypeTrainer> trainers = new ArrayList<TypeTrainer>();
					trainers.add(type);
					biomes.put(b.biomeName, trainers);
				}
    		}
    }
}

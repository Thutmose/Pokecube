package pokecube.compat.mfr;

public class Grindables 
{
//	static void registerGrindables(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//	{
//		for(Integer i: Pokedex.getInstance().getEntries())
//		{
//			PokedexEntry e = Pokedex.getInstance().getEntry(i);
//			register.invoke(registry, new GrindablePokemob(e));
//		}
//	}
//	
//	static void registerSpawnHandler(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//	{
//		for(Integer i: Pokedex.getInstance().getEntries())
//		{
//			PokedexEntry e = Pokedex.getInstance().getEntry(i);
//			register.invoke(registry, new FactorySpawnHandler(e));
//		}
//	}
//	
//	static void registerSpawnCosts(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//	{
//		int min = 400;
//		for(Integer i: Pokedex.getInstance().getEntries())
//		{
//			PokedexEntry e = Pokedex.getInstance().getEntry(i);
//			min = Math.min(min, e.getBaseXP());
//		}
//		for(Integer i: Pokedex.getInstance().getEntries())
//		{
//			PokedexEntry e = Pokedex.getInstance().getEntry(i);
//			int r = e.getBaseXP() - min;
//			Class pokeclass = PokecubeMod.core.getEntityClassFromPokedexNumber(e.getPokedexNb());
//			String id = (String) EntityList.classToStringMapping.get(pokeclass);
//			register.invoke(registry, id, r);
//		}
//	}
//	
//	static class FactorySpawnHandler implements IMobSpawnHandler
//	{
//		Class pokeclass;
//		public FactorySpawnHandler(PokedexEntry entry) {
//			pokeclass = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
//		}
//		
//		@Override
//		public Class<? extends EntityLivingBase> getMobClass() {
//			return pokeclass;
//		}
//
//		@Override
//		public void onMobSpawn(EntityLivingBase entity) {
//			IPokemob pokemob = ((IPokemob)entity);
//			pokemob.specificSpawnInit();
//    		pokemob.setExp(new Random().nextInt(8000), false, true);
//		}
//
//		@Override
//		public void onMobExactSpawn(EntityLivingBase entity) {
//			IPokemob pokemob = ((IPokemob)entity);
//			if(pokemob.getPokemonAIState(IPokemob.TAMED))
//			{
//				pokemob.setPokemonAIState(IPokemob.TAMED, false);
//				pokemob.setPokemonOwner(null);
//			}
//		}
//		
//	}
//	
//	static class GrindablePokemob implements IFactoryGrindable
//	{
//		private Class<? extends EntityLivingBase> _grindableClass;
//		private List<MobDrop> _drops = new ArrayList();
//		private boolean _entityProcessed;
//		private PokedexEntry entry;
//
//		public GrindablePokemob(PokedexEntry entry)
//		{
//			this.entry = entry;
//			this._grindableClass = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
//		}
//
//		@Override
//		public Class<? extends EntityLivingBase> getGrindableEntity()
//		{
//			return _grindableClass;
//		}
//
//		@Override
//		public List<MobDrop> grind(World world, EntityLivingBase entity, Random random)
//		{
//			_entityProcessed = true;
//			_drops.clear();
//			ItemStack food = entry.getFoodDrop(0);
//			ItemStack common = entry.getRandomCommonDrop(0);
//			ItemStack rare = entry.getRandomRareDrop(0);
//			if(food!=null)
//				_drops.add(new MobDrop(10, food));
//			if(common!=null)
//				_drops.add(new MobDrop(10, common));
//			if(rare!=null)
//				_drops.add(new MobDrop(10, rare));
//			
//			
//			return _drops;
//		}
//
//		@Override
//		public boolean processEntity(EntityLivingBase entity)
//		{
//			_entityProcessed = true;
//			
//			return _entityProcessed;
//		}
//	}
}

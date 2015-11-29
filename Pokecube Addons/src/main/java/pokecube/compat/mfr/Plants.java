package pokecube.compat.mfr;

public class Plants {
//
//	public static void registerFruits(Object registry, Method register) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//	{
//		for(Integer i: BerryManager.berryNames.keySet())
//		{
//			IFactoryFruit fruit = getFruit(i);
//			if(fruit!=null)
//				register.invoke(registry, fruit);
//		}
//	}
//	
//	
//	
//	private static IFactoryFruit getFruit(int berryId)
//	{
//		final Block fruit = BerryManager.berryFruits.get(berryId);
//
//		if(fruit != null)
//		{
//			final List<ItemStack> berries = new ArrayList<ItemStack>();
//			ItemStack berry = BerryManager.getBerryItem(BerryManager.berryNames.get(berryId));
//			berries.add(berry);
//			
//			IFactoryFruit ret = new IFactoryFruit() {
//				
//				Block block = fruit;
//				List<ItemStack> drops = berries;
//				
//				@Override
//				public void prePick(World world, int x, int y, int z) {
//					
//				}
//				
//				@Override
//				public void postPick(World world, int x, int y, int z) {
//					
//				}
//				
//				@Override
//				public ReplacementBlock getReplacementBlock(World world, int x, int y, int z) {
//					return new ReplacementBlock(Blocks.air);
//				}
//				
//				@Override
//				public Block getPlant() {
//					return fruit;
//				}
//				
//				@Override
//				public List<ItemStack> getDrops(World world, Random rand, int x, int y,
//						int z) {
//					drops.get(0).stackSize = fruit.quantityDropped(rand);
//					return drops;
//				}
//				
//				@Override
//				public boolean canBePicked(World world, int x, int y, int z) {
//					return true;
//				}
//				
//				@Override
//				public boolean breakBlock() {
//					return true;
//				}
//			};
//			
//			return ret;
//		}
//		return null;
//	}

}

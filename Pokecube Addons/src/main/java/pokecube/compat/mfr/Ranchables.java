package pokecube.compat.mfr;

public class Ranchables {
//
//	public static IFactoryRanchable getMareep()
//	{
//		 IFactoryRanchable mareep = new IFactoryRanchable() {
//				
//				@Override
//				public List<RanchedItem> ranch(World world, EntityLivingBase entity,
//						IInventory rancher) {
//					ArrayList<RanchedItem> ret = new ArrayList();
//					if(entity instanceof IPokemob && entity instanceof IShearable && ((IShearable)entity).isShearable(new ItemStack(Items.shears), world, (int)(entity.posX), (int)(entity.posZ), (int)(entity.posY)))
//					{
//						List<ItemStack> items = ((IShearable)entity).onSheared(new ItemStack(Items.shears), world, (int)(entity.posX), (int)(entity.posZ), (int)(entity.posY), 0);
//						
//						if(items!=null)
//						{
//							for(ItemStack i: items)
//							{
//								if(i!=null)
//								{
//									ret.add(new RanchedItem(i));
//								}
//							}
//						}
//					}
//					return ret;
//				}
//				
//				@Override
//				public Class<? extends EntityLivingBase> getRanchableEntity() {
//					
//					Class ret = PokecubeMod.core.getEntityClassFromPokedexNumber(Database.getEntry("mareep").getPokedexNb());
//						
//					if(ret==null)
//					{
//						System.out.println("Mareep class is null");
//					}
//					return ret;
//				}
//			};
//		
//		return mareep;
//	}
//	
//	public static IFactoryRanchable getMiltank()
//	{
//		IFactoryRanchable miltank = new IFactoryRanchable() {
//			
//			@Override
//			public List<RanchedItem> ranch(World world, EntityLivingBase entity,
//					IInventory rancher) {
//				NBTTagCompound tag = entity.getEntityData();
//				if (tag.getLong("mfr:lastRanched") > world.getTotalWorldTime())
//					return null;
//				tag.setLong("mfr:lastRanched", world.getTotalWorldTime() + 20 * 5);
//				
//				List<RanchedItem> drops = new LinkedList<RanchedItem>();
//				
//				int bucketIndex = -1;
//				
//				for(int i = 0; i< rancher.getSizeInventory(); i++)
//				{
//					if(rancher.getStackInSlot(i)!=null && rancher.getStackInSlot(i).isItemEqual(new ItemStack(Items.bucket)))
//					{
//						bucketIndex = i;
//						break;
//					}
//				}
//				
//				if(bucketIndex >= 0)
//				{
//					drops.add(new RanchedItem(Items.milk_bucket));
//					rancher.decrStackSize(bucketIndex, 1);
//				}
//				else
//				{
//					FluidStack milk = FluidRegistry.getFluidStack("milk", FluidContainerRegistry.BUCKET_VOLUME);
//					drops.add(new RanchedItem(milk));
//				}
//				
//				return drops;
//			}
//			
//			@Override
//			public Class<? extends EntityLivingBase> getRanchableEntity() {
//				
//				Class ret = PokecubeMod.core.getEntityClassFromPokedexNumber(Database.getEntry("miltank").getPokedexNb());
//					
//				if(ret==null)
//				{
//					System.out.println("miltank class is null");
//				}
//				return ret;
//			}
//		};
//		
//		return miltank;
//	}
//	
//	public static IFactoryRanchable makeRanchable(final PokedexEntry entry_,final ItemStack stack_, final FluidStack fluid_, final int delay_)
//	{
//		
//		System.out.println(entry_+" "+stack_+" "+fluid_+" "+delay_);
//		
//		IFactoryRanchable ranch = new IFactoryRanchable() {
//			
//			final int delay = delay_;
//			final PokedexEntry entry = entry_;
//			final FluidStack fluid = fluid_;
//			final ItemStack stack = stack_;
//			
//			@Override
//			public List<RanchedItem> ranch(World world, EntityLivingBase entity, IInventory rancher)
//			{
//				NBTTagCompound tag = entity.getEntityData();
//				if (tag.getLong("mfr:lastRanched") > world.getTotalWorldTime())
//					return null;
//				
//				IPokemob pokemob = (IPokemob) entity;
//				int level = pokemob.getLevel();
//				int effDelay = (delay * ((110 - level)))/100;
//				
//				tag.setLong("mfr:lastRanched", world.getTotalWorldTime() + effDelay);
//				
//				List<RanchedItem> drops = new ArrayList<RanchedItem>();
//				
//				if(fluid!=null)
//				{
//					drops.add(new RanchedItem(fluid));
//				}
//				if(stack!=null)
//				{
//					drops.add(new RanchedItem(stack));
//				}
//				return drops;
//			}
//			
//			@Override
//			public Class<? extends EntityLivingBase> getRanchableEntity() {
//				Class ret = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
//				return ret;
//			}
//		};
//		return ranch;
//	}
}

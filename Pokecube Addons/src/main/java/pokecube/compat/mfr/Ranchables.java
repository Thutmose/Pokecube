package pokecube.compat.mfr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import powercrystals.minefactoryreloaded.api.IFactoryRanchable;
import powercrystals.minefactoryreloaded.api.RanchedItem;

public class Ranchables
{

    private static Field interactionLogic;
    private static Field stacks;

    static
    {
        try
        {
            interactionLogic = PokedexEntry.class.getDeclaredField("interactionLogic");
            interactionLogic.setAccessible(true);
            stacks = InteractionLogic.class.getDeclaredField("stacks");
            stacks.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    public static IFactoryRanchable getMareep()
    {
        IFactoryRanchable mareep = new IFactoryRanchable()
        {

            @Override
            public List<RanchedItem> ranch(World world, EntityLivingBase entity, IInventory rancher)
            {
                ArrayList<RanchedItem> ret = new ArrayList();
                if (entity instanceof IPokemob && entity instanceof IShearable
                        && ((IShearable) entity).isShearable(new ItemStack(Items.SHEARS), world, entity.getPosition()))
                {
                    List<ItemStack> items = ((IShearable) entity).onSheared(new ItemStack(Items.SHEARS), world,
                            entity.getPosition(), 0);

                    if (items != null)
                    {
                        for (ItemStack i : items)
                        {
                            if (i != null)
                            {
                                ret.add(new RanchedItem(i));
                            }
                        }
                    }
                }
                return ret;
            }

            @Override
            public Class<? extends EntityLivingBase> getRanchableEntity()
            {

                Class ret = PokecubeMod.core
                        .getEntityClassFromPokedexNumber(Database.getEntry("mareep").getPokedexNb());

                if (ret == null)
                {
                    System.out.println("Mareep class is null");
                }
                return ret;
            }
        };

        return mareep;
    }

    public static IFactoryRanchable getMiltank()
    {
        IFactoryRanchable miltank = new IFactoryRanchable()
        {

            @Override
            public List<RanchedItem> ranch(World world, EntityLivingBase entity, IInventory rancher)
            {
                NBTTagCompound tag = entity.getEntityData();
                if (tag.getLong("mfr:lastRanched") > world.getTotalWorldTime()) return null;
                tag.setLong("mfr:lastRanched", world.getTotalWorldTime() + 20 * 5);

                List<RanchedItem> drops = new LinkedList<RanchedItem>();

                int bucketIndex = -1;

                for (int i = 0; i < rancher.getSizeInventory(); i++)
                {
                    if (rancher.getStackInSlot(i) != null
                            && rancher.getStackInSlot(i).isItemEqual(new ItemStack(Items.BUCKET)))
                    {
                        bucketIndex = i;
                        break;
                    }
                }

                if (bucketIndex >= 0)
                {
                    drops.add(new RanchedItem(Items.MILK_BUCKET));
                    rancher.decrStackSize(bucketIndex, 1);
                }
                else
                {
                    FluidStack milk = FluidRegistry.getFluidStack("milk", FluidContainerRegistry.BUCKET_VOLUME);
                    drops.add(new RanchedItem(milk));
                }

                return drops;
            }

            @Override
            public Class<? extends EntityLivingBase> getRanchableEntity()
            {

                Class ret = PokecubeMod.core
                        .getEntityClassFromPokedexNumber(Database.getEntry("miltank").getPokedexNb());

                if (ret == null)
                {
                    System.out.println("miltank class is null");
                }
                return ret;
            }
        };

        return miltank;
    }

    public static IFactoryRanchable makeRanchable(final PokedexEntry entry_,
            final HashMap<ItemStack, List<ItemStack>> keys_)
    {
        IFactoryRanchable ranch = new IFactoryRanchable()
        {
            final PokedexEntry                        entry       = entry_;
            final HashMap<ItemStack, List<ItemStack>> logicStacks = keys_;

            @Override
            public List<RanchedItem> ranch(World world, EntityLivingBase entity, IInventory rancher)
            {
                NBTTagCompound tag = entity.getEntityData();
                List<RanchedItem> drops = new ArrayList<RanchedItem>();
                if (tag.hasKey("lastInteract"))
                {
                    long time = tag.getLong("lastInteract");
                    long diff = entity.worldObj.getTotalWorldTime() - time;
                    if (diff < 100) { return drops; }
                }
                tag.setLong("lastInteract", entity.worldObj.getTotalWorldTime());

                for (ItemStack key : logicStacks.keySet())
                {
                    try
                    {
                        if (FluidContainerRegistry.isBucket(key) || FluidContainerRegistry.isContainer(key))
                        {
                            for (ItemStack stack : logicStacks.get(key))
                            {
                                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                                if (fluid != null) drops.add(new RanchedItem(fluid));
                                else drops.add(new RanchedItem(stack));
                            }
                        }
                        else for (ItemStack stack : logicStacks.get(key))
                        {
                            drops.add(new RanchedItem(stack));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                return drops;
            }

            @Override
            public Class<? extends EntityLivingBase> getRanchableEntity()
            {
                Class ret = PokecubeMod.core.getEntityClassFromPokedexNumber(entry.getPokedexNb());
                return ret;
            }
        };
        return ranch;
    }

    public static void registerRanchables(Object registry, Method register)
            throws IllegalArgumentException, IllegalAccessException
    {
        for (PokedexEntry entry : Database.allFormes)
        {
            InteractionLogic logic = (InteractionLogic) interactionLogic.get(entry);
            HashMap<ItemStack, List<ItemStack>> logicStacks = (HashMap<ItemStack, List<ItemStack>>) stacks.get(logic);
        }
    }
}

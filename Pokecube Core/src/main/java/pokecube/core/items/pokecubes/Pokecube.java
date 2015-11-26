package pokecube.core.items.pokecubes;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTranslated;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class Pokecube extends ItemTranslated implements IPokecube
{

    public Pokecube()
    {
        super();
        this.setHasSubtypes(false);
        setMaxDamage(PokecubeMod.MAX_DAMAGE);
    }

    @Override
    /** Called whenever this item is equipped and the right mouse button is
     * pressed. Args: itemStack, world, entityPlayer */
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        throwPokecube(par2World, par3EntityPlayer, par1ItemStack, null, null);
        return par1ItemStack;
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public double getCaptureModifier(IPokemob mob, int id)
    {
        if (id == 1) return 1.5d;
        if (id == 2) return 2d;
        if (id == 3) return 255d;
        if (id == 0) return 1;
        if (id == 5) return dusk(mob, id);
        if (id == 6) return quick(mob, id);
        if (id == 7) return timer(mob, id);
        if (id == 8) return net(mob, id);
        if (id == 9) return nest(mob, id);
        if (id == 10) return dive(mob, id);
        if (id == 12) return 1d;
        if (id == 13) return 1d;
        if (id == 14) return 0d;

        return 0;
    }

    public double dusk(IPokemob mob, int id)
    {
        double x = 1;
        Entity entity = (Entity) mob;
        int light = entity.worldObj.getLight(entity.getPosition());
        if (light < 5)
        {
            x = 3.5;
        }
        return x;
    }

    public double quick(IPokemob mob, int id)
    {
        double x = 1;
        Entity entity = (Entity) mob;
        double alive = entity.ticksExisted;
        if (mob.getPokemonAIState(IPokemob.ANGRY) == false && alive < 601)
        {
            x = 4;
        }
        return x;
    }

    public double timer(IPokemob mob, int id)
    {
        double x = 1;
        Entity entity = (Entity) mob;
        double alive = entity.ticksExisted;
        if (alive > 1500 && alive < 3001)
        {
            x = 2;
        }
        if (alive > 3000 && alive < 4501)
        {
            x = 3;
        }
        if (alive > 4500)
        {
            x = 4;
        }
        return x;
    }

    public double net(IPokemob mob, int id)
    {
        double x = 1;
        if (mob.getType1() == PokeType.bug)
        {
            x = 2;
        }
        if (mob.getType1() == PokeType.water)
        {
            x = 2;
        }
        if (mob.getType2() == PokeType.bug)
        {
            x = 2;
        }
        if (mob.getType2() == PokeType.water)
        {
            x = 2;
        }
        return x;
    }

    public double nest(IPokemob mob, int id)
    {
        double x = 1;
        if (mob.getLevel() < 20)
        {
            x = 3;
        }
        if (mob.getLevel() > 19 && mob.getLevel() < 30)
        {
            x = 2;
        }
        return x;
    }

    public double dive(IPokemob mob, int id)
    {
        double x = 1;
        Entity entity = (Entity) mob;
        if (entity.worldObj.getBlockState(entity.getPosition()).getBlock() == Blocks.water
                && mob.getType1() == PokeType.water)
        {
            x = 3.5;
        }
        if (entity.worldObj.getBlockState(entity.getPosition()).getBlock() == Blocks.water
                && mob.getType2() == PokeType.water)
        {
            x = 3.5;
        }
        return x;
    }

    // Pokeseal stuff

    /** allows items to add custom lines of information to the mouseover
     * description */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean boo)
    {
        if (item.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = PokecubeManager.getSealTag(item);

            displayInformation(nbttagcompound, list);

        }
    }

    @SideOnly(Side.CLIENT)
    public static void displayInformation(NBTTagCompound nbt, List<String> list)
    {
        boolean flag2 = nbt.getBoolean("Flames");

        if (flag2)
        {
            list.add(StatCollector.translateToLocal("item.pokecube.flames"));
        }

        boolean flag3 = nbt.getBoolean("Bubbles");

        if (flag3)
        {
            list.add(StatCollector.translateToLocal("item.pokecube.bubbles"));
        }

        boolean flag4 = nbt.getBoolean("Leaves");

        if (flag4)
        {
            list.add(StatCollector.translateToLocal("item.pokecube.leaves"));
        }

        boolean flag5 = nbt.hasKey("dye");

        if (flag5)
        {
            // list.add(StatCollector.translateToLocal(ItemDye.field_150921_b[nbt.getInteger("dye")]));//TODO
            // dye names in pokeseals
        }
    }
    
    @Override
    /**
     * This is called when the item is used, before the block is activated.
     * @param stack The Item Stack
     * @param player The Player that used the item
     * @param world The Current World
     * @param pos Target position
     * @param side The side of the target hit
     * @return Return true to prevent any further processing.
     */
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean throwPokecube(World world, EntityPlayer player, ItemStack cube, Vector3 targetLocation,
            Entity target)
    {
        pokecube.core.items.pokecubes.EntityPokecube entity = null;
        int id = PokecubeItems.getCubeId(cube.getItem());
        if (id < 0) return false;
        ItemStack stack = ItemStack.copyItemStack(cube);
        stack.stackSize = 1;
        entity = new pokecube.core.items.pokecubes.EntityPokecube(world, player, stack);
        
        if (target instanceof EntityLivingBase || PokecubeManager.isFilled(cube) || player.isSneaking())
        {
            entity.targetEntity = (EntityLivingBase) target;
            
            if(player.isSneaking())
            {
                Vector3 temp = Vector3.getNewVectorFromPool().set(player).add(0, player.getEyeHeight(), 0);
                Vector3 temp1 = Vector3.getNewVectorFromPool().set(player.getLookVec()).scalarMultBy(1.5);
                
                temp.addTo(temp1).moveEntity(entity);
                temp.clear().setVelocities(entity);
                entity.targetEntity = null;
            }
            
            if (!world.isRemote)
            {
                world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                world.spawnEntityInWorld(entity);
            }
        }
        else
        {
            player.addChatMessage(new ChatComponentText("I should probably aim more carefully"));
            return false;
        }

        if (!cube.hasTagCompound()) return true;
        cube.getTagCompound().setBoolean("delete", true);
        // itemstack = new ItemStack(this);
        int current = player.inventory.currentItem;
        player.inventory.mainInventory[current] = null;
        player.inventory.markDirty();

        return true;
    }
}

package pokecube.adventures.blocks.legendary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.LegendaryConditions;
import pokecube.core.blocks.berries.IMetaBlock;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class BlockLegendSpawner extends Block implements IMetaBlock
{
    public PropertyInteger   TYPE;
    public ArrayList<String> types = new ArrayList<String>();

    public BlockLegendSpawner()
    {
        super(Material.rock);
        this.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        this.setHardness(10);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, Integer.valueOf(0)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        if (TYPE == null)
        {
            if (LegendaryConditions.spawner1 == null)
                TYPE = PropertyInteger.create("type", 0, LegendaryConditions.SPAWNER1COUNT - 1);
        }
        return new BlockStateContainer(this, new IProperty[] { TYPE });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).intValue();
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, Integer.valueOf(meta));
    }

    @Override
    /** returns a list of blocks with the same ID, but different meta (eg: wood
     * returns 4 blocks) */
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (int j = 0; j < types.size(); ++j)
        {
            list.add(new ItemStack(itemIn, 1, j));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "tile." + types.get(stack.getItemDamage());
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) { return true; }

        int meta = getMetaFromState(state);

        PokedexEntry entry = Database.getEntry(types.get(meta));
        if (entry == null) return false;

        int pokedexNb = entry.getPokedexNb();
        ISpecialSpawnCondition condition = ISpecialSpawnCondition.spawnMap.get(pokedexNb);
        if (condition != null)
        {
            Vector3 location = Vector3.getNewVector().set(pos);
            if (condition.canSpawn(playerIn, location))
            {
                EntityLiving entity = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(pokedexNb, worldIn);
                entity.setHealth(entity.getMaxHealth());
                location.add(0, 1, 0).moveEntity(entity);
                condition.onSpawn((IPokemob) entity);

                if (((IPokemob) entity).getExp() < 100)
                {
                    ((IPokemob) entity).setExp(6000, true, true);
                }
                worldIn.spawnEntityInWorld(entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    public void registerType(String pokemon)
    {
        if (types.size() > 15)
            throw new ArrayIndexOutOfBoundsException("Cannot add more legends to this block, please make another");
        types.add(pokemon);
    }
}

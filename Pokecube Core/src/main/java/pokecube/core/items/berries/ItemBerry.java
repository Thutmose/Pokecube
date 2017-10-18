package pokecube.core.items.berries;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;

/** @author Oracion
 * @author Manchou */
public class ItemBerry extends Item implements IMoveConstants, IPlantable
{
    public ItemBerry()
    {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced)
    {
        String info = "";
        tooltip.add(I18n.format("item.berry.desc"));
        String berryName = BerryManager.berryNames.get(stack.getItemDamage());
        info = I18n.format("item." + berryName + "Berry.desc");
        if (info.startsWith("Format error: ")) info = info.replaceFirst("Format error: ", "");
        tooltip.add(info);
        if (TileEntityBerries.trees.containsKey(stack.getItemDamage()))
        {
            info = I18n.format("item.berry.istree.desc");
            tooltip.add(info);
        }
        if (PokecubeCore.getPlayer(null) == null) return;
        if (PokecubeCore.getPlayer(null).openContainer instanceof ContainerPokemob)
        {
            ContainerPokemob container = (ContainerPokemob) PokecubeCore.getPlayer(null).openContainer;
            IPokemob pokemob = container.getPokemob();
            Nature nature = pokemob.getNature();
            int fav = Nature.getFavouriteBerryIndex(nature);
            if (fav == stack.getItemDamage())
            {
                info = I18n.format("item.berry.favourite.desc", pokemob.getPokemonDisplayName().getFormattedText());
                tooltip.add(info);
            }
            int weight = Nature.getBerryWeight(stack.getItemDamage(), nature);
            if (weight == 0)
            {
                info = I18n.format("item.berry.nomind.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight >= 10)
            {
                info = I18n.format("item.berry.like1.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight >= 20)
            {
                info = I18n.format("item.berry.like2.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight >= 30)
            {
                info = I18n.format("item.berry.like3.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight <= -10)
            {
                info = I18n.format("item.berry.hate1.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight <= -20)
            {
                info = I18n.format("item.berry.hate2.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            if (weight <= -30)
            {
                info = I18n.format("item.berry.hate3.desc", pokemob.getPokemonDisplayName().getFormattedText());
            }
            tooltip.add(info);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (!this.isInCreativeTab(tab)) return;
        for (Integer i : BerryManager.berryNames.keySet())
        {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item." + BerryManager.berryNames.get(stack.getItemDamage()) + "Berry";
    }

    // 1.11
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int index = stack.getItemDamage();
        net.minecraft.block.state.IBlockState state = worldIn.getBlockState(pos);
        if (side == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(side), side, stack)
                && state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, (IPlantable) Items.WHEAT_SEEDS)
                && worldIn.isAirBlock(pos.up()))
        {
            worldIn.setBlockState(pos.up(), BerryManager.berryCrop.getDefaultState());
            TileEntityBerries tile = (TileEntityBerries) worldIn.getTileEntity(pos.up());
            tile.setBerryId(index);
            stack.splitStack(1);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos)
    {
        return EnumPlantType.Crop;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos)
    {
        return BerryManager.berryCrop.getDefaultState();
    }
}

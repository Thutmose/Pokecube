package pokecube.core.items.berries;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Nature;

/** @author Oracion
 * @author Manchou */
public class ItemBerry extends Item implements IMoveConstants, IPokemobUseable
{
    public ItemBerry()
    {
        super();
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        String info = "";
        list.add(I18n.format("item.berry.desc"));
        switch (stack.getItemDamage())
        {
        case 1:
            info = I18n.format("item.cheriBerry.desc");
            list.add(info);
            break;
        case 2:
            info = I18n.format("item.chestoBerry.desc");
            list.add(info);
            break;
        case 3:
            info = I18n.format("item.pechaBerry.desc");
            list.add(info);
            break;
        case 4:
            info = I18n.format("item.rawstBerry.desc");
            list.add(info);
            break;
        case 5:
            info = I18n.format("item.aspearBerry.desc");
            list.add(info);
            break;
        case 7:
            info = I18n.format("item.oranBerry.desc");
            list.add(info);
            break;
        case 9:
            info = I18n.format("item.lumBerry.desc");
            list.add(info);
            break;
        case 10:
            info = I18n.format("item.sitrusBerry.desc");
            list.add(info);
            break;
        case 63:
            info = I18n.format("item.jabocaBerry.desc");
            list.add(info);
            break;
        case 64:
            info = I18n.format("item.rowapBerry.desc");
            list.add(info);
            break;
        }
        if (TileEntityBerries.trees.containsKey(stack.getItemDamage()))
        {
            info = I18n.format("item.berry.istree.desc");
            list.add(info);
        }

        if (player.openContainer instanceof ContainerPokemob)
        {
            ContainerPokemob container = (ContainerPokemob) player.openContainer;
            IPokemob pokemob = container.getPokemob();
            Nature nature = pokemob.getNature();
            int fav = Nature.getFavouriteBerryIndex(nature);
            if (fav == stack.getItemDamage())
            {
                info = I18n.format("item.berry.favourite.desc", pokemob.getPokemonDisplayName().getFormattedText());
                list.add(info);
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
            list.add(info);
        }
    }

    @Override
    public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
    {
        boolean applied = BerryManager.berryEffect((IPokemob) mob, stack);
        int[] flavours = BerryManager.berryFlavours.get(stack.getItemDamage());
        if (applied && flavours != null)
        {
            IPokemob pokemob = (IPokemob) mob;
            for (int i = 0; i < 5; i++)
            {
                pokemob.setFlavourAmount(i, pokemob.getFlavourAmount(i) + flavours[i]);
            }
        }
        return applied;
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        for (Integer i : BerryManager.berryNames.keySet())
        {
            subItems.add(new ItemStack(itemIn, 1, i));
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

    @Override
    public boolean itemUse(ItemStack stack, Entity user, EntityPlayer player)
    {
        if (user instanceof EntityLivingBase)
        {
            EntityLivingBase mob = (EntityLivingBase) user;
            if (player != null) return useByPlayerOnPokemob(mob, stack);
            return useByPokemob(mob, stack);
        }

        return false;
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
    public boolean useByPlayerOnPokemob(EntityLivingBase mob, ItemStack stack)
    {
        if (stack.isItemEqual(BerryManager.getBerryItem("oran")))
        {
            float health = mob.getHealth();
            float maxHealth = mob.getMaxHealth();

            if (health == maxHealth || health <= 0) return false;

            if (health + 10 < maxHealth) mob.setHealth(health + 10);
            else mob.setHealth(maxHealth);
            HappinessType.applyHappiness((IPokemob) mob, HappinessType.BERRY);
            return true;
        }
        if (stack.isItemEqual(BerryManager.getBerryItem("sitrus")))
        {
            float health = mob.getHealth();
            float maxHealth = mob.getMaxHealth();

            if (health == maxHealth) return false;

            if (health + maxHealth / 4 < maxHealth) mob.setHealth(health + maxHealth / 4);
            else mob.setHealth(maxHealth);
            HappinessType.applyHappiness((IPokemob) mob, HappinessType.BERRY);
            return true;
        }
        if (stack.isItemEqual(BerryManager.getBerryItem("enigma")))
        {
            float health = mob.getHealth();
            float maxHealth = mob.getMaxHealth();

            if (health == maxHealth) return false;

            if (health >= maxHealth / 3) return false;
            if (health == 0) return false;

            if (health + maxHealth / 4 < maxHealth) mob.setHealth(health + maxHealth / 4);
            else mob.setHealth(maxHealth);
            HappinessType.applyHappiness((IPokemob) mob, HappinessType.BERRY);
            return true;
        }
        return applyEffect(mob, stack);
    }

    @Override
    public boolean useByPokemob(EntityLivingBase mob, ItemStack stack)
    {
        return applyEffect(mob, stack);
    }
}

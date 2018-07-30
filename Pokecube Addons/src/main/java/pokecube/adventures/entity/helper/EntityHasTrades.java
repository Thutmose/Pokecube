package pokecube.adventures.entity.helper;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.lib.CompatWrapper;

public abstract class EntityHasTrades extends EntityAgeable implements IMerchant, INpc, IEntityAdditionalSpawnData
{
    public static final int      VERSION       = 5;

    protected boolean            clear         = false;
    protected boolean            shouldrefresh = false;
    /** This villager's current customer. */
    protected EntityPlayer       buyingPlayer;
    /** Initialises the MerchantRecipeList.java */
    protected MerchantRecipeList tradeList;
    /** Initialises the MerchantRecipeList.java */
    protected MerchantRecipeList itemList;

    public EntityHasTrades(World worldIn)
    {
        super(worldIn);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        new PacketBuffer(buffer).writeCompoundTag(tag);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        try
        {
            NBTTagCompound tag = new PacketBuffer(additionalData).readCompoundTag();
            this.readFromNBT(tag);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setCustomer(EntityPlayer player)
    {
        tradeList = null;
        this.buyingPlayer = player;
    }

    @Override
    public EntityPlayer getCustomer()
    {
        return buyingPlayer;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player)
    {
        if (player.openContainer instanceof ContainerMerchant)
        {
            InventoryMerchant inv = ((ContainerMerchant) player.openContainer).getMerchantInventory();
            if (clear)
            {
                inv.removeStackFromSlot(0);
            }
            clear = false;
        }
        if (this.tradeList == null || shouldrefresh)
        {
            shouldrefresh = false;
            this.populateBuyingList(player);
        }
        return this.tradeList;
    }

    @Override
    public void setRecipes(MerchantRecipeList recipeList)
    {
        this.itemList = recipeList;
    }

    @Override
    public void useRecipe(MerchantRecipe recipe)
    {
        trade(recipe);
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        int i = 3 + this.rand.nextInt(4);
        if (recipe.getRewardsExp())
        {
            this.getEntityWorld()
                    .spawnEntity(new EntityXPOrb(this.getEntityWorld(), this.posX, this.posY + 0.5D, this.posZ, i));
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack)
    {
        if (!this.getEntityWorld().isRemote && this.livingSoundTime > -this.getTalkInterval() + 20)
        {
            this.livingSoundTime = -this.getTalkInterval();

            if (CompatWrapper.isValid(stack))
            {
                this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
            }
            else
            {
                this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
            }
        }
    }

    public abstract void populateBuyingList(EntityPlayer player);

    protected abstract void addRandomTrades();

    protected abstract void trade(MerchantRecipe recipe);

    protected void checkTradeIntegrity()
    {
        if (itemList == null) return;
        List<MerchantRecipe> toRemove = Lists.newArrayList();
        for (MerchantRecipe r : itemList)
        {
            if (!CompatWrapper.isValid(r.getItemToSell()))
            {
                shouldrefresh = true;
                toRemove.add(r);
                continue;
            }
            boolean hasBuy = CompatWrapper.isValid(r.getItemToBuy());
            hasBuy = hasBuy || CompatWrapper.isValid(r.getSecondItemToBuy());
            if (!hasBuy)
            {
                shouldrefresh = true;
                toRemove.add(r);
            }
        }
        itemList.removeAll(toRemove);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("version", VERSION);
        if (this.itemList != null)
        {
            checkTradeIntegrity();
            nbt.setTag("Offers", this.itemList.getRecipiesAsTags());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("Offers", 10) && nbt.getInteger("version") == VERSION)
        {
            NBTTagCompound nbttagcompound = nbt.getCompoundTag("Offers");
            this.itemList = new MerchantRecipeList(nbttagcompound);
        }
        checkTradeIntegrity();
    }

    public World getWorld()
    {
        return this.buyingPlayer.world;
    }

    public BlockPos getPos()
    {
        return new BlockPos(this.buyingPlayer);
    }
}

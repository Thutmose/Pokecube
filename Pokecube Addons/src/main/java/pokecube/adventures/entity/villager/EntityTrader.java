package pokecube.adventures.entity.villager;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.items.ItemTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public class EntityTrader extends EntityVillager
{
    EntityPlayer customer;

    MerchantRecipeList list  = new MerchantRecipeList();
    MerchantRecipeList list2 = new MerchantRecipeList();

    HashSet<BlockPos>                 chests = new HashSet<BlockPos>();
    HashMap<BlockPos, MerchantRecipe> map    = new HashMap<BlockPos, MerchantRecipe>();

    public String texture = "male";

    public EntityTrader(World world)
    {
        super(world, 1);
        this.setSize(0.6F, 1.8F);
        ItemStack shards = PokecubeItems.getStack("emerald_shard");
        shards.stackSize = 9;
        ItemStack emeralds = PokecubeItems.getStack("emerald");
        emeralds.stackSize = 30;
        ItemStack trade = PokecubeItems.getStack("exp_share");
        list.add(new MerchantRecipe(emeralds.copy(), trade.copy()));
        emeralds.stackSize = 1;
        trade = PokecubeItems.getStack("pokecube");
        trade.stackSize = 8;
        list.add(new MerchantRecipe(emeralds.copy(), trade.copy()));
        emeralds.stackSize = 8;
        trade = PokecubeItems.getStack("greatcube");
        trade.stackSize = 4;
        list.add(new MerchantRecipe(emeralds.copy(), trade.copy()));
        emeralds.stackSize = 16;
        trade = PokecubeItems.getStack("ultracube");
        trade.stackSize = 2;
        list.add(new MerchantRecipe(emeralds.copy(), trade.copy()));
        emeralds.stackSize = 64;
        trade = PokecubeItems.getStack("mastercube");
        trade.stackSize = 1;
        list.add(new MerchantRecipe(emeralds.copy(), trade.copy()));
        emeralds.stackSize = 4;
        for (String s: ItemVitamin.vitamins)
        {
            list.add(new MerchantRecipe(emeralds.copy(), PokecubeItems.getStack(s)));
        }
    }

    private void addRecipes(EntityPlayer p)
    {
        for (BlockPos c1 : chests)
        {
            Vector3 chest = Vector3.getNewVector().set(c1);
            TileEntity te = chest.getTileEntity(worldObj);
            if (te instanceof IInventory)
            {
                IInventory inv = (IInventory) te;
                if (inv.getSizeInventory() > 3)
                {
                    ItemStack a = inv.getStackInSlot(0);
                    ItemStack b = inv.getStackInSlot(1);
                    ItemStack c = inv.getStackInSlot(2);
                    if (c == null)
                    {
                        continue;
                    }

                    boolean has = false;
                    int count = 0;
                    for (int i = 3; i < inv.getSizeInventory(); i++)
                    {
                        if (inv.getStackInSlot(i) != null && c.isItemEqual(inv.getStackInSlot(i)))
                        {
                            count += inv.getStackInSlot(i).stackSize;
                        }
                        if (count >= c.stackSize)
                        {
                            has = true;
                            break;
                        }
                    }
                    if (!has)
                    {
                        continue;
                    }

                    MerchantRecipe rec = new MerchantRecipe(a, b, c);
                    map.put(c1, rec);
                    list2.add(rec);
                }
            }
        }

    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        Entity e = source.getSourceOfDamage();
        if (e == null || !(e instanceof EntityPlayer)) { return super.attackEntityFrom(source, i); }

        if ((e instanceof EntityPlayer && (((EntityPlayer) e).capabilities.isCreativeMode))
                || (((EntityPlayer) e).getHeldItemMainhand() != null
                        && (((EntityPlayer) e).getHeldItemMainhand().getItem() instanceof ItemTrainer)))
        {

            EntityPlayer p = (EntityPlayer) e;
            if (!p.capabilities.isCreativeMode)
            {
                ChunkCoordinate c = new ChunkCoordinate(MathHelper.floor_double(posX / 16f), (int) posY / 16,
                        MathHelper.floor_double(posZ / 16f), dimension);
                String owner = TeamManager.getInstance().getLandOwner(c);
                Vector3 v = Vector3.getNewVector().set(this);
                String owner1 = TeamManager.getInstance()
                        .getLandOwner(new ChunkCoordinate(MathHelper.floor_double(v.intX() / 16f), v.intY() / 16,
                                MathHelper.floor_double(v.intZ() / 16f), dimension));
                if (owner1 == null || !owner1.equals(owner)) { return false; }
                String team = worldObj.getScoreboard().getPlayersTeam(p.getName()).getRegisteredName();
                if (owner == null) return false;
                if (!owner.equals(team)
                        || !TeamManager.getInstance().isAdmin(p.getName(), p.getTeam())) { return false; }
            }
            this.setDead();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    @Override
    public EntityVillager createChild(EntityAgeable p_90011_1_)
    {
        return null;
    }

    @Override
    public EntityPlayer getCustomer()
    {
        return customer;
    }

    @Override
    /** Get the formatted TextComponent that will be used for the sender's
     * username in chat */
    public ITextComponent getDisplayName()
    {
        String s = "Trader";

        TextComponentString TextComponentString = new TextComponentString(s);
        TextComponentString.getStyle().setHoverEvent(this.getHoverEvent());
        TextComponentString.getStyle().setInsertion(this.getUniqueID().toString());
        return TextComponentString;

    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer p)
    {

        if (chests.isEmpty()) { return list; }
        list2.clear();

        addRecipes(p);

        return list2;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /** Called when a player interacts with a mob. e.g. gets milk from a cow,
     * gets into the saddle on a pig. */
    @Override
    public boolean processInteract(EntityPlayer p, EnumHand hand, ItemStack itemstack)
    {
        boolean flag = itemstack != null && itemstack.getItem() == Items.SPAWN_EGG;
        System.out.println("Test");
        if (!flag && this.isEntityAlive() && !this.isChild() && !p.isSneaking())
        {
            if (!this.worldObj.isRemote)
            {
                this.setCustomer(p);
                p.displayVillagerTradeGui(this);
            }

            return true;
        }
        else if (p.isSneaking())
        {
            if (p.getHeldItemMainhand() != null && p.getHeldItemMainhand().hasTagCompound()
                    && p.getHeldItemMainhand().getItem() instanceof ItemTrainer)
            {
                int[] loc = p.getHeldItemMainhand().getTagCompound().getIntArray("coords");
                if (loc.length == 3)
                {
                    ChunkCoordinate c = new ChunkCoordinate(MathHelper.floor_double(loc[0] / 16f), loc[1] / 16,
                            MathHelper.floor_double(loc[2] / 16f), dimension);
                    String owner = TeamManager.getInstance().getLandOwner(c);
                    Vector3 v = Vector3.getNewVector().set(this);
                    String owner1 = TeamManager.getInstance()
                            .getLandOwner(new ChunkCoordinate(MathHelper.floor_double(v.intX() / 16f), v.intY() / 16,
                                    MathHelper.floor_double(v.intZ() / 16f), dimension));
                    if (owner1 == null || !owner1.equals(owner)) { return false; }
                    String team = worldObj.getScoreboard().getPlayersTeam(p.getName()).getRegisteredName();
                    if (owner == null) return false;
                    if (!owner.equals(team)
                            || !TeamManager.getInstance().isAdmin(p.getName(), p.getTeam())) { return false; }
                    BlockPos c1 = new BlockPos(loc[0], loc[1], loc[2]);
                    if (chests.contains(c1)) chests.remove(c1);
                    else chests.add(c1);
                }
            }
            return true;
        }
        else
        {
            return super.processInteract(p, hand, itemstack);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        list.clear();
        list.readRecipiesFromTags(nbt.getCompoundTag("goods"));
        NBTBase temp = nbt.getTag("chests");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                int[] loc = tag.getIntArray("loc");
                if (loc.length == 3) chests.add(new BlockPos(loc[0], loc[1], loc[2]));
            }
        }

        texture = nbt.getString("texture");
    }

    @Override
    public void setCustomer(EntityPlayer p)
    {
        customer = p;
    }

    @Override
    public void setRecipes(MerchantRecipeList p)
    {

        if (chests.isEmpty())
        {
            list = p;
        }
    }

    @Override
    public void useRecipe(MerchantRecipe p)
    {

        Vector3 chest = Vector3.getNewVector();

        // for(BlockPos c1: map.keySet())
        {// TODO write a method for comparing sales
         // MerchantRecipe p1 = map.get(c1);
         // if(p1!=null && p1.hasSameIDsAs(p) &&
         // p1.getItemToSell().isItemEqual(p.getItemToSell()))
         // {
         // chest = Vector3.getNewVector().set(c1);
         // break;
         // }
        }
        System.out.println(chest + " " + map + " " + p);
        if (!chest.isEmpty() && !list2.isEmpty())
        {
            ItemStack a = p.getItemToBuy();
            ItemStack b = p.getSecondItemToBuy();
            ItemStack c = p.getItemToSell();

            if (a != null) a = a.copy();
            if (b != null) b = b.copy();
            if (c != null) c = c.copy();

            TileEntity te = chest.getTileEntity(worldObj);
            if (te instanceof IInventory)
            {
                IInventory inv = (IInventory) te;
                if (inv.getSizeInventory() > 3)
                {
                    int count = 0;
                    for (int i = 3; i < inv.getSizeInventory(); i++)
                    {
                        if (inv.getStackInSlot(i) != null && c.isItemEqual(inv.getStackInSlot(i)))
                        {
                            count += inv.getStackInSlot(i).stackSize;
                            inv.decrStackSize(i, Math.min(c.stackSize, inv.getStackInSlot(i).stackSize));
                        }
                        if (count >= c.stackSize) break;
                    }
                    if (a != null)
                    {
                        count = a.stackSize;
                        for (int i = 3; i < inv.getSizeInventory(); i++)
                        {
                            if (inv.getStackInSlot(i) == null || a.isItemEqual(inv.getStackInSlot(i)))
                            {
                                if (inv.getStackInSlot(i) != null && inv.getStackInSlot(i).stackSize + a.stackSize < 65)
                                {
                                    a.stackSize = inv.getStackInSlot(i).stackSize + a.stackSize;
                                    count = 0;
                                    inv.setInventorySlotContents(i, a.copy());
                                }
                                else
                                {
                                    count = 0;
                                    inv.setInventorySlotContents(i, a.copy());
                                }
                            }
                            if (count == 0) break;
                        }
                    }
                    if (b != null)
                    {
                        count = b.stackSize;
                        for (int i = 3; i < inv.getSizeInventory(); i++)
                        {
                            if (inv.getStackInSlot(i) == null || b.isItemEqual(inv.getStackInSlot(i)))
                            {
                                if (inv.getStackInSlot(i) != null && inv.getStackInSlot(i).stackSize + b.stackSize < 65)
                                {
                                    b.stackSize = inv.getStackInSlot(i).stackSize + b.stackSize;
                                    count = 0;
                                    inv.setInventorySlotContents(i, b);
                                }
                                else
                                {
                                    count = 0;
                                    inv.setInventorySlotContents(i, b);
                                }
                            }
                            if (count == 0) break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setTag("goods", list.getRecipiesAsTags());
        NBTTagList chestsList = new NBTTagList();
        for (BlockPos c : chests)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setIntArray("loc", new int[] { c.getX(), c.getY(), c.getZ() });
            chestsList.appendTag(tag);
        }
        nbt.setTag("chests", chestsList);
        nbt.setString("texture", texture);
    }

}

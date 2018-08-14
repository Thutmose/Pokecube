package pokecube.core.blocks.tradingTable;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.TradeEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.core.common.blocks.DefaultInventory;
import thut.lib.CompatWrapper;

public class TileEntityTradingTable extends TileEntityOwnable implements DefaultInventory
{
    public static boolean   theftEnabled = false;

    private List<ItemStack> inventory    = NonNullList.<ItemStack> withSize(2, ItemStack.EMPTY);

    public EntityPlayer     player1;
    public EntityPlayer     player2;

    public int              time         = 0;
    public int              renderpass;
    boolean                 init         = true;

    public TileEntityTradingTable()
    {
        super();
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        if (!player.getEntityWorld().isRemote)
        {
            if (player.getCachedUniqueIdString().equals(PokecubeManager.getOwner(inventory.get(0))))
            {
                dropCube(inventory.get(0), player);
                this.setInventorySlotContents(0, ItemStack.EMPTY);
            }
            if (player.getCachedUniqueIdString().equals(PokecubeManager.getOwner(inventory.get(1))))
            {
                dropCube(inventory.get(1), player);
                this.setInventorySlotContents(1, ItemStack.EMPTY);
            }
        }
    }

    private void dropCube(ItemStack cube, EntityPlayer player)
    {
        if (cube != ItemStack.EMPTY)
        {
            if (player.isDead || player.getHealth() <= 0 || player.inventory.getFirstEmptyStack() == -1)
            {
                ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(cube, 0F), null);
                MinecraftForge.EVENT_BUS.post(toss);
                if (!toss.isCanceled())
                {
                    player.dropItem(cube, true);
                }
            }
            else if (cube.getItem() != null && (player.isDead || !player.inventory.addItemStackToInventory(cube)))
            {
                ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(cube, 0F), null);
                MinecraftForge.EVENT_BUS.post(toss);
            }
            else player.dropItem(cube, true);
            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return player1 == null ? -1 : player1.getEntityId();
        if (id == 1) return player2 == null ? -1 : player2.getEntityId();
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    @Override
    public String getName()
    {
        return "trading_table";
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) < 64;
    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (world.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    public void openGUI(EntityPlayer player)
    {
        player.openGui(PokecubeMod.core, Config.GUITRADINGTABLE_ID, world, getPos().getX(), getPos().getY(),
                getPos().getZ());
    }

    public void pokeseal(ItemStack a, ItemStack b, IPokemob mob)
    {
        if (b.hasTagCompound())
        {
            NBTTagCompound tag = b.getTagCompound().getCompoundTag(TagNames.POKESEAL);
            a.getTagCompound().setTag(TagNames.POKESEAL, tag.copy());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        NBTBase temp = tagCompound.getTag("Inventory");
        inventory = NonNullList.<ItemStack> withSize(2, ItemStack.EMPTY);
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
        init = false;
    }

    @Override
    public void setField(int id, int value)
    {
        if (id == 0) player1 = (EntityPlayer) world.getEntityByID(value);
        if (id == 1) player2 = (EntityPlayer) world.getEntityByID(value);
    }

    public void trade()
    {
        ItemStack poke1 = inventory.get(0);
        ItemStack poke2 = inventory.get(1);
        if (!PokecubeManager.isFilled(poke1) || !PokecubeManager.isFilled(poke2) || player1 == player2)
        {
            if (player1 != null && player1 == player2 && (CompatWrapper.isValid(poke1) && CompatWrapper.isValid(poke2)))
            {
                tryChange();
            }
            player1 = null;
            player2 = null;
            return;
        }
        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) { return; }

        IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, world);
        IPokemob mon2 = PokecubeManager.itemToPokemob(poke2, world);

        String owner1 = PokecubeManager.getOwner(poke1);
        String owner2 = PokecubeManager.getOwner(poke2);

        UUID trader1 = player1.getUniqueID();
        UUID trader2 = player2.getUniqueID();

        if ((trader1.toString().equals(owner1) && trader2.toString().equals(owner2)))
        {
            mon2.setPokemonOwner(trader1);
            mon1.setPokemonOwner(trader2);
            boolean mon1everstone = PokecubeManager.getHeldItem(poke1) != ItemStack.EMPTY
                    && Tools.isStack(PokecubeManager.getHeldItem(poke1), "everstone");
            boolean mon2everstone = PokecubeManager.getHeldItem(poke2) != ItemStack.EMPTY
                    && Tools.isStack(PokecubeManager.getHeldItem(poke2), "everstone");
            if (!mon1everstone) mon1.setTraded(true);
            if (!mon2everstone) mon2.setTraded(true);
            poke1 = PokecubeManager.pokemobToItem(mon1);
            poke2 = PokecubeManager.pokemobToItem(mon2);
            ItemStack to1 = poke2;
            ItemStack to2 = poke1;
            if (player1.inventory.getFirstEmptyStack() != -1) player1.inventory.addItemStackToInventory(to1);
            else InventoryPC.addPokecubeToPC(to1, world);
            if (player2.inventory.getFirstEmptyStack() != -1) player2.inventory.addItemStackToInventory(to2);
            else InventoryPC.addPokecubeToPC(to2, world);
            MinecraftForge.EVENT_BUS.post(new TradeEvent(world, to1));
            MinecraftForge.EVENT_BUS.post(new TradeEvent(world, to2));
            inventory = Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY);
        }
        player1 = null;
        player2 = null;
    }

    private boolean tryChange()
    {
        ItemStack a = inventory.get(0);
        ItemStack b = inventory.get(1);

        if (!((PokecubeManager.isFilled(a)) || (PokecubeManager.isFilled(b)))) return false;
        if (((PokecubeManager.isFilled(a)) && (PokecubeManager.isFilled(b)))) return false;
        if (a.getItem() instanceof IPokecube && b.getItem() instanceof IPokecube)
        {
            boolean aFilled;
            if (((aFilled = PokecubeManager.isFilled(a)) && (PokecubeManager.isFilled(b)))) return false;
            ItemStack first = aFilled ? a : b;
            ItemStack second = aFilled ? b : a;
            ItemStack stack;
            ResourceLocation id = PokecubeItems.getCubeId(second);

            // Pokeseal is id 2, send this to pokeseal recipe to process.
            if (id != null && id.getResourcePath().equals("seal"))
            {
                stack = RecipePokeseals.process(first, second);
            }
            else
            {
                // Set the pokecube for the mob to the new one.
                PokecubeManager.setOwner(first, player1.getUniqueID());
                NBTTagCompound visualsTag = TagNames.getPokecubePokemobTag(first.getTagCompound())
                        .getCompoundTag(TagNames.VISUALSTAG);
                NBTTagCompound cube = new NBTTagCompound();
                stack = second.copy();
                second.writeToNBT(cube);
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                visualsTag.setTag(TagNames.POKECUBE, cube);
                stack.getTagCompound().setTag(TagNames.POKEMOB,
                        first.getTagCompound().getCompoundTag(TagNames.POKEMOB).copy());
            }

            // Extract and re-insert pokemob to ensure that the cube is properly
            // setup.
            stack = PokecubeManager.pokemobToItem(PokecubeManager.itemToPokemob(stack, getWorld()));

            player1.inventory.addItemStackToInventory(stack);
            player1 = null;
            player2 = null;
            inventory = Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY);
            return true;
        }
        int index = PokecubeManager.isFilled(a) ? 0 : 1;
        IPokemob mob = PokecubeManager.isFilled(a) ? PokecubeManager.itemToPokemob(inventory.get(0), world)
                : PokecubeManager.itemToPokemob(inventory.get(1), world);
        if (!(a.getItem() == Items.EMERALD || b.getItem() == Items.EMERALD))
        {
            if (mob.getPokemonOwnerID() == null || player1.getUniqueID().equals(mob.getPokemonOwnerID()))
            {
                PokecubeManager.setOwner(inventory.get(index),
                        mob.getPokemonOwnerID() == null ? player1.getUniqueID() : null);
                player1.inventory.addItemStackToInventory(inventory.get(index));
                inventory = Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY);
            }
            return false;
        }
        if (theftEnabled) return false;
        PokecubeManager.setOwner(inventory.get(index), player1.getUniqueID());
        player1.inventory.addItemStackToInventory(inventory.get(index));
        inventory = Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY);
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack;
            if (CompatWrapper.isValid(stack = inventory.get(i)))
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("Inventory", itemList);
        return tagCompound;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        // Reset trading table stuff if stacks change.
        if (player1 != null || player2 != null)
        {
            player1 = null;
            player2 = null;
        }
        DefaultInventory.super.setInventorySlotContents(index, stack);
    }

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
    }
}

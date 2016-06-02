package pokecube.adventures.items.bags;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;

public class ContainerBag extends Container
{

    public static int STACKLIMIT = 64;
    public static int yOffset;
    public static int xOffset;

    /** Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise */
    public static boolean isItemValid(ItemStack itemstack)
    {
        boolean valid = PokecubeItems.isValidHeldItem(itemstack) || itemstack.getItem() instanceof IPokemobUseable;
        valid |= PokecubeItems.getFossilNumber(itemstack) != 0;
        boolean cube = PokecubeItems.getEmptyCube(itemstack) == itemstack.getItem()
                && !PokecubeManager.isFilled(itemstack);
        return valid || cube;
    }

    public final InventoryBag    invBag;
    public final InventoryPlayer invPlayer;

    public ContainerBag(InventoryPlayer ivplay)
    {
        super();
        xOffset = 0;
        yOffset = 0;
        invBag = InventoryBag.getBag(ivplay.player.getUniqueID().toString());
        invPlayer = ivplay;
        bindInventories();
    }

    /**
     * 
     */
    @Override
    protected Slot addSlotToContainer(Slot par1Slot)
    {
        par1Slot.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(par1Slot);
        this.inventoryItemStacks.add(invBag.getStackInSlot(par1Slot.getSlotIndex()));
        return par1Slot;
    }

    protected void bindInventories()
    {
        clearSlots();
        bindPlayerInventory();
        bindPCInventory();
    }

    protected void bindPCInventory()
    {
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new SlotBag(invBag, +j + i * 9, 8 + j * 18 + xOffset, 18 + i * 18 + yOffset));
            }
        }
        // int k = 0;
        for (Object o : inventorySlots)
        {
            if (o instanceof Slot)
            {
                ((Slot) o).onSlotChanged();
            }
        }
    }

    protected void bindPlayerInventory()
    {
        int offset = 64 + yOffset;

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18 + xOffset, 142 + offset));
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18 + xOffset, 84 + i * 18 + offset));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return true;
    }

    public void changeName(String name)
    {
        invBag.boxes[invBag.getPage()] = name;

        if (PokecubeCore.isOnClientSide())
        {
            byte[] string = name.getBytes();
            byte[] message = new byte[string.length + 2];

            message[0] = 11;
            message[1] = (byte) string.length;
            for (int i = 2; i < message.length; i++)
            {
                message[i] = string[i - 2];
            }
            PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket((byte) 6, message);
            PokecubePacketHandler.sendToServer(packet);
            return;
        }
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    public ItemStack clientSlotClick(int i, int j, int flag, EntityPlayer player)
    {
        ItemStack itemstack = invPlayer.getItemStack();
        Slot slot = inventorySlots.get(i);
        ItemStack inSlot = slot.getStack();
        if (flag == 0 || flag == 5)
        {
            invPlayer.setItemStack(inSlot != null ? inSlot.copy() : null);
            inSlot = itemstack;
            return inSlot;
        }

        return inSlot;
    }

    @SideOnly(Side.CLIENT)
    public String getPage()
    {
        return invBag.boxes[invBag.getPage()];
    }

    @SideOnly(Side.CLIENT)
    public String getPageNb()
    {
        return Integer.toString(invBag.getPage() + 1);
    }

    @Override
    public Slot getSlot(int par1)
    {
        return this.inventorySlots.get(par1);
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        PASaveHandler.getInstance().saveBag(player.getUniqueID().toString());
        super.onContainerClosed(player);
    }

    /** args: slotID, itemStack to put in slot */
    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack)
    {
        this.getSlot(par1).putStack(par2ItemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)

    /** places itemstacks in first x slots, x being aitemstack.lenght */
    public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack)
    {
        for (int i = 0; i < par1ArrayOfItemStack.length; ++i)
        {
            if (this.getSlot(i) != null) this.getSlot(i).putStack(par1ArrayOfItemStack[i]);
        }
    }

    @Override
    public ItemStack slotClick(int i, int j, int flag, EntityPlayer entityplayer)
    {
        // if(true)
        // return super.slotClick(i, j, flag, entityplayer);
        //
        if (i < 0) return null;
        if (PokecubeCore.isOnClientSide() && FMLClientHandler.instance()
                .getServer() != null) { return clientSlotClick(i, j, flag, entityplayer); }
        if (flag != 0 && flag != 5)
        {
            ItemStack itemstack = null;
            Slot slot = inventorySlots.get(i);

            if (slot != null && slot.getHasStack())
            {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();
                if (!ContainerBag.isItemValid(itemstack1)) return null;

                if (i > 35)
                {
                    if (!mergeItemStack(itemstack1, 0, 36, false)) { return null; }
                }
                else
                {
                    if (!mergeItemStack(itemstack1, 36, 89, false)) { return null; }
                }

                if (itemstack1.stackSize == 0)
                {
                    slot.putStack(null);
                }
                else
                {
                    slot.onSlotChanged();
                }

                if (itemstack1.stackSize != itemstack.stackSize)
                {
                    slot.onPickupFromSlot(entityplayer, itemstack1);
                }
                else
                {
                    return null;
                }
            }

            if (itemstack != null && isItemValid(itemstack))
            {
                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return super.slotClick(i, j, flag, entityplayer);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
        ItemStack stack = null;
        return stack;
    }

    public void updateInventoryPages(int page)
    {
        if (page < 0) page = InventoryBag.PAGECOUNT - 1;
        if (page > InventoryBag.PAGECOUNT - 1) page = 0;
        invBag.setPage(page);
        if (!invPlayer.player.isServerWorld())
        {
            MessageServer packet;
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer(1));
            buf.writeByte(6);
            buf.writeInt(page);
            packet = new MessageServer(buf);
            PokecubePacketHandler.sendToServer(packet);

        }
        bindInventories();
    }

}

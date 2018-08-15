package pokecube.core.blocks.pc;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.packets.PacketPC;
import thut.lib.CompatWrapper;

@ChestContainer(isLargeChest = true, showButtons = false)
public class ContainerPC extends Container
{
    public static Set<Predicate<ItemStack>> CUSTOMPCWHILTELIST = Sets.newHashSet();

    public static int                       STACKLIMIT         = 64;
    public static int                       yOffset;
    public static int                       xOffset;

    /** Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise */
    public static boolean isItemValid(ItemStack itemstack)
    {
        if (!CompatWrapper.isValid(itemstack)) return false;
        boolean eggorCube = !PokecubeMod.core.getConfig().pcHoldsOnlyPokecubes || PokecubeManager.isFilled(itemstack)
                || itemstack.getItem() instanceof ItemWrittenBook || itemstack.getItem() instanceof ItemPokemobEgg
                || itemstack.getItem() instanceof ItemPokedex || itemstack.hasCapability(MegaCapability.MEGA_CAP, null);
        if (!eggorCube)
        {
            for (Predicate<ItemStack> tester : CUSTOMPCWHILTELIST)
            {
                if (tester.test(itemstack)) return true;
            }
        }
        return eggorCube;
    }

    public final InventoryPC     inv;
    public final InventoryPlayer invPlayer;
    public final TileEntityPC    pcTile;

    public boolean               release   = false;
    // private GuiPC gpc;

    public boolean[]             toRelease = new boolean[54];

    public ContainerPC(InventoryPlayer ivplay, TileEntityPC pc)
    {
        super();
        xOffset = 0;
        yOffset = 0;
        InventoryPC temp = pc != null ? pc.getPC() != null ? pc.getPC() : InventoryPC.getPC(ivplay.player.getUniqueID())
                : InventoryPC.getPC(ivplay.player.getUniqueID());
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) inv = InventoryPC.getPC(ivplay.player);
        else inv = temp;
        invPlayer = ivplay;
        pcTile = pc;
        bindInventories();
    }

    protected void bindInventories()
    {
        // System.out.println("bind");
        clearSlots();
        bindPCInventory();
        bindPlayerInventory();
    }

    protected void bindPCInventory()
    {
        int n = 0;
        n = inv.getPage() * 54;
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new SlotPC(inv, n + j + i * 9, 8 + j * 18 + xOffset, 18 + i * 18 + yOffset));
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
        inv.boxes[inv.getPage()] = name;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PacketPC packet = new PacketPC(PacketPC.RENAME);
            packet.data.setString("N", name);
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    @SideOnly(Side.CLIENT)
    public String getPage()
    {
        return inv.boxes[inv.getPage()];
    }

    @SideOnly(Side.CLIENT)
    public String getPageNb()
    {
        return Integer.toString(inv.getPage() + 1);
    }

    public boolean getRelease()
    {
        return release;
    }

    @Override
    public Slot getSlot(int par1)
    {
        return this.inventorySlots.get(par1);
    }

    public void gotoInventoryPage(int page)
    {
        if (page - 1 == inv.getPage()) return;
        inv.setPage(page - 1);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PacketPC packet = new PacketPC(PacketPC.SETPAGE);
            packet.data.setInteger("P", page);
            PokecubeMod.packetPipeline.sendToServer(packet);
            inv.clear();
        }
        bindInventories();
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        inv.closeInventory(player);
    }

    public void toggleAuto()
    {
        inv.autoToPC = !inv.autoToPC;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PacketPC packet = new PacketPC(PacketPC.TOGGLEAUTO);
            packet.data.setBoolean("A", inv.autoToPC);
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
    }

    public void setRelease(boolean bool)
    {
        if (release && !bool)
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                PacketPC packet = new PacketPC(PacketPC.RELEASE);
                packet.data.setBoolean("T", false);
                packet.data.setInteger("page", inv.getPage());
                for (int i = 0; i < 54; i++)
                {
                    if (toRelease[i])
                    {
                        packet.data.setBoolean("val" + i, true);
                    }
                }
                PokecubeMod.packetPipeline.sendToServer(packet);
            }
        }
        release = bool;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        if (release)
        {
            if (slotId < 54 && slotId >= 0) toRelease[slotId] = !toRelease[slotId];
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int numRows = 6;
            if (index < numRows * 9)
            {
                if (!this.mergeItemStack(itemstack1, numRows * 9, this.inventorySlots.size(),
                        false)) { return ItemStack.EMPTY; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, numRows * 9, false)) { return ItemStack.EMPTY; }

            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    public void updateInventoryPages(int dir, InventoryPlayer invent)
    {
        int page = (inv.getPage() == 0) && (dir == -1) ? InventoryPC.PAGECOUNT - 1
                : (inv.getPage() + dir) % InventoryPC.PAGECOUNT;
        page += 1;
        gotoInventoryPage(page);
    }
}

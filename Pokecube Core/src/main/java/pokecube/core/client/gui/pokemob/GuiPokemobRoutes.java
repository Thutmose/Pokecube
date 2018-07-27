package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.RouteEditHelper.GuardEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.pokemobs.PacketPokemobGui;

public class GuiPokemobRoutes extends GuiContainer
{
    final IInventory         playerInventory;
    final IInventory         pokeInventory;
    final IPokemob           pokemob;
    final Entity             entity;
    final IGuardAICapability guard;
    ScrollGui                list;
    private float            yRenderAngle = 10;
    private float            xRenderAngle = 0;

    public GuiPokemobRoutes(IInventory playerInv, IPokemob pokemob)
    {
        super(new ContainerPokemob(playerInv, pokemob.getPokemobInventory(), pokemob, false));
        this.pokemob = pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = pokemob.getPokemobInventory();
        this.entity = pokemob.getEntity();
        this.guard = this.entity.getCapability(EventsHandler.GUARDAI_CAP, null);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    int num;

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = width / 2 - 10;
        int yOffset = height / 2 - 77;
        buttonList.add(new GuiButton(0, xOffset + 60, yOffset, 30, 10, "Inv"));
        buttonList.add(new GuiButton(1, xOffset + 30, yOffset, 30, 10, "ST"));
        buttonList.add(new GuiButton(2, xOffset + 00, yOffset, 30, 10, "AI"));
        Function<NBTTagCompound, NBTTagCompound> function = new Function<NBTTagCompound, NBTTagCompound>()
        {
            @Override
            public NBTTagCompound apply(NBTTagCompound t)
            {
                PacketSyncRoutes.sendServerPacket(entity, t);
                return t;
            }
        };
        final List<IGuiListEntry> entries = Lists.newArrayList();
        int dx = 0;
        int dy = 14;
        RouteEditHelper.getGuiList(entries, guard, function, entity, this, 60, dx, dy, 50);
        list = new ScrollGui(mc, 100, 50, 50, xOffset - 5, yOffset, entries);
        list.scrollBar = false;
        buttonList.add(new GuiButton(3, xOffset + 45, yOffset + 54, 30, 10, "\u21e7"));
        buttonList.add(new GuiButton(4, xOffset + 15, yOffset + 54, 30, 10, "\u21e9"));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        list.mouseClicked(mouseX, mouseY, mouseButton);
        int slot = list.getSlotIndexFromScreenCoords(mouseX, mouseY);
        for (int i = 0; i < list.getSize(); i++)
        {
            if (i != slot)
            {
                ((GuardEntry) list.getListEntry(i)).location.setFocused(false);
                ((GuardEntry) list.getListEntry(i)).timeperiod.setFocused(false);
                ((GuardEntry) list.getListEntry(i)).variation.setFocused(false);
            }
            else
            {
                ((GuardEntry) list.getListEntry(i)).location.mouseClicked(mouseX, mouseY, mouseButton);
                ((GuardEntry) list.getListEntry(i)).timeperiod.mouseClicked(mouseX, mouseY, mouseButton);
                ((GuardEntry) list.getListEntry(i)).variation.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        for (int i = 0; i < list.getSize(); i++)
        {
            ((GuardEntry) list.getListEntry(i)).keyTyped(typedChar, keyCode);
        }
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(this.pokeInventory.hasCustomName() ? this.pokeInventory.getName()
                : I18n.format(this.pokeInventory.getName(), new Object[0]), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
                : I18n.format(this.playerInventory.getName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException
    {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, entity.getEntityId());
        }
        else if (guibutton.id == 1)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, entity.getEntityId());
        }
        else if (guibutton.id == 2)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.AI, entity.getEntityId());
        }
        else if (guibutton.id == 3)
        {
            list.scrollBy(-50);
        }
        else if (guibutton.id == 4)
        {
            list.scrollBy(50);
        }
        else
        {
            this.list.actionPerformed(guibutton);
        }
        num = this.list.getAmountScrolled() / 50;
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        this.list.drawScreen(i, j, f);
        this.renderHoveredToolTip(i, j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        yRenderAngle = -entity.rotationYaw + 45;
        xRenderAngle = 0;
        GuiPokemob.renderMob(pokemob, k, l, xSize, ySize, xRenderAngle, yRenderAngle, 0, 1);
        String number = num + "";
        fontRenderer.drawString(number, k + 87 - fontRenderer.getStringWidth(number), l + 62, 0xFF888888);
    }
}

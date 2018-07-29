package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.RouteEditHelper.GuardEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;

public class EditRoutePage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    protected final int      index;
    final IGuardAICapability guard;
    public boolean           scroll = false;

    public EditRoutePage(GuiEditTrainer watch, int index)
    {
        super(watch);
        this.index = index;
        guard = watch.guard;
    }

    @Override
    protected void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        int width = 180;
        if (guard != null)
        {
            this.height = parent.height;
            this.width = parent.width;
            Function<NBTTagCompound, NBTTagCompound> function = new Function<NBTTagCompound, NBTTagCompound>()
            {
                @Override
                public NBTTagCompound apply(NBTTagCompound t)
                {
                    PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                    packet.data = t;
                    onPageClosed();
                    PokecubeMod.packetPipeline.sendToServer(packet);
                    onPageOpened();
                    initList();
                    return t;
                }
            };
            int dx = 0;
            int dy = 0;
            RouteEditHelper.getGuiList(entries, guard, function, parent.entity, this, width, dx, dy, 125);
        }
        int x = parent.width / 2 - 124;
        int y = parent.height / 2 - 60;
        list = new ScrollGui(mc, 248, 125, 40, x, y, entries);
    }

    @Override
    protected void onPageOpened()
    {
        int x = parent.width / 2;
        int y = parent.height / 2;
        // Init buttons
        String home = I18n.format("traineredit.button.home");
        parent.getButtons().add(new Button(0, x - 25, y + 64, 50, 12, home));

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        switch (button.id)
        {
        case 0:
            parent.setIndex(0);
            break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (scroll) super.mouseClicked(mouseX, mouseY, mouseButton);
        scroll = false;
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

    @Override
    public void handleMouseInput() throws IOException
    {
        scroll = true;
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.routes"), x, y, 0xFFFFFFFF);
    }

    @Override
    protected void onPageClosed()
    {
        super.onPageClosed();
        this.parent.getButtons().removeIf(new Predicate<GuiButton>()
        {
            @Override
            public boolean test(GuiButton t)
            {
                return t instanceof Button;
            }
        });
    }
}

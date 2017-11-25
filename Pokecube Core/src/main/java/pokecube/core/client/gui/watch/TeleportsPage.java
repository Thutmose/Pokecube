package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class TeleportsPage extends ListPage
{
    public static class TeleOption implements IGuiListEntry
    {
        final TeleportsPage parent;
        final int           offsetY;
        final Minecraft     mc;
        final TeleDest      dest;
        final GuiTextField  text;
        final GuiButton     delete;
        final GuiButton     confirm;
        final int           guiHeight;

        public TeleOption(Minecraft mc, int offsetY, TeleDest dest, GuiTextField text, int height, TeleportsPage parent)
        {
            this.dest = dest;
            this.text = text;
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;
            delete = new GuiButton(0, 0, 0, 10, 10, "x");
            delete.packedFGColour = 0xFFFF0000;
            confirm = new GuiButton(0, 0, 0, 10, 10, "Y");
            confirm.enabled = false;
        }

        @Override
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected)
        {
            boolean fits = true;
            text.xPosition = x - 2;
            text.yPosition = y - 4;
            delete.yPosition = y - 5;
            delete.xPosition = x - 1 + text.width;
            confirm.yPosition = y - 5;
            confirm.xPosition = x - 2 + 10 + text.width;
            fits = text.yPosition >= offsetY;
            fits = fits && text.yPosition + text.height <= offsetY + guiHeight;
            if (fits)
            {
                text.drawTextBox();
                delete.drawButton(mc, mouseX, mouseY);
                confirm.drawButton(mc, mouseX, mouseY);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            boolean fits = true;
            fits = text.yPosition >= offsetY;
            fits = fits && mouseX - text.xPosition >= 0;
            fits = fits && mouseX - text.xPosition <= text.width;
            fits = fits && text.yPosition + text.height <= offsetY + guiHeight;
            text.setFocused(fits);
            if (delete.isMouseOver())
            {
                delete.playPressSound(this.mc.getSoundHandler());
                confirm.enabled = !confirm.enabled;
            }
            else if (confirm.isMouseOver() && confirm.enabled)
            {
                confirm.playPressSound(this.mc.getSoundHandler());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(dest.index, parent.watch.player.getCachedUniqueIdString());
                // Update the list for the page.
                parent.initList();
            }
            return fits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            text.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_RETURN)
            {
                if (!text.getText().equals(dest.getName()))
                {
                    PacketPokedex.sendRenameTelePacket(text.getText(), dest.index);
                    dest.setName(text.getText());
                }
            }
        }

    }

    protected List<TeleDest>     locations;
    protected List<GuiTextField> teleNames = Lists.newArrayList();

    public TeleportsPage(GuiPokeWatch watch)
    {
        super(watch);
    }

    @Override
    protected void initList()
    {
        locations = TeleportHandler.getTeleports(watch.player.getCachedUniqueIdString());
        List<IGuiListEntry> entries = Lists.newArrayList();
        int offsetX = (watch.width - 160) / 2 + 10;
        int offsetY = (watch.height - 160) / 2 + 20;
        int height = 120;
        for (TeleDest d : locations)
        {
            GuiTextField name = new GuiTextField(0, fontRendererObj, 0, 0, 110, 10);
            teleNames.add(name);
            name.setText(d.getName());
            entries.add(new TeleOption(mc, offsetY, d, name, height, this));
        }
        list = new ScrollGui(mc, 140, height, 10, offsetX, offsetY, entries);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int slot = list.getSlotIndexFromScreenCoords(mouseX, mouseY);
        if (slot != -1)
        {
            for (int i = 0; i < teleNames.size(); i++)
            {
                if (i != slot)
                {
                    teleNames.get(i).setFocused(false);
                    ((TeleOption) list.getListEntry(i)).keyTyped(' ', Keyboard.KEY_RETURN);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        for (int i = 0; i < teleNames.size(); i++)
        {
            GuiTextField text = teleNames.get(i);
            if (text.isFocused())
            {
                ((TeleOption) list.getListEntry(i)).keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected String getTitle()
    {
        return I18n.format("pokewatch.title.teleports");
    }

}

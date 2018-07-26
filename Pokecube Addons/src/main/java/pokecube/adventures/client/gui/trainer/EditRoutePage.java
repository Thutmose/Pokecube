package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability.IGuardTask;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.TimePeriod;

public class EditRoutePage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class GuardEntry implements IGuiListEntry
    {
        final int           index;
        final GuiTextField  location;
        final GuiTextField  timeperiod;
        final GuiTextField  variation;
        final EditRoutePage parent;
        final GuiButton     delete;
        final GuiButton     confirm;
        final GuiButton     moveUp;
        final GuiButton     moveDown;

        public GuardEntry(int index, EditRoutePage parent, GuiTextField location, GuiTextField timeperiod,
                GuiTextField variation)
        {
            this.parent = parent;
            this.location = location;
            this.timeperiod = timeperiod;
            this.variation = variation;
            this.index = index;
            delete = new GuiButton(0, 0, 0, 10, 10, "x");
            delete.packedFGColour = 0xFFFF0000;
            confirm = new GuiButton(0, 0, 0, 10, 10, "Y");
            confirm.enabled = false;
            moveUp = new GuiButton(0, 0, 0, 10, 10, "\u21e7");
            moveDown = new GuiButton(0, 0, 0, 10, 10, "\u21e9");
            moveUp.enabled = index > 0 && index < parent.num;
            moveDown.enabled = index < parent.num - 1;
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {

            int width = 10;
            delete.y = y - 5;
            delete.x = x - 1 + width;
            confirm.y = y - 5;
            confirm.x = x - 2 + 10 + width;
            moveUp.y = y - 5;
            moveUp.x = x - 2 + 18 + width;
            moveDown.y = y - 5;
            moveDown.x = x - 2 + 26 + width;

            boolean fits = true;
            location.x = x - 2 + 60;
            location.y = y - 4;
            timeperiod.y = y + 7;
            timeperiod.x = x - 2 + 60;
            variation.y = y + 18;
            variation.x = x - 2 + 60;
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            fits = location.y >= offsetY;
            fits = fits && location.y + 2 * location.height <= offsetY + guiHeight;
            if (fits)
            {
                RenderHelper.disableStandardItemLighting();
                location.drawTextBox();
                timeperiod.drawTextBox();
                variation.drawTextBox();

                delete.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                confirm.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                moveUp.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                moveDown.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                GL11.glColor3f(1, 1, 1);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 100;
            boolean buy1Fits = true;
            buy1Fits = location.y >= offsetY;
            buy1Fits = buy1Fits && mouseX - location.x >= 0;
            buy1Fits = buy1Fits && mouseX - location.x <= location.width;
            buy1Fits = buy1Fits && mouseY - location.y >= 0;
            buy1Fits = buy1Fits && mouseY - location.y <= location.height;
            buy1Fits = buy1Fits && location.y + location.height <= offsetY + guiHeight;
            location.setFocused(buy1Fits);
            boolean buy2Fits = true;
            buy2Fits = timeperiod.y >= offsetY;
            buy2Fits = buy2Fits && mouseX - timeperiod.x >= 0;
            buy2Fits = buy2Fits && mouseX - timeperiod.x <= timeperiod.width;
            buy2Fits = buy2Fits && mouseY - timeperiod.y >= 0;
            buy2Fits = buy2Fits && mouseY - timeperiod.y <= timeperiod.height;
            buy2Fits = buy2Fits && timeperiod.y + timeperiod.height <= offsetY + guiHeight;
            timeperiod.setFocused(buy2Fits);
            boolean sellFits = true;
            sellFits = variation.y >= offsetY;
            sellFits = sellFits && mouseX - variation.x >= 0;
            sellFits = sellFits && mouseX - variation.x <= variation.width;
            sellFits = sellFits && mouseY - variation.y >= 0;
            sellFits = sellFits && mouseY - variation.y <= variation.height;
            sellFits = sellFits && variation.y + variation.height <= offsetY + guiHeight;
            variation.setFocused(sellFits);

            if (delete.isMouseOver())
            {
                delete.playPressSound(this.parent.mc.getSoundHandler());
                confirm.enabled = !confirm.enabled;
            }
            else if (confirm.isMouseOver() && confirm.enabled)
            {
                confirm.playPressSound(this.parent.mc.getSoundHandler());
                // Send packet for removal server side
                delete();
            }
            else if (moveUp.isMouseOver() && moveUp.enabled)
            {
                moveUp.playPressSound(this.parent.mc.getSoundHandler());
                // Update the list for the page.
                reOrder(-1);
            }
            else if (moveDown.isMouseOver() && moveDown.enabled)
            {
                moveDown.playPressSound(this.parent.mc.getSoundHandler());
                // Update the list for the page.
                reOrder(1);
            }
            return buy1Fits || buy2Fits || sellFits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            location.textboxKeyTyped(typedChar, keyCode);
            timeperiod.textboxKeyTyped(typedChar, keyCode);
            variation.textboxKeyTyped(typedChar, keyCode);
            if (keyCode != Keyboard.KEY_RETURN) return;
            if (!(location.isFocused() || timeperiod.isFocused() || variation.isFocused())) return;
            update();
        }

        private void delete()
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("GU", true);
            tag.setInteger("I", index);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.parent.entity.getEntityId());
            if (index < parent.guard.getTasks().size()) parent.guard.getTasks().remove(index);
            parent.onPageClosed();
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.onPageOpened();
            parent.initList();
        }

        private void reOrder(int dir)
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("GU", true);
            tag.setInteger("I", index);
            tag.setInteger("N", dir);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.parent.entity.getEntityId());
            int index1 = tag.getInteger("I");
            int index2 = index1 + tag.getInteger("N");
            IGuardTask temp = parent.guard.getTasks().get(index1);
            parent.guard.getTasks().set(index1, parent.guard.getTasks().get(index2));
            parent.guard.getTasks().set(index2, temp);
            parent.onPageClosed();
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.onPageOpened();
            parent.initList();
        }

        private void update()
        {
            BlockPos loc = posFromText(location.getText());
            TimePeriod time = timeFromText(timeperiod.getText());
            float dist = 2;
            try
            {
                dist = Float.parseFloat(variation.getText());
            }
            catch (NumberFormatException e)
            {
                ITextComponent mess = new TextComponentTranslation("traineredit.info.dist.formatinfo");
                parent.mc.player.sendStatusMessage(mess, true);
                return;
            }
            if (loc != null && time != null)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setBoolean("GU", true);
                tag.setInteger("I", index);
                // TODO generalize this maybe?
                IGuardTask task = index < parent.guard.getTasks().size() ? parent.guard.getTasks().get(index)
                        : new GuardAICapability.GuardTask();
                if (index >= parent.guard.getTasks().size()) parent.guard.getTasks().add(task);
                task.setPos(loc);
                task.setActiveTime(time);
                task.setRoamDistance(dist);
                NBTBase var = task.serialze();
                tag.setTag("V", var);
                packet.data.setTag("T", tag);
                packet.data.setInteger("I", parent.parent.entity.getEntityId());
                parent.onPageClosed();
                PokecubeMod.packetPipeline.sendToServer(packet);
                parent.onPageOpened();
                parent.initList();
            }
        }

        private TimePeriod timeFromText(String text)
        {
            if (text.isEmpty()) return null;
            String[] args = text.split(" ");
            if (args.length == 2)
            {
                try
                {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    return new TimePeriod(x, y);
                }
                catch (NumberFormatException e)
                {
                    // Send status message about not working here.
                    ITextComponent mess = new TextComponentTranslation("traineredit.info.time.formaterror");
                    parent.mc.player.sendStatusMessage(mess, true);
                }
            }
            else if (args.length != 0)
            {
                // Send status message about not working here.
                ITextComponent mess = new TextComponentTranslation("traineredit.info.time.formatinfo");
                parent.mc.player.sendStatusMessage(mess, true);
            }
            return null;
        }

        private BlockPos posFromText(String text)
        {
            if (text.isEmpty()) return null;
            String[] args = text.split(" ");
            if (args.length == 3)
            {
                try
                {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int z = Integer.parseInt(args[2]);
                    return new BlockPos(x, y, z);
                }
                catch (NumberFormatException e)
                {
                    // Send status message about not working here.
                    ITextComponent mess = new TextComponentTranslation("traineredit.info.pos.formaterror");
                    parent.mc.player.sendStatusMessage(mess, true);
                }
            }
            else if (args.length != 0)
            {
                // Send status message about not working here.
                ITextComponent mess = new TextComponentTranslation("traineredit.info.pos.formatinfo");
                parent.mc.player.sendStatusMessage(mess, true);
            }
            return null;
        }

    }

    protected final int      index;
    protected int            num;
    final IGuardAICapability guard;

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

        int num = 0;
        int width = 180;
        if (guard != null)
        {
            for (IGuardTask task : guard.getTasks())
            {
                GuiTextField location = new GuiTextField(0, fontRenderer, 0, 0, width, 10);
                GuiTextField time = new GuiTextField(1, fontRenderer, 0, 0, width, 10);
                GuiTextField dist = new GuiTextField(2, fontRenderer, 0, 0, width, 10);
                location.setMaxStringLength(Short.MAX_VALUE);
                time.setMaxStringLength(Short.MAX_VALUE);
                dist.setMaxStringLength(Short.MAX_VALUE);
                if (task.getPos() != null)
                    location.setText(task.getPos().getX() + " " + task.getPos().getY() + " " + task.getPos().getZ());
                time.setText(task.getActiveTime().startTick + " " + task.getActiveTime().endTick);
                dist.setText(task.getRoamDistance() + "");
                location.moveCursorBy(-location.getCursorPosition());
                time.moveCursorBy(-time.getCursorPosition());
                dist.moveCursorBy(-dist.getCursorPosition());
                GuardEntry entry = new GuardEntry(num++, this, location, time, dist);
                entries.add(entry);
            }
            // Blank value.
            GuiTextField buy1 = new GuiTextField(0, fontRenderer, 0, 0, width, 10);
            GuiTextField buy2 = new GuiTextField(1, fontRenderer, 0, 0, width, 10);
            GuiTextField sell = new GuiTextField(2, fontRenderer, 0, 0, width, 10);
            buy1.setMaxStringLength(Short.MAX_VALUE);
            buy2.setMaxStringLength(Short.MAX_VALUE);
            sell.setMaxStringLength(Short.MAX_VALUE);
            GuardEntry entry = new GuardEntry(num++, this, buy1, buy2, sell);
            entries.add(entry);
        }
        int x = parent.width / 2 - 124;
        int y = parent.height / 2 - 60;
        list = new ScrollGui(mc, 248, 125, 40, x, y, entries);
    }

    @Override
    public void initGui()
    {
        super.initGui();
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
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

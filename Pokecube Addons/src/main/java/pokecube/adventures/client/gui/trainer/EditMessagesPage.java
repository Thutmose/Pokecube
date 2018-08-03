package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import pokecube.adventures.entity.helper.Action;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;

public class EditMessagesPage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class MessageEntry implements IGuiListEntry
    {
        final GuiTextField     message;
        final GuiTextField     action;
        final MessageState     state;
        final EditMessagesPage parent;

        public MessageEntry(EditMessagesPage parent, GuiTextField action, GuiTextField message, MessageState state)
        {
            this.parent = parent;
            this.state = state;
            this.action = action;
            this.message = message;
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            boolean fits = true;
            message.x = x - 2 + 60;
            message.y = y - 4;
            action.y = y + 7;
            action.x = x - 2 + 60;
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            fits = message.y >= offsetY;
            fits = fits && message.y + 2 * message.height <= offsetY + guiHeight;
            if (fits)
            {
                parent.drawString(parent.fontRenderer, state.toString(), x, y + 2, 0xFFFFFFFF);
                parent.drawString(parent.fontRenderer, "M", x + 50, y - 3, 0xFFFFFFFF);
                parent.drawString(parent.fontRenderer, "A", x + 50, y + 8, 0xFFFFFFFF);
                message.drawTextBox();
                action.drawTextBox();
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            boolean messageFits = true;
            messageFits = message.y >= offsetY;
            messageFits = messageFits && mouseX - message.x >= 0;
            messageFits = messageFits && mouseX - message.x <= message.width;
            messageFits = messageFits && mouseY - message.y >= 0;
            messageFits = messageFits && mouseY - message.y <= message.height;
            messageFits = messageFits && message.y + message.height <= offsetY + guiHeight;
            message.setFocused(messageFits);
            boolean actionFits = true;
            actionFits = action.y >= offsetY;
            actionFits = actionFits && mouseX - action.x >= 0;
            actionFits = actionFits && mouseX - action.x <= action.width;
            actionFits = actionFits && mouseY - action.y >= 0;
            actionFits = actionFits && mouseY - action.y <= action.height;
            actionFits = actionFits && action.y + action.height <= offsetY + guiHeight;
            action.setFocused(actionFits);
            return messageFits || actionFits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            message.textboxKeyTyped(typedChar, keyCode);
            action.textboxKeyTyped(typedChar, keyCode);
            if (keyCode != Keyboard.KEY_RETURN) return;
            if (!(message.isFocused() || action.isFocused())) return;
            parent.parent.messages.setMessage(state, message.getText());
            parent.parent.messages.setAction(state, new Action(action.getText()));
            parent.onPageClosed();
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            NBTBase tag = CapabilityNPCMessages.storage.writeNBT(CapabilityNPCMessages.MESSAGES_CAP,
                    parent.parent.messages, null);
            packet.data.setTag("T", tag);
            packet.data.setByte("V", (byte) 2);
            packet.data.setInteger("I", parent.parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.onPageOpened();
        }
    }

    protected final int index;

    public EditMessagesPage(GuiEditTrainer watch, int index)
    {
        super(watch);
        this.index = index;
    }

    @Override
    protected void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();
        for (MessageState state : MessageState.values())
        {
            GuiTextField messageField = new GuiTextField(state.ordinal(), fontRenderer, 0, 0, 180, 10);
            GuiTextField actionField = new GuiTextField(state.ordinal(), fontRenderer, 0, 0, 180, 10);
            messageField.setMaxStringLength(Short.MAX_VALUE);
            actionField.setMaxStringLength(Short.MAX_VALUE);
            String message = parent.messages.getMessage(state);
            String action = parent.messages.getAction(state) != null ? parent.messages.getAction(state).getCommand()
                    : "";
            if (message == null) message = "";
            messageField.setText(message);
            actionField.setText(action);
            MessageEntry page = new MessageEntry(this, actionField, messageField, state);
            entries.add(page);
        }
        int x = parent.width / 2 - 124;
        int y = parent.height / 2 - 60;
        list = new ScrollGui(mc, 248, 125, 25, x, y, entries);
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
                ((MessageEntry) list.getListEntry(i)).message.setFocused(false);
                ((MessageEntry) list.getListEntry(i)).action.setFocused(false);
            }
            else
            {
                ((MessageEntry) list.getListEntry(i)).message.mouseClicked(mouseX, mouseY, mouseButton);
                ((MessageEntry) list.getListEntry(i)).action.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        for (int i = 0; i < list.getSize(); i++)
        {
            ((MessageEntry) list.getListEntry(i)).keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.message"), x, y, 0xFFFFFFFF);
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

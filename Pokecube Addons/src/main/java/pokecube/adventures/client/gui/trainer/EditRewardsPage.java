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
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;

public class EditRewardsPage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class RewardEntry implements IGuiListEntry
    {
        final int             index;
        final GuiTextField    reward;
        final EditRewardsPage parent;

        public RewardEntry(EditRewardsPage parent, ItemStack stack, int index)
        {
            this.index = index;
            this.reward = new GuiTextField(0, parent.fontRenderer, 0, 0, 240, 10);
            reward.setMaxStringLength(Short.MAX_VALUE);
            this.parent = parent;
            if (!stack.isEmpty())
            {
                String value = stack.getItem().getRegistryName() + " " + stack.getCount() + " " + stack.getMetadata();
                if (stack.hasTagCompound())
                {
                    value = value + " " + stack.getTagCompound().toString();
                }
                reward.setText(value);
            }
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            boolean fits = true;
            reward.x = x - 2;
            reward.y = y - 4;
            fits = reward.y >= offsetY;
            fits = fits && reward.y + reward.height <= offsetY + guiHeight;
            if (fits)
            {
                reward.drawTextBox();
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            boolean fits = true;
            fits = reward.y >= offsetY;
            fits = fits && mouseX - reward.x >= 0;
            fits = fits && mouseX - reward.x <= reward.width;
            fits = fits && reward.y + reward.height <= offsetY + guiHeight;
            reward.setFocused(fits);
            return fits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }

        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            reward.textboxKeyTyped(typedChar, keyCode);
            if (!reward.isFocused()) return;
            if (keyCode == Keyboard.KEY_RETURN)
            {
                if (reward.getText().isEmpty())
                {
                    if (index != -1)
                    {
                        ItemStack removed = parent.parent.rewards.getRewards().remove(index);
                        parent.mc.player.sendStatusMessage(new TextComponentTranslation("traineredit.set.removereward",
                                removed.getTextComponent()), true);
                        parent.onPageClosed();
                        PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                        NBTBase tag = CapabilityHasRewards.storage.writeNBT(CapabilityHasRewards.REWARDS_CAP,
                                parent.parent.rewards, null);
                        packet.data.setTag("T", tag);
                        packet.data.setByte("V", (byte) 1);
                        packet.data.setInteger("I", parent.parent.entity.getEntityId());
                        PokecubeMod.packetPipeline.sendToServer(packet);
                        parent.onPageOpened();
                    }
                    return;
                }

                String[] args = reward.getText().split(" ");

                try
                {
                    Item item = CommandBase.getItemByText(parent.mc.player, args[0]);
                    int i = 1;
                    int j = args.length >= 3 ? CommandBase.parseInt(args[2].trim()) : 0;
                    ItemStack itemstack = new ItemStack(item, i, j);
                    if (args.length >= 4)
                    {
                        String s = CommandBase.buildString(args, 3);

                        try
                        {
                            itemstack.setTagCompound(JsonToNBT.getTagFromJson(s));
                        }
                        catch (NBTException nbtexception)
                        {
                            throw new CommandException("commands.give.tagError",
                                    new Object[] { nbtexception.getMessage() });
                        }
                    }
                    if (args.length >= 2)
                        itemstack.setCount(CommandBase.parseInt(args[1].trim(), 1, item.getItemStackLimit(itemstack)));
                    if (itemstack.isEmpty())
                    {
                        parent.mc.player.sendStatusMessage(
                                new TextComponentTranslation("traineredit.info.invalidreward"), true);
                    }
                    else
                    {
                        if (index != -1)
                        {
                            parent.parent.rewards.getRewards().set(index, itemstack);
                            parent.mc.player.sendStatusMessage(new TextComponentTranslation("traineredit.set.reward",
                                    itemstack.getTextComponent()), true);
                            parent.onPageClosed();
                            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                            NBTBase tag = CapabilityHasRewards.storage.writeNBT(CapabilityHasRewards.REWARDS_CAP,
                                    parent.parent.rewards, null);
                            packet.data.setTag("T", tag);
                            packet.data.setByte("V", (byte) 1);
                            packet.data.setInteger("I", parent.parent.entity.getEntityId());
                            PokecubeMod.packetPipeline.sendToServer(packet);
                            parent.onPageOpened();
                        }
                        else
                        {
                            parent.parent.rewards.getRewards().add(itemstack);
                            parent.mc.player.sendStatusMessage(new TextComponentTranslation("traineredit.set.rewardnew",
                                    itemstack.getTextComponent()), true);
                            parent.onPageClosed();
                            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                            NBTBase tag = CapabilityHasRewards.storage.writeNBT(CapabilityHasRewards.REWARDS_CAP,
                                    parent.parent.rewards, null);
                            packet.data.setTag("T", tag);
                            packet.data.setByte("V", (byte) 1);
                            packet.data.setInteger("I", parent.parent.entity.getEntityId());
                            PokecubeMod.packetPipeline.sendToServer(packet);
                            parent.onPageOpened();
                        }
                    }
                }
                catch (Exception e)
                {
                    parent.mc.player.sendStatusMessage(new TextComponentTranslation("traineredit.info.invalidreward"),
                            true);
                }

            }
        }

    }

    private List<RewardEntry> rewards = Lists.newArrayList();

    public EditRewardsPage(GuiEditTrainer watch)
    {
        super(watch);
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    void initList()
    {
        rewards.clear();
        List<ItemStack> rewardStacks = parent.rewards.getRewards();
        int i = 0;
        for (ItemStack stack : rewardStacks)
        {
            rewards.add(new RewardEntry(this, stack, i++));
        }
        rewards.add(new RewardEntry(this, ItemStack.EMPTY, -1));
        List<IGuiListEntry> entries = Lists.newArrayList(rewards);
        int x = parent.width / 2 - 122;
        int y = parent.height / 2 - 60;
        list = new ScrollGui(mc, 248, 120, 10, x, y, entries);
    }

    @Override
    protected void onPageOpened()
    {
        int x = parent.width / 2;
        int y = parent.height / 2;
        // Init buttons
        String home = I18n.format("traineredit.button.home");
        parent.getButtons().add(new Button(0, x - 25, y + 64, 50, 12, home));
        initList();
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
                rewards.get(i).reward.setFocused(false);
            }
            else
            {
                rewards.get(i).reward.mouseClicked(mouseX, mouseY, mouseButton);
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
        List<RewardEntry> list = Lists.newArrayList(rewards);
        for (RewardEntry entry : list)
            entry.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.rewards"), x, y, 0xFFFFFFFF);
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

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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.events.PAEventsHandler;
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
        final GuiTextField    chance;
        final EditRewardsPage parent;

        public RewardEntry(EditRewardsPage parent, Reward reward2, int index)
        {
            this.index = index;
            this.reward = new GuiTextField(0, parent.fontRenderer, 0, 0, 200, 10);
            reward.setMaxStringLength(Short.MAX_VALUE);
            com.google.common.base.Predicate<String> floatValid = new com.google.common.base.Predicate<String>()
            {
                @Override
                public boolean apply(String input)
                {
                    try
                    {
                        float var = Float.parseFloat(input);
                        return var <= 1 && var >= 0;
                    }
                    catch (NumberFormatException e)
                    {
                        return input.isEmpty();
                    }
                }
            };
            this.chance = new GuiTextField(1, parent.fontRenderer, 200, 0, 40, 10);
            this.chance.setValidator(floatValid);
            this.parent = parent;
            if (reward2 != null && !reward2.stack.isEmpty())
            {
                ItemStack stack = reward2.stack;
                String value = stack.getItem().getRegistryName() + " " + stack.getCount() + " " + stack.getMetadata();
                if (stack.hasTagCompound())
                {
                    value = value + " " + stack.getTagCompound().toString();
                }
                reward.setText(value);
            }
            if (reward2 != null) this.chance.setText(reward2.chance + "");
            else this.chance.setText("1.0");
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
            chance.x = x + 198;
            chance.y = y - 4;
            fits = reward.y >= offsetY;
            fits = fits && reward.y + reward.height <= offsetY + guiHeight;
            if (fits)
            {
                reward.drawTextBox();
                chance.drawTextBox();
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

            boolean fits2 = chance.y >= offsetY;
            fits2 = fits2 && mouseX - chance.x >= 0;
            fits2 = fits2 && mouseX - chance.x <= chance.width;
            fits2 = fits2 && chance.y + chance.height <= offsetY + guiHeight;
            chance.setFocused(fits2);

            return fits || fits2;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }

        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            reward.textboxKeyTyped(typedChar, keyCode);
            chance.textboxKeyTyped(typedChar, keyCode);
            if (reward.isFocused() || chance.isFocused())
            {
                if (keyCode == Keyboard.KEY_RETURN)
                {

                    float prob = 1;
                    try
                    {
                        prob = Float.valueOf(chance.getText());
                    }
                    catch (NumberFormatException e1)
                    {
                        chance.setText("1.0");
                    }

                    if (reward.getText().isEmpty())
                    {
                        if (index != -1)
                        {
                            Reward rreward = parent.parent.rewards.getRewards().remove(index);
                            ItemStack removed = rreward.stack;
                            parent.mc.player.sendStatusMessage(new TextComponentTranslation(
                                    "traineredit.set.removereward", removed.getTextComponent()), true);
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
                    try
                    {
                        ItemStack itemstack = PAEventsHandler.fromString(reward.getText(), parent.mc.player);
                        if (itemstack.isEmpty())
                        {
                            parent.mc.player.sendStatusMessage(
                                    new TextComponentTranslation("traineredit.info.invalidreward"), true);
                        }
                        else
                        {
                            if (index != -1)
                            {
                                parent.parent.rewards.getRewards().set(index, new Reward(itemstack, prob));
                                parent.mc.player.sendStatusMessage(new TextComponentTranslation(
                                        "traineredit.set.reward", itemstack.getTextComponent()), true);
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
                                parent.parent.rewards.getRewards().add(new Reward(itemstack, prob));
                                parent.mc.player.sendStatusMessage(new TextComponentTranslation(
                                        "traineredit.set.rewardnew", itemstack.getTextComponent()), true);
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
                        parent.mc.player.sendStatusMessage(
                                new TextComponentTranslation("traineredit.info.invalidreward"), true);
                    }

                }
            }
        }

    }

    private List<RewardEntry> rewards = Lists.newArrayList();
    protected final int       index;

    public EditRewardsPage(GuiEditTrainer watch, int index)
    {
        super(watch);
        this.index = index;
    }

    @Override
    protected void initList()
    {
        rewards.clear();
        List<Reward> rewardStacks = parent.rewards.getRewards();
        int i = 0;
        for (Reward reward : rewardStacks)
        {
            rewards.add(new RewardEntry(this, reward, i++));
        }
        rewards.add(new RewardEntry(this, null, -1));
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
                rewards.get(i).chance.setFocused(false);
            }
            else
            {
                rewards.get(i).reward.mouseClicked(mouseX, mouseY, mouseButton);
                rewards.get(i).chance.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
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

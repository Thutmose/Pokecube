package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;

public class EditTradesPage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class TradeEntry implements IGuiListEntry
    {
        final int            index;
        final GuiTextField   buy1;
        final GuiTextField   buy2;
        final GuiTextField   sell;
        final EditTradesPage parent;
        final GuiButton      delete;
        final GuiButton      confirm;
        final GuiButton      moveUp;
        final GuiButton      moveDown;
        ItemStack            stack1 = ItemStack.EMPTY;
        ItemStack            stack2 = ItemStack.EMPTY;
        ItemStack            stacks = ItemStack.EMPTY;

        public TradeEntry(int index, EditTradesPage parent, GuiTextField buy1, GuiTextField buy2, GuiTextField sell)
        {
            this.parent = parent;
            this.buy1 = buy1;
            this.buy2 = buy2;
            this.sell = sell;
            this.index = index;
            delete = new GuiButton(0, 0, 0, 10, 10, "x");
            delete.packedFGColour = 0xFFFF0000;
            confirm = new GuiButton(0, 0, 0, 10, 10, "Y");
            confirm.enabled = false;
            moveUp = new GuiButton(0, 0, 0, 10, 10, "\u21e7");
            moveDown = new GuiButton(0, 0, 0, 10, 10, "\u21e9");
            moveUp.enabled = index > 0 && index < parent.num;
            moveDown.enabled = index < parent.num - 1;
            populateStacks();
        }

        private void populateStacks()
        {
            stack1 = ItemStack.EMPTY;
            stack2 = ItemStack.EMPTY;
            stacks = ItemStack.EMPTY;
            try
            {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(buy1.getText());
                stack1 = new ItemStack(tag);
            }
            catch (NBTException e)
            {
                try
                {
                    stack1 = PAEventsHandler.fromString(buy1.getText(), parent.mc.player);
                }
                catch (CommandException e1)
                {
                }
            }
            try
            {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(buy2.getText());
                stack2 = new ItemStack(tag);
            }
            catch (NBTException e)
            {
                try
                {
                    stack2 = PAEventsHandler.fromString(buy2.getText(), parent.mc.player);
                }
                catch (CommandException e1)
                {
                }
            }
            try
            {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(sell.getText());
                stacks = new ItemStack(tag);
            }
            catch (NBTException e)
            {
                try
                {
                    stacks = PAEventsHandler.fromString(sell.getText(), parent.mc.player);
                }
                catch (CommandException e1)
                {
                }
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
            buy1.x = x - 2 + 60;
            buy1.y = y - 4;
            buy2.y = y + 7;
            buy2.x = x - 2 + 60;
            sell.y = y + 18;
            sell.x = x - 2 + 60;
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 120;
            fits = buy1.y >= offsetY;
            fits = fits && buy1.y + 2 * buy1.height <= offsetY + guiHeight;
            if (fits)
            {
                RenderHelper.disableStandardItemLighting();
                buy1.drawTextBox();
                buy2.drawTextBox();
                sell.drawTextBox();

                delete.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                confirm.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                moveUp.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                moveDown.drawButton(parent.mc, mouseX, mouseY, partialTicks);
                GL11.glColor3f(1, 1, 1);

                int dx = buy1.x - 60;
                int dy = buy1.y + 12;
                int mx = mouseX - dx;
                int my = mouseY - dy;

                Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
                minecraft.renderEngine.bindTexture(new ResourceLocation(PokecubeAdv.ID, "textures/gui/arrow.png"));
                parent.drawTexturedModalRect(dx + 28, dy, 0, 0, 16, 16);

                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                this.parent.mc.getRenderItem().zLevel = 100;

                if (!stack1.isEmpty())
                {
                    this.parent.mc.getRenderItem().renderItemAndEffectIntoGUI(stack1, dx, dy);
                    this.parent.mc.getRenderItem().renderItemOverlays(this.parent.mc.fontRenderer, stack1, dx, dy);
                }
                if (!stack2.isEmpty())
                {
                    this.parent.mc.getRenderItem().renderItemAndEffectIntoGUI(stack2, dx + 16, dy);
                    this.parent.mc.getRenderItem().renderItemOverlays(this.parent.mc.fontRenderer, stack2, dx + 16, dy);
                }
                if (!stacks.isEmpty())
                {
                    this.parent.mc.getRenderItem().renderItemIntoGUI(stacks, dx + 42, dy);
                    this.parent.mc.getRenderItem().renderItemOverlays(this.parent.mc.fontRenderer, stacks, dx + 42, dy);
                }
                this.parent.mc.getRenderItem().zLevel = 0;

                int tx = 0;
                int ty = mouseY - 30;
                int tz = 0;
                GL11.glTranslated(tx, ty, tz);
                if (!stack1.isEmpty()) if (mx > 0 && mx < 17 && my > 0 && my < 17)
                {
                    net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack1);
                    List<String> text = parent.getItemToolTip(stack1);
                    this.parent.drawHoveringText(text, mouseX, 0);
                    net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
                }
                if (!stack2.isEmpty()) if (mx > 16 && mx < 32 && my > 0 && my < 17)
                {
                    net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack2);
                    List<String> text = parent.getItemToolTip(stack2);
                    this.parent.drawHoveringText(text, mouseX, 0);
                    net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
                }
                if (!stacks.isEmpty()) if (mx > 43 && mx < 59 && my > 0 && my < 17)
                {
                    net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stacks);
                    List<String> text = parent.getItemToolTip(stacks);
                    this.parent.drawHoveringText(text, mouseX, 0);
                    net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
                }
                GL11.glTranslated(-tx, -ty, -tz);

                GlStateManager.popMatrix();
                GlStateManager.enableDepth();
                RenderHelper.disableStandardItemLighting();

            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            int offsetY = parent.parent.height / 2 - 60;
            int guiHeight = 100;
            boolean buy1Fits = true;
            buy1Fits = buy1.y >= offsetY;
            buy1Fits = buy1Fits && mouseX - buy1.x >= 0;
            buy1Fits = buy1Fits && mouseX - buy1.x <= buy1.width;
            buy1Fits = buy1Fits && mouseY - buy1.y >= 0;
            buy1Fits = buy1Fits && mouseY - buy1.y <= buy1.height;
            buy1Fits = buy1Fits && buy1.y + buy1.height <= offsetY + guiHeight;
            buy1.setFocused(buy1Fits);
            boolean buy2Fits = true;
            buy2Fits = buy2.y >= offsetY;
            buy2Fits = buy2Fits && mouseX - buy2.x >= 0;
            buy2Fits = buy2Fits && mouseX - buy2.x <= buy2.width;
            buy2Fits = buy2Fits && mouseY - buy2.y >= 0;
            buy2Fits = buy2Fits && mouseY - buy2.y <= buy2.height;
            buy2Fits = buy2Fits && buy2.y + buy2.height <= offsetY + guiHeight;
            buy2.setFocused(buy2Fits);
            boolean sellFits = true;
            sellFits = sell.y >= offsetY;
            sellFits = sellFits && mouseX - sell.x >= 0;
            sellFits = sellFits && mouseX - sell.x <= sell.width;
            sellFits = sellFits && mouseY - sell.y >= 0;
            sellFits = sellFits && mouseY - sell.y <= sell.height;
            sellFits = sellFits && sell.y + sell.height <= offsetY + guiHeight;
            sell.setFocused(sellFits);

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
            buy1.textboxKeyTyped(typedChar, keyCode);
            buy2.textboxKeyTyped(typedChar, keyCode);
            sell.textboxKeyTyped(typedChar, keyCode);
            populateStacks();
            if (keyCode != Keyboard.KEY_RETURN) return;
            if (!(buy1.isFocused() || buy2.isFocused() || sell.isFocused())) return;
            update();
        }

        private void delete()
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("TR", true);
            tag.setInteger("I", index);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.parent.entity.getEntityId());
            Entity mob = parent.parent.entity;
            NBTTagCompound tag2 = new NBTTagCompound();
            mob.writeToNBT(tag2);
            MerchantRecipeList list = new MerchantRecipeList(tag2.getCompoundTag("Offers"));
            if (index < list.size()) list.remove(index);
            tag2.setTag("Offers", list.getRecipiesAsTags());
            mob.readFromNBT(tag2);
            parent.onPageClosed();
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.onPageOpened();
            parent.initList();
        }

        private void reOrder(int dir)
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("TR", true);
            tag.setInteger("I", index);
            tag.setInteger("N", dir);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.parent.entity.getEntityId());
            Entity mob = parent.parent.entity;
            NBTTagCompound tag2 = new NBTTagCompound();
            mob.writeToNBT(tag2);
            MerchantRecipeList list = new MerchantRecipeList(tag2.getCompoundTag("Offers"));
            int index1 = tag.getInteger("I");
            int index2 = index1 + tag.getInteger("N");
            MerchantRecipe temp = list.get(index1);
            list.set(index1, list.get(index2));
            list.set(index2, temp);
            tag2.setTag("Offers", list.getRecipiesAsTags());
            mob.readFromNBT(tag2);
            parent.onPageClosed();
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.onPageOpened();
            parent.initList();
        }

        private void update()
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);

            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("TR", true);
            tag.setInteger("I", index);
            // Delete in this case.
            if (buy1.getText().isEmpty())
            {
                // Default behavior for malformed packet is delete tag, so don't
                // do anything here.
            }
            // Otherwise validate all stacks, and update trade if valid.
            else
            {
                // Needs at least buy1 and sell
                if (!stack1.isEmpty() || stacks.isEmpty())
                {
                    MerchantRecipe recipe = new MerchantRecipe(stack1, stack2, stacks, 0, 65);
                    NBTTagCompound tag1 = recipe.writeToTags();
                    tag.setTag("R", tag1);
                }
            }
            if (tag.hasKey("R"))
            {
                packet.data.setTag("T", tag);
                packet.data.setInteger("I", parent.parent.entity.getEntityId());
                Entity mob = parent.parent.entity;
                NBTTagCompound tag2 = new NBTTagCompound();
                mob.writeToNBT(tag2);
                MerchantRecipeList list = new MerchantRecipeList(tag2.getCompoundTag("Offers"));
                if (tag.hasKey("R"))
                {
                    MerchantRecipe recipe = new MerchantRecipe(tag.getCompoundTag("R"));
                    if (index < list.size()) list.set(index, recipe);
                    else list.add(recipe);
                }
                tag2.setTag("Offers", list.getRecipiesAsTags());
                mob.readFromNBT(tag2);
                parent.onPageClosed();
                PokecubeMod.packetPipeline.sendToServer(packet);
                parent.onPageOpened();
                parent.initList();
            }
        }

    }

    protected final int index;
    protected int       num;

    public EditTradesPage(GuiEditTrainer watch, int index)
    {
        super(watch);
        this.index = index;
    }

    @Override
    protected void initList()
    {
        List<IGuiListEntry> entries = Lists.newArrayList();

        NBTTagCompound tag = new NBTTagCompound();
        this.parent.entity.writeToNBT(tag);
        MerchantRecipeList recipes = new MerchantRecipeList(tag.getCompoundTag("Offers"));
        this.num = recipes.size();
        int num = 0;
        int width = 180;
        if (recipes != null)
        {
            for (MerchantRecipe recipe : recipes)
            {
                GuiTextField buy1 = new GuiTextField(0, fontRenderer, 0, 0, width, 10);
                GuiTextField buy2 = new GuiTextField(1, fontRenderer, 0, 0, width, 10);
                GuiTextField sell = new GuiTextField(2, fontRenderer, 0, 0, width, 10);
                buy1.setMaxStringLength(Short.MAX_VALUE);
                buy2.setMaxStringLength(Short.MAX_VALUE);
                sell.setMaxStringLength(Short.MAX_VALUE);
                String value = recipe.getItemToBuy().getItem().getRegistryName() + " "
                        + recipe.getItemToBuy().getCount() + " " + recipe.getItemToBuy().getMetadata();
                if (recipe.getItemToBuy().hasTagCompound())
                {
                    value = value + " " + recipe.getItemToBuy().getTagCompound().toString();
                }
                buy1.setText(value);
                if (recipe.hasSecondItemToBuy())
                {
                    ItemStack stack = recipe.getSecondItemToBuy();
                    value = stack.getItem().getRegistryName() + " " + stack.getCount() + " " + stack.getMetadata();
                    if (stack.hasTagCompound())
                    {
                        value = value + " " + stack.getTagCompound().toString();
                    }
                    buy2.setText(value);
                }
                ItemStack stack = recipe.getItemToSell();
                value = stack.getItem().getRegistryName() + " " + stack.getCount() + " " + stack.getMetadata();
                if (stack.hasTagCompound())
                {
                    value = value + " " + stack.getTagCompound().toString();
                }
                sell.setText(value);
                buy1.moveCursorBy(-buy1.getCursorPosition());
                buy2.moveCursorBy(-buy2.getCursorPosition());
                sell.moveCursorBy(-sell.getCursorPosition());
                TradeEntry entry = new TradeEntry(num++, this, buy1, buy2, sell);
                entries.add(entry);
            }
        }
        // Blank value.
        GuiTextField buy1 = new GuiTextField(0, fontRenderer, 0, 0, width, 10);
        GuiTextField buy2 = new GuiTextField(1, fontRenderer, 0, 0, width, 10);
        GuiTextField sell = new GuiTextField(2, fontRenderer, 0, 0, width, 10);
        buy1.setMaxStringLength(Short.MAX_VALUE);
        buy2.setMaxStringLength(Short.MAX_VALUE);
        sell.setMaxStringLength(Short.MAX_VALUE);
        TradeEntry entry = new TradeEntry(num++, this, buy1, buy2, sell);
        entries.add(entry);
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int slot = list.getSlotIndexFromScreenCoords(mouseX, mouseY);
        for (int i = 0; i < list.getSize(); i++)
        {
            if (i != slot)
            {
                ((TradeEntry) list.getListEntry(i)).buy1.setFocused(false);
                ((TradeEntry) list.getListEntry(i)).buy2.setFocused(false);
                ((TradeEntry) list.getListEntry(i)).sell.setFocused(false);
            }
            else
            {
                ((TradeEntry) list.getListEntry(i)).buy1.mouseClicked(mouseX, mouseY, mouseButton);
                ((TradeEntry) list.getListEntry(i)).buy2.mouseClicked(mouseX, mouseY, mouseButton);
                ((TradeEntry) list.getListEntry(i)).sell.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        for (int i = 0; i < list.getSize(); i++)
        {
            ((TradeEntry) list.getListEntry(i)).keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.trades"), x, y, 0xFFFFFFFF);
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

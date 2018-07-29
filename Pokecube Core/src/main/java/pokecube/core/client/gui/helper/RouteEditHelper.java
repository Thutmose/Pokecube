package pokecube.core.client.gui.helper;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.ai.properties.GuardAICapability.GuardTask;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability.IGuardTask;
import pokecube.core.utils.TimePeriod;
import thut.api.network.PacketHandler;

public class RouteEditHelper
{

    public static class GuardEntry implements IGuiListEntry
    {
        final int                                      index;
        public final GuiTextField                      location;
        public final GuiTextField                      timeperiod;
        public final GuiTextField                      variation;
        final GuiScreen                                parent;
        final IGuardAICapability                       guard;
        final Entity                                   entity;
        final GuiButton                                delete;
        final GuiButton                                confirm;
        final GuiButton                                moveUp;
        final GuiButton                                moveDown;
        final Function<NBTTagCompound, NBTTagCompound> function;
        final int                                      guiX;
        final int                                      guiY;
        final int                                      guiHeight;

        public GuardEntry(int index, IGuardAICapability guard, Entity entity, GuiScreen parent, GuiTextField location,
                GuiTextField timeperiod, GuiTextField variation, Function<NBTTagCompound, NBTTagCompound> function,
                int dx, int dy, int dh)
        {
            this.guard = guard;
            this.parent = parent;
            this.location = location;
            this.timeperiod = timeperiod;
            this.variation = variation;
            this.index = index;
            this.entity = entity;
            delete = new GuiButton(0, 0, 0, 10, 10, "x");
            delete.packedFGColour = 0xFFFF0000;
            confirm = new GuiButton(0, 0, 0, 10, 10, "Y");
            confirm.enabled = false;
            moveUp = new GuiButton(0, 0, 0, 10, 10, "\u21e7");
            moveDown = new GuiButton(0, 0, 0, 10, 10, "\u21e9");
            moveUp.enabled = index > 0 && index < guard.getTasks().size();
            moveDown.enabled = index < guard.getTasks().size() - 1;
            this.function = function;
            this.guiX = dx;
            this.guiY = dy;
            this.guiHeight = dh;
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
            int dx = 4 + guiX;
            int dy = -5 + guiY;
            delete.y = y + dy;
            delete.x = x - dx + width;
            confirm.y = y + dy;
            confirm.x = x - dx + 10 + width;
            moveUp.y = y + dy + width;
            moveUp.x = x - dx + width;
            moveDown.y = y + dy + width;
            moveDown.x = x - dx + 10 + width;
            dx += 26;
            dy += 1;
            boolean fits = true;
            location.x = x - 2 + dx;
            location.y = y + dy;
            timeperiod.y = y + dy + 10;
            timeperiod.x = x - 2 + dx;
            variation.y = y + dy + 20;
            variation.x = x - 2 + dx;
            dy = -60;
            int offsetY = parent.height / 2 - guiY + dy;
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
            int offsetY = parent.height / 2 - 60;
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

        public void keyTyped(char typedChar, int keyCode) throws IOException
        {
            location.textboxKeyTyped(typedChar, keyCode);
            timeperiod.textboxKeyTyped(typedChar, keyCode);
            variation.textboxKeyTyped(typedChar, keyCode);

            if (keyCode == Keyboard.KEY_TAB)
            {
                if (location.isFocused())
                {
                    location.setFocused(false);
                    timeperiod.setFocused(true);
                }
                else if (timeperiod.isFocused())
                {
                    timeperiod.setFocused(false);
                    variation.setFocused(true);
                }
                else if (variation.isFocused())
                {
                    variation.setFocused(false);
                    location.setFocused(true);
                }
            }
            if (keyCode != Keyboard.KEY_RETURN) return;
            if (!(location.isFocused() || timeperiod.isFocused() || variation.isFocused())) return;
            update();
        }

        private void delete()
        {
            NBTTagCompound data = new NBTTagCompound();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("GU", true);
            tag.setInteger("I", index);
            data.setTag("T", tag);
            data.setInteger("I", entity.getEntityId());
            if (index < guard.getTasks().size()) guard.getTasks().remove(index);
            function.apply(data);
        }

        private void reOrder(int dir)
        {
            NBTTagCompound data = new NBTTagCompound();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("GU", true);
            tag.setInteger("I", index);
            tag.setInteger("N", dir);
            data.setTag("T", tag);
            data.setInteger("I", entity.getEntityId());
            int index1 = tag.getInteger("I");
            int index2 = index1 + tag.getInteger("N");
            IGuardTask temp = guard.getTasks().get(index1);
            guard.getTasks().set(index1, guard.getTasks().get(index2));
            guard.getTasks().set(index2, temp);
            function.apply(data);
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
                NBTTagCompound data = new NBTTagCompound();
                NBTTagCompound tag = new NBTTagCompound();
                tag.setBoolean("GU", true);
                tag.setInteger("I", index);
                // TODO generalize this maybe?
                IGuardTask task = index < guard.getTasks().size() ? guard.getTasks().get(index)
                        : new GuardAICapability.GuardTask();
                if (index >= guard.getTasks().size()) guard.getTasks().add(task);
                task.setPos(loc);
                task.setActiveTime(time);
                task.setRoamDistance(dist);
                NBTBase var = task.serialze();
                tag.setTag("V", var);
                data.setTag("T", tag);
                data.setInteger("I", entity.getEntityId());
                function.apply(data);
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

    public static void applyServerPacket(NBTBase tag, Entity mob, IGuardAICapability guard)
    {
        NBTTagCompound nbt = ((NBTTagCompound) tag);
        int index = nbt.getInteger("I");
        if (nbt.hasKey("V"))
        {
            // TODO generalize this maybe?
            GuardTask task = new GuardTask();
            task.load(nbt.getTag("V"));
            if (index < guard.getTasks().size()) guard.getTasks().set(index, task);
            else guard.getTasks().add(task);
        }
        else
        {
            if (nbt.hasKey("N"))
            {
                int index1 = nbt.getInteger("I");
                int index2 = index1 + nbt.getInteger("N");
                IGuardTask temp = guard.getTasks().get(index1);
                guard.getTasks().set(index1, guard.getTasks().get(index2));
                guard.getTasks().set(index2, temp);
            }
            else if (index < guard.getTasks().size()) guard.getTasks().remove(index);
        }
        PacketHandler.sendEntityUpdate(mob);
    }

    public static void getGuiList(List<IGuiListEntry> entries, IGuardAICapability guard,
            Function<NBTTagCompound, NBTTagCompound> function, Entity entity, GuiScreen parent, int width, int dx,
            int dy, int height)
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int num = 0;
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
            GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx, dy,
                    height);
            entries.add(entry);
        }
        // Blank value.
        GuiTextField location = new GuiTextField(0, fontRenderer, 0, 0, width, 10);
        GuiTextField time = new GuiTextField(1, fontRenderer, 0, 0, width, 10);
        GuiTextField dist = new GuiTextField(2, fontRenderer, 0, 0, width, 10);
        location.setMaxStringLength(Short.MAX_VALUE);
        time.setMaxStringLength(Short.MAX_VALUE);
        dist.setMaxStringLength(Short.MAX_VALUE);
        GuardEntry entry = new GuardEntry(num++, guard, entity, parent, location, time, dist, function, dx, dy, height);
        entries.add(entry);
    }

}

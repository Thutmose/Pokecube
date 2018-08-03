package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer.Page;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs.LevelMode;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.TimePeriod;

public class EditAIPage extends Page
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    protected final int index;

    public EditAIPage(GuiEditTrainer watch, int index)
    {
        super(watch);
        this.index = index;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = parent.width / 2;
        int y = parent.height / 2;
        int dx = 30;
        textList.add(new GuiTextField(0, fontRenderer, x + dx, y - 56, 30, 10));
        textList.add(new GuiTextField(1, fontRenderer, x + dx, y - 45, 30, 10));
        textList.add(new GuiTextField(2, fontRenderer, x + dx + 50, y - 45, 30, 10));
        dx = 20;
        textList.add(new GuiTextField(3, fontRenderer, x + dx, y - 20, 60, 10));
        textList.add(new GuiTextField(4, fontRenderer, x + dx, y, 60, 10));

        textList.add(new GuiTextField(5, fontRenderer, x + dx - 78, y + 29, 30, 10));
    }

    @Override
    protected void onPageOpened()
    {
        IGuardAICapability guard = parent.entity.getCapability(EventsHandler.GUARDAI_CAP, null);
        com.google.common.base.Predicate<String> floatValid = new com.google.common.base.Predicate<String>()
        {
            @Override
            public boolean apply(String input)
            {
                try
                {
                    Float.parseFloat(input);
                    return true;
                }
                catch (NumberFormatException e)
                {
                    return input.isEmpty();
                }
            }
        };
        com.google.common.base.Predicate<String> intValid = new com.google.common.base.Predicate<String>()
        {
            @Override
            public boolean apply(String input)
            {
                try
                {
                    Integer.parseInt(input);
                    return true;
                }
                catch (NumberFormatException e)
                {
                    return input.isEmpty();
                }
            }
        };
        int x = parent.width / 2;
        int y = parent.height / 2;
        // Init buttons
        String home = I18n.format("traineredit.button.home");
        parent.getButtons().add(new Button(0, x - 25, y + 64, 50, 12, home));
        String wanderbutton = parent.aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                ? I18n.format("traineredit.button.stationary") : I18n.format("traineredit.button.wander");
        parent.getButtons().add(new Button(1, x - 120, y - 64, 60, 12, wanderbutton));

        String rotateButton = parent.aiStates.getAIState(IHasNPCAIStates.FIXEDDIRECTION)
                ? I18n.format("traineredit.button.norotates") : I18n.format("traineredit.button.rotates");
        parent.getButtons().add(new Button(2, x - 120, y + 27, 60, 12, rotateButton));

        if (parent.trainer instanceof DefaultPokemobs)
        {
            String visible = ((DefaultPokemobs) parent.trainer).notifyDefeat ? I18n.format("traineredit.button.notify")
                    : I18n.format("traineredit.button.nonotify");
            parent.getButtons().add(new Button(3, x - 120, y + 39, 60, 12, visible));
        }

        String friendlyButton = parent.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY)
                ? I18n.format("traineredit.button.friendly") : I18n.format("traineredit.button.unfriendly");
        parent.getButtons().add(new Button(4, x - 120, y - 76, 60, 12, friendlyButton));

        String levelsButton = I18n.format("traineredit.button.levels." + parent.trainer.getLevelMode());
        parent.getButtons().add(new Button(5, x + 60, y + 44, 60, 12, levelsButton));
        String button = parent.aiStates.getAIState(IHasNPCAIStates.MATES) ? I18n.format("traineredit.button.mates")
                : I18n.format("traineredit.button.nomates");
        parent.getButtons().add(new Button(6, x - 120, y + 51, 60, 12, button));
        button = parent.aiStates.getAIState(IHasNPCAIStates.INVULNERABLE)
                ? I18n.format("traineredit.button.invulnerable") : I18n.format("traineredit.button.vulnerable");
        parent.getButtons().add(new Button(7, x - 120, y + 63, 60, 12, button));
        button = parent.aiStates.getAIState(IHasNPCAIStates.TRADES) ? I18n.format("traineredit.button.trade")
                : I18n.format("traineredit.button.notrade");
        parent.getButtons().add(new Button(8, x - 120, y + 15, 60, 12, button));

        textList.get(0).setValidator(floatValid);
        textList.get(0).setText(guard.getPrimaryTask().getRoamDistance() + "");

        TimePeriod times = guard.getPrimaryTask().getActiveTime();
        if (times == null)
        {
            times = new TimePeriod(0, 0);
        }
        times = new TimePeriod(((int) (times.startTime * 1000)) / 1000d, ((int) (times.endTime * 1000)) / 1000d);
        textList.get(1).setValidator(floatValid);
        textList.get(1).setText(times.startTime + "");
        textList.get(2).setValidator(floatValid);
        textList.get(2).setText(times.endTime + "");

        textList.get(3).setValidator(intValid);
        textList.get(3).setText(((DefaultPokemobs) parent.trainer).resetTime + "");

        textList.get(4).setValidator(intValid);
        textList.get(4).setText(((DefaultPokemobs) parent.trainer).battleCooldown + "");

        textList.get(5).setValidator(floatValid);
        textList.get(5).setText(parent.aiStates.getDirection() + "");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        ITextComponent mess;
        PacketTrainer packet;
        NBTBase tag;
        DefaultPokemobs trainer = (DefaultPokemobs) parent.trainer;
        switch (button.id)
        {
        case 0:
            parent.setIndex(0);
            break;
        case 1:
            parent.aiStates.setAIState(IHasNPCAIStates.STATIONARY,
                    !parent.aiStates.getAIState(IHasNPCAIStates.STATIONARY));

            IGuardAICapability guard = parent.entity.getCapability(EventsHandler.GUARDAI_CAP, null);
            guard.getPrimaryTask().setPos(parent.entity.getPosition());
            guard.getPrimaryTask().setActiveTime(!parent.aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                    ? new TimePeriod(0, 0) : TimePeriod.fullDay);
            sendGuardUpdate();
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.stationary." + parent.aiStates.getAIState(IHasNPCAIStates.STATIONARY));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 2:
            parent.aiStates.setAIState(IHasNPCAIStates.FIXEDDIRECTION,
                    !parent.aiStates.getAIState(IHasNPCAIStates.FIXEDDIRECTION));
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.norotates." + parent.aiStates.getAIState(IHasNPCAIStates.FIXEDDIRECTION));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 3:
            trainer.notifyDefeat = !trainer.notifyDefeat;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, parent.trainer, null);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            this.onPageOpened();
            mess = new TextComponentTranslation(
                    "traineredit.set.notify." + parent.aiStates.getAIState(IHasNPCAIStates.FIXEDDIRECTION));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 4:
            parent.aiStates.setAIState(IHasNPCAIStates.PERMFRIENDLY,
                    !parent.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY));
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.friendly." + parent.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 5:
            trainer.setLevelMode(
                    LevelMode.values()[(trainer.getLevelMode().ordinal() + 1) % LevelMode.values().length]);
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, parent.trainer, null);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            this.onPageOpened();
            String levelsButton = I18n.format("traineredit.button.levels." + parent.trainer.getLevelMode());
            mess = new TextComponentTranslation("traineredit.set.levels", levelsButton);
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 6:
            parent.aiStates.setAIState(IHasNPCAIStates.MATES, !parent.aiStates.getAIState(IHasNPCAIStates.MATES));
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.mates." + parent.aiStates.getAIState(IHasNPCAIStates.MATES));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 7:
            parent.aiStates.setAIState(IHasNPCAIStates.INVULNERABLE,
                    !parent.aiStates.getAIState(IHasNPCAIStates.INVULNERABLE));
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.invulnerable." + parent.aiStates.getAIState(IHasNPCAIStates.INVULNERABLE));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 8:
            parent.aiStates.setAIState(IHasNPCAIStates.TRADES, !parent.aiStates.getAIState(IHasNPCAIStates.TRADES));
            sendAIUpdate();
            mess = new TextComponentTranslation(
                    "traineredit.set.trade." + parent.aiStates.getAIState(IHasNPCAIStates.TRADES));
            parent.mc.player.sendStatusMessage(mess, true);
            break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        boolean[] checks = new boolean[textList.size()];
        for (int i = 0; i < textList.size(); i++)
        {
            if (textList.get(i).isFocused()) checks[i] = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (keyCode != Keyboard.KEY_RETURN) return;
        for (int i = 0; i < textList.size(); i++)
        {
            updateField(i);
        }
    }

    private void updateField(int i)
    {
        // TODO differentiate whether the field is the same as it was before.
        GuiTextField field;
        field = textList.get(i);
        if (!field.isFocused()) return;
        String value = field.getText();
        float argFloat;
        int argInt;
        TimePeriod time = null;
        float start, end;
        IGuardAICapability guard = parent.entity.getCapability(EventsHandler.GUARDAI_CAP, null);
        TimePeriod old = guard.getPrimaryTask().getActiveTime();
        if (old == null) old = new TimePeriod(0, 0);
        start = (float) old.startTime;
        end = (float) old.endTime;
        PacketTrainer packet;
        NBTBase tag;
        ITextComponent mess = null;
        switch (i)
        {
        case 0:
            argFloat = value.isEmpty() ? 0 : Float.parseFloat(value);
            guard.getPrimaryTask().setRoamDistance(argFloat);
            mess = new TextComponentTranslation("traineredit.set.guarddist", argFloat);
            sendGuardUpdate();
            break;
        case 1:
            start = value.isEmpty() ? 0 : Float.parseFloat(value);
            end = Float.parseFloat(textList.get(2).getText());
            time = new TimePeriod(start, end);
            guard.getPrimaryTask().setActiveTime(time);
            sendGuardUpdate();
            break;
        case 2:
            end = value.isEmpty() ? 0 : Float.parseFloat(value);
            start = Float.parseFloat(textList.get(1).getText());
            time = new TimePeriod(start, end);
            guard.getPrimaryTask().setActiveTime(time);
            sendGuardUpdate();
            break;
        case 3:
            argInt = value.isEmpty() ? 0 : Integer.parseInt(value);
            ((DefaultPokemobs) parent.trainer).resetTime = argInt;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, parent.trainer, null);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            this.onPageOpened();
            mess = new TextComponentTranslation("traineredit.set.cooldown_p", argInt);
            break;
        case 4:
            argInt = value.isEmpty() ? 0 : Integer.parseInt(value);
            ((DefaultPokemobs) parent.trainer).battleCooldown = argInt;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, parent.trainer, null);
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            this.onPageOpened();
            mess = new TextComponentTranslation("traineredit.set.cooldown_g", argInt);
            break;
        case 5:
            argFloat = value.isEmpty() ? 0 : Float.parseFloat(value);
            parent.aiStates.setDirection(argFloat);
            this.onPageClosed();
            sendAIUpdate();
            this.onPageOpened();
            mess = new TextComponentTranslation("traineredit.set.look", argFloat);
            break;
        }
        if (time != null)
        {
            mess = new TextComponentTranslation("traineredit.set.guardtime", time.startTick, time.endTick);
        }
        if (mess != null) parent.mc.player.sendStatusMessage(mess, true);
    }

    private void sendGuardUpdate()
    {
        onPageClosed();
        PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
        IGuardAICapability guard = parent.entity.getCapability(EventsHandler.GUARDAI_CAP, null);
        NBTBase tag = EventsHandler.storage.writeNBT(EventsHandler.GUARDAI_CAP, guard, null);
        packet.data.setTag("T", tag);
        packet.data.setByte("V", (byte) 4);
        packet.data.setInteger("I", parent.entity.getEntityId());
        PokecubeMod.packetPipeline.sendToServer(packet);
        onPageOpened();
    }

    private void sendAIUpdate()
    {
        onPageClosed();
        PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
        NBTBase tag = CapabilityNPCAIStates.storage.writeNBT(CapabilityNPCAIStates.AISTATES_CAP, parent.aiStates, null);
        packet.data.setTag("T", tag);
        packet.data.setByte("V", (byte) 3);
        packet.data.setInteger("I", parent.entity.getEntityId());
        PokecubeMod.packetPipeline.sendToServer(packet);
        onPageOpened();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.aipage"), x, y, 0xFFFFFFFF);
        y += 14;
        drawString(fontRenderer, I18n.format("traineredit.info.wanderdist"), x - 55, y, 0xFFFFFFFF);
        y += 12;
        drawString(fontRenderer, I18n.format("traineredit.info.stationarytime"), x - 55, y, 0xFFFFFFFF);
        drawString(fontRenderer, "->", x + 65, y, 0xFFFFFFFF);
        y += 25;
        drawString(fontRenderer, I18n.format("traineredit.info.cooldown_p"), x - 115, y, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.cooldown_g"), x - 115, y + 20, 0xFFFFFFFF);

        drawString(fontRenderer, I18n.format("traineredit.info.levels"), x + 60, y + 50, 0xFFFFFFFF);
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

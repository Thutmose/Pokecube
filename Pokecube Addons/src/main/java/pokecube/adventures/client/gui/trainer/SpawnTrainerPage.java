package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer.Page;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.interfaces.PokecubeMod;

public class SpawnTrainerPage extends Page
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    private static final int TRAINER  = 0;
    private static final int LEADER   = 1;
    private static final int TRADER   = 2;
    private static final int PREVTYPE = 3;
    private static final int NEXTYPE  = 4;

    private TypeTrainer      type;

    public SpawnTrainerPage(GuiEditTrainer watch)
    {
        super(watch);
        type = TypeTrainer.merchant;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = parent.width / 2;
        int y = parent.height / 2;
        String next = ">";
        String prev = "<";
        // Cycle Trainer Type buttons
        int o = -20;
        parent.getButtons().add(new Button(PREVTYPE, x + o, y - 55, 20, 20, prev));
        parent.getButtons().add(new Button(NEXTYPE, x + 20 + o, y - 55, 20, 20, next));
        o = -40;
        parent.getButtons().add(new Button(TRAINER, x + o, y - 35, 80, 20, I18n.format("traineredit.button.trainer")));
        parent.getButtons()
                .add(new Button(LEADER, x + o, y - 35 + 20, 80, 20, I18n.format("traineredit.button.leader")));
        parent.getButtons()
                .add(new Button(TRADER, x + o, y - 35 + 40, 80, 20, I18n.format("traineredit.button.trader")));

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTitle(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        PacketTrainer packet = null;
        int dir = 0;
        switch (button.id)
        {
        case NEXTYPE:
            dir = 1;
            break;
        case PREVTYPE:
            dir = -1;
            break;
        case TRADER:
            packet = new PacketTrainer(PacketTrainer.MESSAGESPAWNTRAINER);
            packet.data.setByte("I", (byte) TRADER);
            break;
        case LEADER:
            packet = new PacketTrainer(PacketTrainer.MESSAGESPAWNTRAINER);
            packet.data.setByte("I", (byte) LEADER);
            break;
        case TRAINER:
            packet = new PacketTrainer(PacketTrainer.MESSAGESPAWNTRAINER);
            packet.data.setByte("I", (byte) TRAINER);
            break;
        }

        if (packet != null)
        {
            packet.data.setString("T", type.name);
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.mc.player.closeScreen();
        }

        // Change type
        if (dir != 0)
        {
            List<TypeTrainer> types = Lists.newArrayList();
            types.addAll(TypeTrainer.typeMap.values());
            types.sort(new Comparator<TypeTrainer>()
            {
                @Override
                public int compare(TypeTrainer o1, TypeTrainer o2)
                {
                    return o1.name.compareTo(o2.name);
                }
            });
            int index = -1;
            for (int i = 0; i < types.size(); i++)
            {
                if (types.get(i) == type)
                {
                    index = i;
                }
            }
            if (dir == -1)
            {
                if (index <= 0)
                {
                    index = types.size() - 1;
                }
                else
                {
                    index--;
                }
            }
            else
            {
                if (index >= types.size() - 1)
                {
                    index = 0;
                }
                else
                {
                    index++;
                }
            }
            type = types.get(index);
        }

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

    protected void drawTitle(int mouseX, int mouseY, float partialTicks)
    {
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, type.name, x, y, 0xFFFFFFFF);
    }
}

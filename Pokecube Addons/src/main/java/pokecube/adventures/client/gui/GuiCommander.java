package pokecube.adventures.client.gui;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.adventures.network.packets.PacketCommander;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.network.PokecubePacketHandler;

public class GuiCommander extends GuiScreen
{
    TileEntityCommander tile  = null;
    int                 index = 0;
    GuiTextField        command;
    GuiTextField        args;

    public GuiCommander(TileEntityCommander tile)
    {
        this.tile = tile;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        List<Command> types = Lists.newArrayList(Command.values());
        List<String> names = Lists.newArrayList();
        for (Command command : types)
            names.add(command.name());
        names.add("");
        if (button.id == 0)
        {
            if (index < names.size() - 1) index++;
            else index = 0;
        }
        else if (button.id == 1)
        {
            if (index > 0) index--;
            else index = names.size() - 1;
        }
        command.setText(names.get(index));
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        command.drawTextBox();
        args.drawTextBox();
        fontRenderer.drawString("Set Command",
                width / 2 - fontRenderer.getStringWidth("Set Command") / 3 - 20, height / 4 - 15, 4210752);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = 0;
        int yOffset = -11;

        buttonList.add(new GuiButton(0, width / 2 - xOffset + 64, height / 2 - yOffset - 85, 20, 20, "\u25b2"));
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 64, height / 2 - yOffset - 65, 20, 20, "\u25bc"));

        command = new GuiTextField(0, fontRenderer, width / 2 - 50, height / 4 + 20 + yOffset, 100, 10);
        command.setText(tile.getCommand() == null ? "" : "" + tile.getCommand());

        List<Command> types = Lists.newArrayList(Command.values());
        List<String> names = Lists.newArrayList();
        for (Command command : types)
            names.add(command.name());
        names.add("");
        for (index = 0; index < names.size(); index++)
        {
            if (command.getText().equals(names.get(index))) break;
        }

        args = new GuiTextField(1, fontRenderer, width / 2 - 50, height / 4 + 40 + yOffset, 100, 10);
        args.setText(tile.args);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        args.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        args.textboxKeyTyped(par1, par2);
        if (par1 == 13)
        {
            this.mc.displayGuiScreen((GuiScreen) null);
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void onGuiClosed()
    {
        sendChooseToServer();
        super.onGuiClosed();
    }

    private void sendChooseToServer()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("biome", command.getText());
        PacketCommander mess = new PacketCommander();
        mess.data.setInteger("x", tile.getPos().getX());
        mess.data.setInteger("y", tile.getPos().getY());
        mess.data.setInteger("z", tile.getPos().getZ());
        mess.data.setString("C", command.getText());
        mess.data.setString("A", args.getText());
        PokecubePacketHandler.sendToServer(mess);
    }
}

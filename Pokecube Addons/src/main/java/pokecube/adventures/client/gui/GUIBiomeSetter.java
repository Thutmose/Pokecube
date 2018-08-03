package pokecube.adventures.client.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.terrain.BiomeType;

public class GUIBiomeSetter extends GuiScreen
{
    ItemStack    setter;
    int          index = 0;
    GuiTextField textField0;

    public GUIBiomeSetter(ItemStack item)
    {
        setter = item;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        List<BiomeType> types = BiomeType.values();

        if (button.id == 0)
        {
            if (index < types.size() - 1) index++;
            else index = 0;
        }
        else if (button.id == 1)
        {
            if (index > 0) index--;
            else index = types.size() - 1;
        }
        BiomeType type = types.get(index);
        textField0.setText(type.readableName);
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        textField0.drawTextBox();
        fontRenderer.drawString("Sub-Biome to set",
                width / 2 - fontRenderer.getStringWidth("Sub-Biome to set") / 3 - 20, height / 4 - 15, 4210752);
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

        yOffset = -20;
        String biome = "";
        if (setter != null && setter.hasTagCompound())
        {
            biome = setter.getTagCompound().getString("biome");
        }

        textField0 = new GuiTextField(0, fontRenderer, width / 2 - 50, height / 4 + 20 + yOffset, 100, 10);
        textField0.setText(biome);
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
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
        tag.setString("biome", textField0.getText());
        MessageServer mess = new MessageServer(MessageServer.MESSAGEBIOMESETTER, tag);
        PokecubePacketHandler.sendToServer(mess);
    }
}

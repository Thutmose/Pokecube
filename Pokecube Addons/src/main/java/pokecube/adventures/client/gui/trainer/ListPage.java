package pokecube.adventures.client.gui.trainer;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer.Page;
import pokecube.core.client.gui.helper.ScrollGui;

public abstract class ListPage extends Page
{
    ScrollGui list;

    public ListPage(GuiEditTrainer watch)
    {
        super(watch);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        initList();
    }

    protected abstract void initList();

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        list.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        list.actionPerformed(button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        // Prevents super call.
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        list.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        list.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

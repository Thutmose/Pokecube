package pokecube.core.client.gui.blocks;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.pc.SlotPC;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPC;

public class GuiPC extends GuiContainer
{

    String          page;
    ContainerPC     cont;
    GuiTextField    textFieldSelectedBox;
    GuiTextField    textFieldBoxName;
    GuiTextField    textFieldSearch;

    String          autoOn   = I18n.format("tile.pc.autoon");
    String          autoOff  = I18n.format("tile.pc.autooff");

    private String  boxName  = "1";
    private boolean toRename = false;

    boolean         bound    = false;
    boolean         release  = false;

    private Slot    theSlot;

    public GuiPC(ContainerPC cont)
    {
        super(cont);
        this.cont = cont;
        this.xSize = 175;
        this.ySize = 229;
        page = cont.getPageNb();
        boxName = cont.getPage();
        if (cont.pcTile != null) this.bound = cont.pcTile.isBound();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (mc.player.getEntityWorld().isRemote)
        {
            if (guibutton.id == 5 && cont.pcTile != null)// Toggle Bound
            {
                cont.pcTile.toggleBound();
                mc.player.closeScreen();
                return;
            }
            if (guibutton.id == 8 && cont.pcTile != null)// Bind PC to self
            {
                cont.pcTile.setBoundOwner(mc.player);
                mc.player.closeScreen();
                return;
            }

            else if (guibutton.id == 4)// Toggle Rename
            {
                if (toRename)
                {
                    String box = textFieldBoxName.getText();
                    if (box != boxName) cont.changeName(box);
                }
                toRename = !toRename;
            }
            else if (guibutton.id == 3)// Toggle Auto To PC
            {
                cont.toggleAuto();
            }
            else if (guibutton.id == 6)// Initialte Release
            {
                release = !release;
                if (!release && cont.release)
                {
                    cont.toRelease = new boolean[54];
                    for (int i = 0; i < 54; i++)
                    {
                        int index = i;
                        SlotPC slot = (SlotPC) cont.inventorySlots.get(index);
                        slot.release = false;
                    }
                }
                else
                {
                    for (int i = 0; i < 54; i++)
                    {
                        int index = i;
                        SlotPC slot = (SlotPC) cont.inventorySlots.get(index);
                        slot.release = true;
                    }
                }
                cont.release = release;
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                {
                    PacketPC packet = new PacketPC(PacketPC.RELEASE);
                    packet.data.setBoolean("T", true);
                    packet.data.setBoolean("R", release);
                    PokecubeMod.packetPipeline.sendToServer(packet);
                }
                if (release)
                {
                    buttonList.get(6).enabled = release;
                    buttonList.get(6).visible = release;
                }
                else
                {
                    buttonList.get(6).enabled = release;
                    buttonList.get(6).visible = release;
                }
            }
            else if (guibutton.id == 7)// Confirm Release
            {
                release = !release;
                cont.setRelease(release);
                if (release)
                {
                    buttonList.get(6).enabled = release;
                    buttonList.get(6).visible = release;
                }
                else
                {
                    buttonList.get(6).enabled = release;
                    buttonList.get(6).visible = release;
                }
                mc.player.closeScreen();
            }
            else
            {
                cont.updateInventoryPages((byte) (guibutton.id == 2 ? -1 : guibutton.id == 1 ? 1 : 0),
                        mc.player.inventory);
                textFieldSelectedBox.setText(cont.getPageNb());
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"));
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        GL11.glPushMatrix();
        GL11.glScaled(0.8, 0.8, 0.8);

        String name = (cont.pcTile != null) ? cont.pcTile.getName() : "";
        String pcTitle = bound ? name : I18n.format("tile.pc.title", cont.inv.seenOwner ? "Thutmose" : "Someone");
        fontRenderer.drawString(cont.getPage(), xSize / 2 - fontRenderer.getStringWidth(cont.getPage()) / 3 - 60,
                13, 4210752);
        fontRenderer.drawString(pcTitle, xSize / 2 - fontRenderer.getStringWidth(pcTitle) / 3 - 60, 4, 4210752);
        GL11.glPopMatrix();

        for (int i = 0; i < 54; i++)
        {
            if (cont.toRelease[i])
            {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(0, 1, 0, 1);
                mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/hologram.png"));
                int x = (i % 9) * 18 + 8;
                int y = 18 + (i / 9) * 18;
                drawTexturedModalRect(x, y, 0, 0, 16, 16);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }

        }

        if (!bound) buttonList.get(2).displayString = cont.inv.autoToPC ? autoOn : autoOff;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, f);
        textFieldSelectedBox.drawTextBox();

        if (!bound) textFieldSearch.drawTextBox();

        if (toRename) textFieldBoxName.drawTextBox();
        float zLevel = 1000;
        for (int i = 0; i < 54; i++)
            if (!textFieldSearch.getText().isEmpty())
            {
                ItemStack stack = cont.inv.getStackInSlot(i + 54 * cont.inv.getPage());
                int x = (i % 9) * 18 + width / 2 - 80;
                int y = (i / 9) * 18 + height / 2 - 96;

                String name = stack == null ? "" : stack.getDisplayName();
                if (name.isEmpty() || !name.toLowerCase(java.util.Locale.ENGLISH).contains(textFieldSearch.getText()))
                {
                    GL11.glPushMatrix();
                    GL11.glTranslated(0, 0, zLevel);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glColor4f(0, 0, 0, 1);
                    mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/hologram.png"));
                    drawTexturedModalRect(x, y, 0, 0, 16, 16);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glTranslated(0, 0, -zLevel);
                    GL11.glPopMatrix();
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glColor4f(0, 1, 0, 1);
                    mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/hologram.png"));
                    drawTexturedModalRect(x, y, 0, 0, 16, 16);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = 0;
        int yOffset = -11;
        String next = I18n.format("tile.pc.next");
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 15, height / 2 - yOffset, 50, 20, next));
        String prev = I18n.format("tile.pc.previous");
        buttonList.add(new GuiButton(2, width / 2 - xOffset - 65, height / 2 - yOffset, 50, 20, prev));

        if (!bound)
        {
            String auto = cont.inv.autoToPC ? I18n.format("tile.pc.autoon") : I18n.format("tile.pc.autooff");
            buttonList.add(new GuiButton(3, width / 2 - xOffset - 137, height / 2 - yOffset - 105, 50, 20, auto));
        }
        if (!bound)
        {
            String rename = I18n.format("tile.pc.rename");
            buttonList.add(new GuiButton(4, width / 2 - xOffset - 137, height / 2 - yOffset - 125, 50, 20, rename));
        }
        if (cont.pcTile != null)
        {
            if (!bound) buttonList
                    .add(new GuiButton(5, width / 2 - xOffset - 137, height / 2 - yOffset - 85, 50, 20, "private"));
            else
            {
                buttonList
                        .add(new GuiButton(5, width / 2 - xOffset - 137, height / 2 - yOffset - 125, 50, 20, "public"));
                buttonList.add(new GuiButton(8, width / 2 - xOffset - 137, height / 2 - yOffset - 105, 50, 20, "bind"));
            }
        }
        else
        {
            buttonList.add(new GuiButton(5, width / 2 - xOffset - 137, height / 2 - yOffset - 125, 0, 0, ""));
        }
        if (!bound)
        {
            buttonList.add(new GuiButton(6, width / 2 - xOffset - 137, height / 2 - yOffset - 65, 50, 20, "release"));
            buttonList.add(new GuiButton(7, width / 2 - xOffset - 137, height / 2 - yOffset - 45, 50, 20, "confirm"));
            buttonList.get(6).visible = false;
            buttonList.get(6).enabled = false;

        }

        textFieldSelectedBox = new GuiTextField(0, fontRenderer, width / 2 - xOffset - 13, height / 2 - yOffset + 5,
                25, 10);
        textFieldSelectedBox.setText(page);

        textFieldBoxName = new GuiTextField(0, fontRenderer, width / 2 - xOffset - 190, height / 2 - yOffset - 40,
                100, 10);
        textFieldBoxName.setText(boxName);

        textFieldSearch = new GuiTextField(0, fontRenderer, width / 2 - xOffset - 10, height / 2 - yOffset - 121, 90,
                10);
        textFieldSearch.setText("");
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        keyTyped2(par1, par2);
        if (par2 == 1)
        {
            mc.player.closeScreen();
            return;
        }

        textFieldSearch.textboxKeyTyped(par1, par2);

        if (toRename) textFieldBoxName.textboxKeyTyped(par1, par2);
        if (par1 <= 57)
        {
            textFieldSelectedBox.textboxKeyTyped(par1, par2);
        }
        if (par2 == 28)
        {
            String entry = textFieldSelectedBox.getText();
            String box = textFieldBoxName.getText();
            int number = 1;

            try
            {
                number = Integer.parseInt(entry);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            number = Math.max(1, Math.min(number, InventoryPC.PAGECOUNT));
            cont.gotoInventoryPage(number);

            if (toRename && box != boxName)
            {

                if (toRename)
                {
                    box = textFieldBoxName.getText();
                    if (box != boxName) cont.changeName(box);
                }
                toRename = !toRename;
            }
        }

    }

    /** Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). */
    protected void keyTyped2(char par1, int par2)
    {
        this.checkHotbarKeys(par2);

        if (this.theSlot != null && this.theSlot.getHasStack())
        {
            if (par2 == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, ClickType.CLONE);
            }
            else if (par2 == this.mc.gameSettings.keyBindDrop.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        textFieldSelectedBox.mouseClicked(par1, par2, par3);
        textFieldSearch.mouseClicked(par1, par2, par3);
        if (toRename) textFieldBoxName.mouseClicked(par1, par2, par3);

    }

    /** Called when the screen is unloaded. Used to disable keyboard repeat
     * events */
    @Override
    public void onGuiClosed()
    {
        if (this.mc.player != null)
        {
            this.inventorySlots.onContainerClosed(this.mc.player);
        }
    }

}
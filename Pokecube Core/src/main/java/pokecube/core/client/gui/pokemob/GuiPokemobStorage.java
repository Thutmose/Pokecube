package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.ai.thread.aiRunnables.utility.AIStoreStuff;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector4;
import thut.lib.CompatWrapper;

public class GuiPokemobStorage extends GuiContainer
{
    final IInventory   playerInventory;
    final IInventory   pokeInventory;
    final IPokemob     pokemob;
    final Entity       entity;
    GuiTextField       berry;
    GuiTextField       storage;
    GuiTextField       storageFace;
    GuiTextField       empty;
    AIStoreStuff       ai;
    GuiTextField       emptyFace;
    private float      yRenderAngle = 10;
    private float      xRenderAngle = 0;
    List<GuiTextField> textBoxes    = Lists.newArrayList();

    public GuiPokemobStorage(IInventory playerInv, IPokemob pokemob)
    {
        super(new ContainerPokemob(playerInv, pokemob.getPokemobInventory(), pokemob, false));
        this.pokemob = pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = pokemob.getPokemobInventory();
        this.entity = pokemob.getEntity();
    }

    @Override
    /** Draws the background (i is always 0 as of 1.2.2) */
    public void drawBackground(int tint)
    {
        super.drawBackground(tint);
    }

    @Override
    public void drawWorldBackground(int tint)
    {
        super.drawWorldBackground(tint);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = width / 2 - 10;
        int yOffset = height / 2 - 77;
        buttonList.add(new GuiButton(0, xOffset + 60, yOffset, 30, 10, "Inv"));
        buttonList.add(new GuiButton(1, xOffset + 30, yOffset, 30, 10, "AI"));
        buttonList.add(new GuiButton(2, xOffset + 00, yOffset, 30, 10, "RT"));
        xOffset += 29;
        int dy = 13;
        int ds = 10;
        berry = new GuiTextField(0, fontRenderer, xOffset + 10, yOffset + dy + ds * 0, 50, 10);
        storage = new GuiTextField(1, fontRenderer, xOffset + 10, yOffset + dy + ds * 1, 50, 10);
        storageFace = new GuiTextField(2, fontRenderer, xOffset + 10, yOffset + dy + ds * 2, 50, 10);
        empty = new GuiTextField(3, fontRenderer, xOffset + 10, yOffset + dy + ds * 3, 50, 10);
        emptyFace = new GuiTextField(4, fontRenderer, xOffset + 10, yOffset + dy + ds * 4, 50, 10);
        textBoxes = Lists.newArrayList(berry, storage, storageFace, empty, emptyFace);

        for (IAIRunnable run : pokemob.getAI().aiTasks)
        {
            if (run instanceof AIStoreStuff)
            {
                ai = (AIStoreStuff) run;
                NBTTagCompound nbt = ai.serializeNBT();
                NBTTagCompound berry = nbt.getCompoundTag("b");
                NBTTagCompound storage = nbt.getCompoundTag("s");
                NBTTagCompound empty = nbt.getCompoundTag("e");
                if (!berry.hasNoTags())
                {
                    this.berry
                            .setText(berry.getInteger("x") + " " + berry.getInteger("y") + " " + berry.getInteger("z"));
                }
                if (!storage.hasNoTags())
                {
                    this.storage.setText(
                            storage.getInteger("x") + " " + storage.getInteger("y") + " " + storage.getInteger("z"));
                    storageFace.setText(EnumFacing.values()[storage.getByte("f")] + "");
                }
                else
                {
                    storageFace.setText("UP");
                }
                if (!empty.hasNoTags())
                {
                    this.empty
                            .setText(empty.getInteger("x") + " " + empty.getInteger("y") + " " + empty.getInteger("z"));
                    emptyFace.setText(EnumFacing.values()[empty.getByte("f")] + "");
                }
                else
                {
                    emptyFace.setText("UP");
                }
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        for (GuiTextField text : textBoxes)
            text.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN)
        {
            sendUpdate();
        }
    }

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
        try
        {
            super.mouseClicked(x, y, mouseButton);
            BlockPos newLink = null;
            InventoryPlayer inv = (InventoryPlayer) playerInventory;
            boolean effect = false;
            if (CompatWrapper.isValid(inv.getItemStack()) && inv.getItemStack().hasTagCompound())
            {
                NBTTagCompound link = inv.getItemStack().getTagCompound().getCompoundTag("link");
                if (!link.hasNoTags())
                {
                    Vector4 pos = new Vector4(link);
                    newLink = new BlockPos((int) (pos.x - 0.5), (int) (pos.y), (int) (pos.z - 0.5));
                }
            }
            for (GuiTextField text : textBoxes)
            {
                boolean before = text.isFocused();
                text.mouseClicked(x, y, mouseButton);
                boolean after = text.isFocused();
                if (newLink != null && text.isFocused() && (text == berry || text == storage || text == empty))
                {
                    text.setText(newLink.getX() + " " + newLink.getY() + " " + newLink.getZ());
                    effect = true;
                }
                if (before && !after)
                {
                    effect = true;
                }
            }
            if (effect) sendUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sendUpdate()
    {
        BlockPos berryLoc = posFromText(berry.getText());
        if (berryLoc == null) berry.setText("");
        BlockPos storageLoc = posFromText(storage.getText());
        if (storageLoc == null) storage.setText("");
        BlockPos emptyInventory = posFromText(empty.getText());
        if (emptyInventory == null) empty.setText("");
        EnumFacing storageFace = dirFromText(this.storageFace.getText());
        this.storageFace.setText(storageFace + "");
        EnumFacing emptyFace = dirFromText(this.emptyFace.getText());
        this.emptyFace.setText(emptyFace + "");
        ai.berryLoc = berryLoc;
        ai.storageLoc = storageLoc;
        ai.storageFace = storageFace;
        ai.emptyFace = emptyFace;
        ai.emptyInventory = emptyInventory;
        PacketUpdateAI.sendUpdatePacket(pokemob, ai.getIdentifier(), null);

        // Send status message thingy
        this.mc.player.sendStatusMessage(new TextComponentTranslation("pokemob.gui.updatestorage"), true);
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
                System.err.println("Error with pos:" + text);
            }
        }
        else if (args.length != 0)
        {
            // Send status message about not working here.
            System.err.println("Error with pos:" + text);
        }
        return null;
    }

    private EnumFacing dirFromText(String text)
    {
        text = text.toLowerCase(Locale.ENGLISH);
        EnumFacing dir = EnumFacing.byName(text);
        if (dir == null)
        {
            if (!text.isEmpty())
            {
                // Send status message about not working here.
                System.err.println("Error with dir:" + text);
            }
            dir = EnumFacing.UP;
        }
        return dir;
    }

    /** Called when a mouse button is released. */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(this.pokeInventory.hasCustomName() ? this.pokeInventory.getName()
                : I18n.format(this.pokeInventory.getName(), new Object[0]), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
                : I18n.format(this.playerInventory.getName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);
        int x = 83;
        int y = 20;
        this.fontRenderer.drawString(I18n.format("pokemob.gui.berry"), x, y, 4210752);
        this.fontRenderer.drawString(I18n.format("pokemob.gui.store"), x, y + 10, 4210752);
        this.fontRenderer.drawString(I18n.format("pokemob.gui.face"), x, y + 20, 4210752);
        this.fontRenderer.drawString(I18n.format("pokemob.gui.empty"), x, y + 30, 4210752);
        this.fontRenderer.drawString(I18n.format("pokemob.gui.face"), x, y + 40, 4210752);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 0)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, entity.getEntityId());
        }
        else if (guibutton.id == 1)
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.AI, entity.getEntityId());
        }
        else
        {
            PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, entity.getEntityId());
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        for (GuiTextField text : textBoxes)
            text.drawTextBox();
        this.renderHoveredToolTip(i, j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        yRenderAngle = -entity.rotationYaw + 45;
        xRenderAngle = 0;
        GuiPokemob.renderMob(pokemob, k, l, xSize, ySize, xRenderAngle, yRenderAngle, 0, 1);
    }
}

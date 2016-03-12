package pokecube.adventures.client.gui;

import static pokecube.core.utils.PokeType.flying;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;

@SuppressWarnings("deprecation")
public class GuiTrainerEdit extends GuiScreen
{
    public static int x;
    public static int y;

    static float              lastTime       = 0;
    /** to pass as last parameter when rendering the mob so that the render
     * knows the rendering is asked by the pokedex gui */
    public final static float POKEDEX_RENDER = 1.5f;

    GuiTextField textfieldPokedexNb0;
    GuiTextField textfieldLevel0;

    GuiTextField textfieldPokedexNb1;
    GuiTextField textfieldLevel1;

    GuiTextField textfieldPokedexNb2;
    GuiTextField textfieldLevel2;

    GuiTextField textfieldPokedexNb3;
    GuiTextField textfieldLevel3;

    GuiTextField textfieldPokedexNb4;
    GuiTextField textfieldLevel4;

    GuiTextField textfieldPokedexNb5;

    GuiTextField textfieldLevel5;
    GuiTextField textFieldName;

    GuiTextField textFieldType;

    private final EntityTrainer trainer;

    String F = "false";

    String T = "true";

    public GuiTrainerEdit(EntityTrainer trainer)
    {
        this.trainer = trainer;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 1)
        {
            trainer.setAIState(EntityTrainer.STATIONARY, !trainer.getAIState(EntityTrainer.STATIONARY));
            guibutton.displayString = trainer.getAIState(EntityTrainer.STATIONARY) ? T : F;
        }
        if (guibutton.id == 2)
        {
            sendChooseToServer();
            mc.thePlayer.addChatComponentMessage(
                    new ChatComponentText(StatCollector.translateToLocal("gui.trainer.saved")));
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);

        int x1 = width / 2;
        int y1 = height / 2;

        String info = StatCollector.translateToLocal("gui.trainer.stationary");
        int l = fontRendererObj.getStringWidth(info);
        this.fontRendererObj.drawString(info, x1 + 90 - l / 2, y1 + 10, 0xffffff);

        int num = 0;
        if (trainer.getEquipmentInSlot(1) != null)
        {
                    int i1 = x1 + 50;
                    int j1 = y1 - 20;
                    int z = 0;
                    GL11.glPushMatrix();
                    GL11.glTranslated(i1,j1, z);
                    GL11.glScaled(8, 8, 8);
                    Minecraft.getMinecraft().getItemRenderer().renderItem(mc.thePlayer, trainer.getEquipmentInSlot(1),TransformType.GUI);
                    GL11.glPopMatrix();
        }

        try
        {
            num = Integer.parseInt(textfieldPokedexNb0.getText());
            PokedexEntry entry = Database.getEntry(num);
            if (entry != null)
            {
                renderMob(Database.getEntry(num));
            }
            num = Integer.parseInt(textfieldPokedexNb1.getText());
            entry = Database.getEntry(num);
            int dy = 20;
            if (entry != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(0, dy, 0);
                renderMob(Database.getEntry(num));
                GL11.glPopMatrix();
            }
            num = Integer.parseInt(textfieldPokedexNb2.getText());
            entry = Database.getEntry(num);
            if (entry != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(0, 2 * dy, 0);
                renderMob(Database.getEntry(num));
                GL11.glPopMatrix();
            }
            num = Integer.parseInt(textfieldPokedexNb3.getText());
            entry = Database.getEntry(num);
            if (entry != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(0, 3 * dy, 0);
                renderMob(Database.getEntry(num));
                GL11.glPopMatrix();
            }
            num = Integer.parseInt(textfieldPokedexNb4.getText());
            entry = Database.getEntry(num);
            if (entry != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(0, 4 * dy, 0);
                renderMob(Database.getEntry(num));
                GL11.glPopMatrix();
            }
            num = Integer.parseInt(textfieldPokedexNb5.getText());
            entry = Database.getEntry(num);
            if (entry != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(0, 5 * dy, 0);
                renderMob(Database.getEntry(num));
                GL11.glPopMatrix();
            }
        }
        catch (NumberFormatException e)
        {
        }

        textFieldName.drawTextBox();
        textFieldType.drawTextBox();

        textfieldPokedexNb0.drawTextBox();
        textfieldLevel0.drawTextBox();
        textfieldPokedexNb1.drawTextBox();
        textfieldLevel1.drawTextBox();
        textfieldPokedexNb2.drawTextBox();
        textfieldLevel2.drawTextBox();
        textfieldPokedexNb3.drawTextBox();
        textfieldLevel3.drawTextBox();
        textfieldPokedexNb4.drawTextBox();
        textfieldLevel4.drawTextBox();
        textfieldPokedexNb5.drawTextBox();
        textfieldLevel5.drawTextBox();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();

        int yOffset = -20;
        int xOffset = +20;

        textFieldName = new GuiTextField(0, fontRendererObj, width / 2 - 50, height / 4 + 20 + yOffset, 100, 10);
        textFieldType = new GuiTextField(0, fontRendererObj, width / 2 - 50, height / 4 + 40 + yOffset, 100, 10);

        String next = trainer.getAIState(EntityTrainer.STATIONARY) ? T : F;
        ;
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 80, height / 2 - yOffset, 50, 20, next));
        buttonList.add(new GuiButton(2, width / 2 - xOffset + 80, height / 2 - yOffset + 20, 50, 20, "Save"));

        textfieldPokedexNb0 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 60 + yOffset,
                30, 10);
        textfieldLevel0 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 60 + yOffset, 30,
                10);
        textfieldPokedexNb1 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 80 + yOffset,
                30, 10);
        textfieldLevel1 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 80 + yOffset, 30,
                10);
        textfieldPokedexNb2 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 100 + yOffset,
                30, 10);
        textfieldLevel2 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 100 + yOffset, 30,
                10);
        textfieldPokedexNb3 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 120 + yOffset,
                30, 10);
        textfieldLevel3 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 120 + yOffset, 30,
                10);
        textfieldPokedexNb4 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 140 + yOffset,
                30, 10);
        textfieldLevel4 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 140 + yOffset, 30,
                10);
        textfieldPokedexNb5 = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset, height / 4 + 160 + yOffset,
                30, 10);
        textfieldLevel5 = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset, height / 4 + 160 + yOffset, 30,
                10);

        int[] numbers = trainer.pokenumbers;
        int[] levels = trainer.pokelevels;

        textfieldPokedexNb0.setText(numbers[0] + "");
        textfieldLevel0.setText(levels[0] + "");
        textfieldPokedexNb1.setText(numbers[1] + "");
        textfieldLevel1.setText(levels[1] + "");
        textfieldPokedexNb2.setText(numbers[2] + "");
        textfieldLevel2.setText(levels[2] + "");
        textfieldPokedexNb3.setText(numbers[3] + "");
        textfieldLevel3.setText(levels[3] + "");
        textfieldPokedexNb4.setText(numbers[4] + "");
        textfieldLevel4.setText(levels[4] + "");
        textfieldPokedexNb5.setText(numbers[5] + "");
        textfieldLevel5.setText(levels[5] + "");
        textFieldName.setText(trainer.name);
        textFieldType.setText(trainer.type.name);
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

        if (par2 == Keyboard.KEY_UP)
        {
            int num = 0;
            try
            {
                if (textfieldPokedexNb0.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb0.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb0.setText("" + n);
                }
                if (textfieldPokedexNb1.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb1.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb1.setText("" + n);
                }
                if (textfieldPokedexNb2.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb2.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb2.setText("" + n);
                }
                if (textfieldPokedexNb3.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb3.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb3.setText("" + n);
                }
                if (textfieldPokedexNb4.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb4.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb4.setText("" + n);
                }
                if (textfieldPokedexNb5.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb5.getText());
                    int n = Pokedex.getInstance().getNext(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb5.setText("" + n);
                }
            }
            catch (NumberFormatException e)
            {

            }
        }
        else if (par2 == Keyboard.KEY_DOWN)
        {

            int num = 0;
            try
            {
                if (textfieldPokedexNb0.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb0.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb0.setText("" + n);
                }
                if (textfieldPokedexNb1.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb1.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb1.setText("" + n);
                }
                if (textfieldPokedexNb2.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb2.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb2.setText("" + n);
                }
                if (textfieldPokedexNb3.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb3.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb3.setText("" + n);
                }
                if (textfieldPokedexNb4.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb4.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb4.setText("" + n);
                }
                if (textfieldPokedexNb5.isFocused())
                {
                    num = Integer.parseInt(textfieldPokedexNb5.getText());
                    int n = Pokedex.getInstance().getPrevious(Pokedex.getInstance().getEntry(num), 1).getPokedexNb();
                    textfieldPokedexNb5.setText("" + n);
                }
            }
            catch (NumberFormatException e)
            {

            }
        }

        textFieldName.textboxKeyTyped(par1, par2);
        textFieldType.textboxKeyTyped(par1, par2);

        if (par1 <= 57)
        {
            textfieldPokedexNb0.textboxKeyTyped(par1, par2);
            textfieldLevel0.textboxKeyTyped(par1, par2);

            textfieldPokedexNb1.textboxKeyTyped(par1, par2);
            textfieldLevel1.textboxKeyTyped(par1, par2);

            textfieldPokedexNb2.textboxKeyTyped(par1, par2);
            textfieldLevel2.textboxKeyTyped(par1, par2);

            textfieldPokedexNb3.textboxKeyTyped(par1, par2);
            textfieldLevel3.textboxKeyTyped(par1, par2);

            textfieldPokedexNb4.textboxKeyTyped(par1, par2);
            textfieldLevel4.textboxKeyTyped(par1, par2);

            textfieldPokedexNb5.textboxKeyTyped(par1, par2);
            textfieldLevel5.textboxKeyTyped(par1, par2);

        }
    }
    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        textfieldPokedexNb0.mouseClicked(par1, par2, par3);
        textfieldLevel0.mouseClicked(par1, par2, par3);

        textfieldPokedexNb1.mouseClicked(par1, par2, par3);
        textfieldLevel1.mouseClicked(par1, par2, par3);

        textfieldPokedexNb2.mouseClicked(par1, par2, par3);
        textfieldLevel2.mouseClicked(par1, par2, par3);

        textfieldPokedexNb3.mouseClicked(par1, par2, par3);
        textfieldLevel3.mouseClicked(par1, par2, par3);

        textfieldPokedexNb4.mouseClicked(par1, par2, par3);
        textfieldLevel4.mouseClicked(par1, par2, par3);

        textfieldPokedexNb5.mouseClicked(par1, par2, par3);
        textfieldLevel5.mouseClicked(par1, par2, par3);

        textFieldName.mouseClicked(par1, par2, par3);
        textFieldType.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed()
    {
        sendChooseToServer();

        super.onGuiClosed();
    }

    private void renderMob(PokedexEntry mob)
    {
        try
        {
            EntityLiving entity = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(mob.getPokedexNb(),
                    trainer.worldObj);

            float size = 0;
            int j = 0;
            int k = 0;

            IPokemob pokemob = null;
            if (entity instanceof IPokemob)
            {
                pokemob = (IPokemob) entity;
            }

            if (entity instanceof IPokemob)
            {
                pokemob.setShiny(false);
                pokemob.setSize(4);
            }
            if (entity instanceof IMobColourable) ((IMobColourable) pokemob).setRGBA(255, 255, 255, 255);
            size = Math.max(entity.width, entity.height);
            j = (width - 100) / 2;
            k = (height - 100) / 2;

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(j + 42, k + 36, 50F);
            float zoom = 25f / size;// (float)(23F / Math.sqrt(size + 0.6));
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - 100;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = 0F;
            entity.rotationYaw = 0;
            entity.rotationPitch = 0;
            entity.rotationYawHead = entity.rotationYaw;
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);

            GL11.glRotatef(-30, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0, 1.0F, 0.0F, 0.0F);

            // LoggerPokecube.logError(""+triangle);
            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.onGround = ((IPokemob) entity).getType1() != flying && ((IPokemob) entity).getType2() != flying;
            int i = 15728880;
            int j1 = i % 65536;
            int k1 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0, 0, 0, 0, POKEDEX_RENDER);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    private void sendChooseToServer()
    {
        PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
        buff.writeByte(1);
        try
        {
            buff.writeInt(Integer.parseInt(textfieldPokedexNb0.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel0.getText()));
            buff.writeInt(Integer.parseInt(textfieldPokedexNb1.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel1.getText()));
            buff.writeInt(Integer.parseInt(textfieldPokedexNb2.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel2.getText()));
            buff.writeInt(Integer.parseInt(textfieldPokedexNb3.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel3.getText()));
            buff.writeInt(Integer.parseInt(textfieldPokedexNb4.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel4.getText()));
            buff.writeInt(Integer.parseInt(textfieldPokedexNb5.getText()));
            buff.writeInt(Integer.parseInt(textfieldLevel5.getText()));
            buff.writeInt(textFieldName.getText().length());
            buff.writeBytes(textFieldName.getText().getBytes());
            buff.writeInt(textFieldType.getText().length());
            buff.writeBytes(textFieldType.getText().getBytes());
            buff.writeInt(trainer.getId());
        }
        catch (NumberFormatException e)
        {
        }
        MessageServer packet = new MessageServer(buff);
        PokecubePacketHandler.sendToServer(packet);
    }
}

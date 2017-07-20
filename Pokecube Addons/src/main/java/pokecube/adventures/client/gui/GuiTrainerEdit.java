package pokecube.adventures.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class GuiTrainerEdit extends GuiScreen
{
    GuiTextField[]                 textFieldPokemobs = new GuiTextField[6];
    GuiTextField[]                 textFieldLevels   = new GuiTextField[6];

    GuiTextField                   textFieldName;

    GuiTextField                   textFieldType;

    String                         oldType;
    String                         oldName;

    private final IHasPokemobs     pokemobCap;
    private final IHasNPCAIStates  aiCap;
    private final EntityLivingBase trainer;

    String                         F                 = "false";

    String                         T                 = "true";

    boolean                        stationary        = false;
    boolean                        resetTeam         = false;

    public GuiTrainerEdit(EntityLivingBase trainer)
    {
        this.pokemobCap = CapabilityHasPokemobs.getHasPokemobs(trainer);
        this.aiCap = CapabilityNPCAIStates.getNPCAIStates(trainer);
        this.trainer = trainer;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 1)
        {
            stationary = !stationary;
            guibutton.displayString = stationary ? F : T;
            sendChooseToServer();
        }
        else if (guibutton.id == 2)
        {
            sendChooseToServer();
            CompatWrapper.sendChatMessage(mc.thePlayer, new TextComponentString(I18n.format("gui.trainer.saved")));
        }
        else if (guibutton.id == 5)
        {
            byte gender = pokemobCap.getGender();
            pokemobCap.setGender((byte) (gender == 1 ? 2 : 1));
            guibutton.displayString = gender == 1 ? "\u2642" : "\u2640";
            sendChooseToServer();
        }
        else if (guibutton.id == 6)
        {
            resetTeam = true;
            sendChooseToServer();
            mc.thePlayer.closeScreen();
            resetTeam = false;
        }
        else if (guibutton.id == 7)
        {
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEKILLTRAINER);
            packet.data.setInteger("I", trainer.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            mc.thePlayer.closeScreen();
        }
        else
        {
            List<String> types = Lists.newArrayList();
            types.addAll(TypeTrainer.typeMap.keySet());
            Collections.sort(types);
            int index = -1;
            for (int i = 0; i < types.size(); i++)
            {
                if (types.get(i).equalsIgnoreCase(textFieldType.getText()))
                {
                    index = i;
                }
            }
            if (guibutton.id == 3)
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
            pokemobCap.setType(TypeTrainer.getTrainer(types.get(index)));
            textFieldType.setText(types.get(index));
            if (textFieldName.getText().startsWith(oldType))
            {
                textFieldName.setText(textFieldName.getText().replaceFirst(oldType, pokemobCap.getType().name));
            }
            sendChooseToServer();
            oldType = pokemobCap.getType().name;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int x1 = width / 2;
        int y1 = height / 2;

        String info = I18n.format("gui.trainer.stationary");
        int l = fontRendererObj.getStringWidth(info);
        this.fontRendererObj.drawString(info, x1 + 90 - l / 2, y1 + 10, 0xffffff);

        ItemStack stack = trainer.getHeldItemOffhand();
        if (stack != null)
        {
            int i1 = x1 + 50;
            int j1 = y1 - 20;
            int z = 0;
            GL11.glPushMatrix();
            GL11.glTranslated(i1, j1, z);
            GL11.glScaled(8, 8, 8);
            Minecraft.getMinecraft().getItemRenderer().renderItem(mc.thePlayer, stack, TransformType.GUI);
            GL11.glPopMatrix();
        }

        try
        {
            GuiTextField field;
            int i1, j1;
            for (int n = 0; n < 6; n++)
            {
                PokedexEntry entry = getEntry(n);
                field = textFieldLevels[n];
                i1 = field.xPosition - 20;
                j1 = field.yPosition - 3;
                drawGradientRect(i1, j1, i1 + 16, j1 + 16, 0xffaaaaaa, 0xffaaaaaa);
                double scale = 1;
                if (entry != null)
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(i1 + 8, j1 + 14, 50F);
                    GL11.glScaled(scale, scale, scale);
                    EventsHandlerClient.renderMob(
                            EventsHandlerClient.getPokemobForRender(pokemobCap.getPokemob(n), trainer.getEntityWorld()),
                            partialTicks, true);
                    GL11.glPopMatrix();
                }
            }
        }
        catch (NumberFormatException e)
        {
        }

        textFieldName.drawTextBox();
        textFieldType.drawTextBox();

        for (int i1 = 0; i1 < 6; i1++)
        {
            textFieldLevels[i1].drawTextBox();
            textFieldPokemobs[i1].drawTextBox();
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();

        int yOffset = -20;
        int xOffset = +20;

        textFieldName = new GuiTextField(0, fontRendererObj, width / 2 - 80, height / 4 + 10 + yOffset, 130, 10);
        textFieldType = new GuiTextField(0, fontRendererObj, width / 2 - 50, height / 4 + 40 + yOffset, 100, 10);
        textFieldType.setEnabled(false);

        oldName = trainer instanceof EntityTrainer ? ((EntityTrainer) trainer).name : "";
        oldType = pokemobCap.getType().name;

        String next = (stationary = aiCap.getAIState(IHasNPCAIStates.STATIONARY)) ? F : T;
        buttonList.add(new GuiButton(1, width / 2 - xOffset + 80, height / 2 - yOffset, 50, 20, next));
        buttonList.add(new GuiButton(2, width / 2 - xOffset + 80, height / 2 - yOffset + 20, 50, 20, "Save"));
        yOffset = 0;

        next = I18n.format("tile.pc.next");
        String prev = I18n.format("tile.pc.previous");
        // Cycle Trainer Type buttons
        buttonList.add(new GuiButton(3, width / 2 - xOffset - 90, height / 2 - yOffset - 60, 50, 20, prev));
        buttonList.add(new GuiButton(4, width / 2 - xOffset + 80, height / 2 - yOffset - 60, 50, 20, next));
        String gender = pokemobCap.getGender() == 2 ? "\u2642" : "\u2640";
        buttonList.add(new GuiButton(5, width / 2 - xOffset - 90, height / 2 - yOffset - 90, 20, 20, gender));
        buttonList.add(new GuiButton(6, width / 2 - xOffset + 80, height / 2 - yOffset - 90, 50, 20, "Reset"));
        buttonList.add(new GuiButton(7, width / 2 - xOffset + 80, height / 2 - yOffset + 60, 50, 20, "Kill"));
        yOffset = -20;
        for (int i = 0; i < 6; i++)
        {
            xOffset = -50;
            textFieldPokemobs[i] = new GuiTextField(0, fontRendererObj, width / 2 - 70 + xOffset,
                    height / 4 + 60 + i * 20 + yOffset, 100, 10);
            xOffset = +20;
            textFieldLevels[i] = new GuiTextField(0, fontRendererObj, width / 2 - 15 + xOffset,
                    height / 4 + 60 + i * 20 + yOffset, 30, 10);

            ItemStack stack = pokemobCap.getPokemob(i);
            if (stack == null) continue;
            IPokemob pokemob = PokecubeManager.itemToPokemob(stack, trainer.getEntityWorld());
            if (pokemob != null)
            {
                textFieldPokemobs[i].setText(pokemob.getPokedexEntry().getName());
                textFieldLevels[i].setText("" + pokemob.getLevel());
            }
        }

        textFieldName.setText(trainer.getCustomNameTag());
        textFieldType.setText(pokemobCap.getType().name);
    }

    private PokedexEntry getEntry(int num)
    {
        ItemStack stack = pokemobCap.getPokemob(num);
        if (PokecubeManager.isFilled(stack))
        {
            int number = PokecubeManager.getPokedexNb(stack);
            PokedexEntry ret = null;
            NBTTagCompound poketag = stack.getTagCompound().getCompoundTag("Pokemob");
            String forme = poketag.getString("forme");
            if (!forme.isEmpty())
            {
                ret = Database.getEntry(forme);
            }
            else
            {
                ret = Database.getEntry(number);
            }
            return ret;
        }
        return null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_UP)
        {
            for (int i = 0; i < 6; i++)
            {
                if (textFieldPokemobs[i].isFocused())
                {
                    String name = textFieldPokemobs[i].getText();
                    textFieldPokemobs[i].setText(Pokedex.getInstance().getNext(Database.getEntry(name), 1).getName());
                }
            }
        }
        else if (keyCode == Keyboard.KEY_DOWN)
        {
            for (int i = 0; i < 6; i++)
            {
                if (textFieldPokemobs[i].isFocused())
                {
                    String name = textFieldPokemobs[i].getText();
                    textFieldPokemobs[i]
                            .setText(Pokedex.getInstance().getPrevious(Database.getEntry(name), 1).getName());
                }
            }
        }
        if (typedChar == 13)
        {
            for (int i = 0; i < 6; i++)
            {
                if (textFieldPokemobs[i].isFocused())
                {
                    String name = textFieldPokemobs[i].getText();
                    PokedexEntry entry = Database.getEntry(name);
                    if (entry != null && entry != getEntry(i))
                    {
                        int level;
                        try
                        {
                            level = Integer.parseInt(textFieldLevels[i].getText());
                        }
                        catch (NumberFormatException e)
                        {
                            level = 1;
                        }
                        ItemStack newMob = TypeTrainer.makeStack(entry, trainer, trainer.getEntityWorld(), level);
                        pokemobCap.setPokemob(i, newMob);
                        textFieldPokemobs[i].setText(entry.getName());
                    }
                    else if (entry == null)
                    {
                        pokemobCap.setPokemob(i, CompatWrapper.nullStack);
                        textFieldPokemobs[i].setText("");
                    }
                }
                if (textFieldLevels[i].isFocused())
                {
                    PokedexEntry entry = getEntry(i);
                    if (entry != null)
                    {
                        int level = Integer.parseInt(textFieldLevels[i].getText());
                        int exp = Tools.levelToXp(entry.getEvolutionMode(), level);
                        ItemStack stack = pokemobCap.getPokemob(i);
                        NBTTagCompound pokemob = stack.getTagCompound().getCompoundTag("Pokemob");
                        pokemob.setInteger("exp", exp);
                        stack.getTagCompound().setTag("Pokemob", pokemob);
                    }
                }
            }
        }

        textFieldName.textboxKeyTyped(typedChar, keyCode);
        textFieldType.textboxKeyTyped(typedChar, keyCode);
        for (int i = 0; i < 6; i++)
        {
            textFieldPokemobs[i].textboxKeyTyped(typedChar, keyCode);
        }
        if (typedChar <= 57)
        {
            for (int i = 0; i < 6; i++)
            {
                textFieldLevels[i].textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        for (int i = 0; i < 6; i++)
        {
            textFieldLevels[i].mouseClicked(par1, par2, par3);
            textFieldPokemobs[i].mouseClicked(par1, par2, par3);
        }

        textFieldName.mouseClicked(par1, par2, par3);
        textFieldType.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
    }

    private void sendChooseToServer()
    {
        try
        {
            for (int i = 0; i < 6; i++)
            {
                String name = textFieldPokemobs[i].getText();
                PokedexEntry entry = Database.getEntry(name);
                if (entry != null && entry != getEntry(i))
                {
                    int level = Integer.parseInt(textFieldLevels[i].getText());
                    ItemStack newMob = TypeTrainer.makeStack(entry, trainer, trainer.getEntityWorld(), level);
                    pokemobCap.setPokemob(i, newMob);
                    textFieldPokemobs[i].setText(entry.getName());
                }
                else if (entry == null)
                {
                    pokemobCap.setPokemob(i, CompatWrapper.nullStack);
                    textFieldPokemobs[i].setText("");
                }
            }
            for (int i = 0; i < 6; i++)
            {
                PokedexEntry entry = getEntry(i);
                if (entry != null)
                {
                    int level = Integer.parseInt(textFieldLevels[i].getText());
                    int exp = Tools.levelToXp(entry.getEvolutionMode(), level);
                    ItemStack stack = pokemobCap.getPokemob(i);
                    NBTTagCompound pokemob = stack.getTagCompound().getCompoundTag("Pokemob")
                            .getCompoundTag(TagNames.POKEMOBTAG).getCompoundTag(TagNames.STATSTAG);
                    pokemob.setInteger(TagNames.EXP, exp);
                }
            }
            NBTTagCompound tag = new NBTTagCompound();
            trainer.writeEntityToNBT(tag);
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            packet.data.setString("N", textFieldName.getText());
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", trainer.getEntityId());
            packet.data.setBoolean("S", stationary);
            packet.data.setBoolean("R", resetTeam);
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
        catch (NumberFormatException e)
        {
        }
    }
}

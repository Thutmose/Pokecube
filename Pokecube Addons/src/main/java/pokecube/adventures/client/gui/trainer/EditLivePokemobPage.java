package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer.Page;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.client.gui.GuiPokemob;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;

public class EditLivePokemobPage extends Page
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    public static final int ENTRY       = 0;
    public static final int MOVE0       = 1;
    public static final int MOVE1       = 2;
    public static final int MOVE2       = 3;
    public static final int MOVE3       = 4;
    public static final int LEVEL       = 5;

    public static final int EV0         = 6;
    public static final int EV1         = 7;
    public static final int EV2         = 8;
    public static final int EV3         = 9;
    public static final int EV4         = 10;
    public static final int EV5         = 11;

    public static final int IV0         = 12;
    public static final int IV1         = 13;
    public static final int IV2         = 14;
    public static final int IV3         = 15;
    public static final int IV4         = 16;
    public static final int IV5         = 17;

    public static final int NATURE      = 18;
    public static final int ABILITY     = 19;
    public static final int SIZE        = 20;

    public static final int RANDMOVES   = 21;
    public static final int MAXIVS      = 22;
    public static final int RANDIVS     = 23;
    public static final int MINIVS      = 24;
    public static final int RANDABILITY = 25;
    public static final int RANDNATURE  = 26;

    IPokemob                pokemob;

    public EditLivePokemobPage(GuiEditTrainer parent, IPokemob pokemob)
    {
        super(parent);
        this.pokemob = pokemob;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = parent.width / 2;
        int y = parent.height / 2;
        GuiTextField field = null;
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
        textList.add(new GuiTextField(ENTRY, fontRenderer, x - 120, y - 55, 70, 10));
        for (int i = 0; i < 4; i++)
        {
            textList.add(new GuiTextField(i + 1, fontRenderer, x - 120, y - 30 + i * 10, 70, 10));
        }
        textList.add(field = new GuiTextField(LEVEL, fontRenderer, x - 120, y + 26, 27, 10));
        field.setValidator(intValid);
        int evivshiftx = 60;
        int evivshifty = -26;
        for (int i = EV0; i < EV0 + 6; i++)
        {
            int index = i - EV0;
            field = new GuiTextField(i, fontRenderer, x + evivshiftx + 30, y + evivshifty - 30 + index * 10, 30, 10);
            textList.add(field);
            field.setValidator(intValid);
        }
        for (int i = IV0; i < IV0 + 6; i++)
        {
            int index = i - IV0;
            field = new GuiTextField(i, fontRenderer, x + evivshiftx, y + evivshifty - 30 + index * 10, 20, 10);
            textList.add(field);
            field.setValidator(intValid);
        }
        textList.add(new GuiTextField(NATURE, fontRenderer, x - 120, y + 50, 50, 10));
        textList.add(new GuiTextField(ABILITY, fontRenderer, x - 60, y + 50, 90, 10));
        textList.add(field = new GuiTextField(SIZE, fontRenderer, x - 90, y + 26, 50, 10));
        field.setValidator(floatValid);

    }

    @Override
    protected void onPageOpened()
    {
        int level = 1;
        byte sexe = -1;
        float size = 1;
        boolean isShiny = false;
        String nature = "";
        String ability = "";
        String name = "none";
        PokedexEntry entry = null;
        if (pokemob != null)
        {
            entry = pokemob.getPokedexEntry();
            name = entry.getName();
            level = pokemob.getLevel();
            nature = pokemob.getNature() + "";
            if (pokemob.getAbility() != null) ability = pokemob.getAbility().toString();
            size = pokemob.getSize();
            sexe = pokemob.getSexe();
            isShiny = pokemob.isShiny();
        }

        int x = parent.width / 2;
        int y = parent.height / 2;

        // Init buttons
        String delete = I18n.format("traineredit.button.delete");
        parent.getButtons().add(new Button(1, x + 73, y + 64, 50, 12, delete));
        String gender = sexe == IPokemob.MALE ? "\u2642" : sexe == IPokemob.FEMALE ? "\u2640" : "o";
        parent.getButtons().add(new Button(2, x - 48, y - 60, 20, 20, gender));
        String shiny = isShiny ? "Y" : "N";
        parent.getButtons().add(new Button(3, x - 48, y - 30, 20, 20, shiny));
        parent.getButtons().add(new Button(RANDMOVES, x - 58, y - 41, 10, 10, "R"));
        parent.getButtons().add(new Button(MAXIVS, x + 38, y - 60, 20, 20, "^"));
        parent.getButtons().add(new Button(RANDIVS, x + 38, y - 40, 20, 20, "R"));
        parent.getButtons().add(new Button(MINIVS, x + 38, y - 20, 20, 20, "v"));
        parent.getButtons().add(new Button(RANDABILITY, x + 22, y + 39, 10, 10, "R"));
        parent.getButtons().add(new Button(RANDNATURE, x - 78, y + 39, 10, 10, "R"));
        parent.getButtons().add(new Button(99, x + 73, y + 24, 50, 12, I18n.format("traineredit.button.routes")));

        // Init values in text fields
        for (int i = 0; i < 4; i++)
        {
            String move = "";
            if (pokemob != null)
            {
                move = pokemob.getMove(i);
                if (move == null) move = "";
            }
            textList.get(i + 1).setText(move);
            textList.get(i + 1).moveCursorBy(-100);
        }
        for (int i = EV0; i < EV0 + 6; i++)
        {
            int index = i - EV0;
            int value = 0;
            if (pokemob != null)
            {
                value = pokemob.getEVs()[index] - Byte.MIN_VALUE;
            }
            textList.get(i).setText("" + value);
        }
        for (int i = IV0; i < IV0 + 6; i++)
        {
            int index = i - IV0;
            int value = 0;
            if (pokemob != null)
            {
                value = pokemob.getIVs()[index];
            }
            textList.get(i).setText("" + value);
        }
        textList.get(NATURE).setText("" + nature);
        textList.get(ABILITY).setText("" + ability);
        textList.get(SIZE).setText("" + size);
        textList.get(LEVEL).setText("" + level);
        textList.get(ENTRY).setText(name);

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
        drawCenteredString(fontRenderer, I18n.format("traineredit.title.pokemob"), x, y, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.pokemob"), x - 120, y + 5, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.moves"), x - 120, y + 30, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.shiny"), x - 46, y + 30, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.level"), x - 120, y + 85, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.size"), x - 90, y + 85, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.nature"), x - 120, y + 110, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.ability"), x - 60, y + 110, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.evs"), x + 90, y, 0xFFFFFFFF);
        drawString(fontRenderer, I18n.format("traineredit.info.ivs"), x + 60, y, 0xFFFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        boolean[] active = new boolean[textList.size()];
        for (int i = 0; i < active.length; i++)
        {
            active[i] = textList.get(i).isFocused();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        GuiTextField field;
        if (pokemob == null)
        {
            field = textList.get(ENTRY);
            if (field.isFocused())
            {
                setEntry(field.getText());
            }
            return;
        }
        for (int i = 0; i < active.length; i++)
        {
            if (active[i] && !textList.get(i).isFocused())
            {
                field = textList.get(i);
                String value = field.getText();
                switch (i)
                {
                case ENTRY:
                    setEntry(value);
                    break;
                case LEVEL:
                    setLevel(value);
                    break;
                case NATURE:
                    setNature(value);
                    break;
                case SIZE:
                    setSize(value);
                    break;
                case ABILITY:
                    setAbility(value);
                    break;

                // Set moves
                case MOVE0:
                    setMove(0, value);
                    break;
                case MOVE1:
                    setMove(1, value);
                    break;
                case MOVE2:
                    setMove(2, value);
                    break;
                case MOVE3:
                    setMove(3, value);
                    break;

                // Set EVs
                case EV0:
                    setEV(0, value);
                    break;
                case EV1:
                    setEV(1, value);
                    break;
                case EV2:
                    setEV(2, value);
                    break;
                case EV3:
                    setEV(3, value);
                    break;
                case EV4:
                    setEV(4, value);
                    break;
                case EV5:
                    setEV(5, value);
                    break;

                // Set IVs
                case IV0:
                    setIV(0, value);
                    break;
                case IV1:
                    setIV(1, value);
                    break;
                case IV2:
                    setIV(2, value);
                    break;
                case IV3:
                    setIV(3, value);
                    break;
                case IV4:
                    setIV(4, value);
                    break;
                case IV5:
                    setIV(5, value);
                    break;
                }
                break;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        ITextComponent mess;
        switch (button.id)
        {
        case 0:
            parent.mainPage.initList();
            parent.setIndex(0);
            break;
        case 99:
            parent.routesPage.scroll = true;
            parent.setIndex(parent.routesPage.index);
            return;
        case 1:
            pokemob.getEntity().setDead();
            mess = new TextComponentTranslation("traineredit.info.deletemob");
            parent.mc.player.sendStatusMessage(mess, true);
            // Send kill mob packet
            sendUpdate();
            break;
        case 2:
            if (pokemob == null) break;
            // Toggle gender
            byte sexe = pokemob.getSexe();
            sexe = sexe == IPokemob.MALE ? IPokemob.FEMALE : sexe == IPokemob.FEMALE ? IPokemob.MALE : sexe;
            pokemob.setSexe(sexe);
            sendUpdate();
            break;
        case 3:
            if (pokemob == null) break;
            // toggle shiny
            pokemob.setShiny(!pokemob.isShiny());
            sendUpdate();
            break;
        case RANDMOVES:
            if (pokemob == null) break;
            List<String> moves = Lists.newArrayList(pokemob.getPokedexEntry().getMovesForLevel(pokemob.getLevel()));
            if (!moves.isEmpty())
            {
                Collections.shuffle(moves);
                for (int i = 0; i < Math.min(4, moves.size()); i++)
                {
                    setMove(i, moves.get(i));
                }
                sendUpdate();
            }
            break;
        case RANDIVS:
            if (pokemob == null) break;
            for (int i = 0; i < 6; i++)
                setIV(i, new Random().nextInt(32) + "");
            sendUpdate();
            break;
        case MAXIVS:
            if (pokemob == null) break;
            for (int i = 0; i < 6; i++)
                setIV(i, "31");
            sendUpdate();
            break;
        case MINIVS:
            if (pokemob == null) break;
            for (int i = 0; i < 6; i++)
                setIV(i, "0");
            sendUpdate();
            break;
        case RANDABILITY:
            if (pokemob == null) break;
            int num = new Random().nextInt(3);
            pokemob.setAbility(null);
            pokemob.setAbilityIndex(num);
            sendUpdate();
            break;
        case RANDNATURE:
            if (pokemob == null) break;
            Nature nature = Nature.values()[new Random().nextInt(Nature.values().length)];
            pokemob.setNature(nature);
            sendUpdate();
            break;
        }

    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (keyCode != Keyboard.KEY_RETURN) return;
        GuiTextField field;
        if (pokemob == null)
        {
            field = textList.get(ENTRY);
            if (field.isFocused())
            {
                setEntry(field.getText());
            }
            return;
        }

        for (int i = 0; i < textList.size(); i++)
        {
            field = textList.get(i);
            if (!field.isFocused()) continue;
            String value = field.getText();
            switch (i)
            {
            case ENTRY:
                setEntry(value);
                break;
            case LEVEL:
                setLevel(value);
                break;
            case NATURE:
                setNature(value);
                break;
            case SIZE:
                setSize(value);
                break;
            case ABILITY:
                setAbility(value);
                break;

            // Set moves
            case MOVE0:
                setMove(0, value);
                break;
            case MOVE1:
                setMove(1, value);
                break;
            case MOVE2:
                setMove(2, value);
                break;
            case MOVE3:
                setMove(3, value);
                break;

            // Set EVs
            case EV0:
                setEV(0, value);
                break;
            case EV1:
                setEV(1, value);
                break;
            case EV2:
                setEV(2, value);
                break;
            case EV3:
                setEV(3, value);
                break;
            case EV4:
                setEV(4, value);
                break;
            case EV5:
                setEV(5, value);
                break;

            // Set IVs
            case IV0:
                setIV(0, value);
                break;
            case IV1:
                setIV(1, value);
                break;
            case IV2:
                setIV(2, value);
                break;
            case IV3:
                setIV(3, value);
                break;
            case IV4:
                setIV(4, value);
                break;
            case IV5:
                setIV(5, value);
                break;
            }
        }
    }

    private void setAbility(String value)
    {
        Ability ability = AbilityManager.getAbility(value);
        ITextComponent mess;
        if (ability != null)
        {
            mess = new TextComponentTranslation("traineredit.set.ability", I18n.format(ability.getName()));
            pokemob.setAbility(ability);
            sendUpdate();
        }
        else
        {
            mess = new TextComponentTranslation("traineredit.info.invalidability", value);
        }
        parent.mc.player.sendStatusMessage(mess, true);
    }

    private void setEntry(String value)
    {
        PokedexEntry entry = Database.getEntry(value);
        if (entry != null) pokemob = pokemob.setPokedexEntry(entry);
    }

    private void setSize(String value)
    {
        float size = value.isEmpty() ? 0.01f : Float.parseFloat(value);
        size = Math.max(0.01f, size);
        pokemob.setSize(size);
        ITextComponent mess = new TextComponentTranslation("traineredit.set.size", pokemob.getSize());
        sendUpdate();
        parent.mc.player.sendStatusMessage(mess, true);
    }

    private void setNature(String value)
    {
        Nature nature = Nature.valueOf(value.toUpperCase(Locale.ENGLISH));
        ITextComponent mess;
        if (nature != null)
        {
            mess = new TextComponentTranslation("traineredit.set.nature", nature);
            pokemob.setNature(nature);
            sendUpdate();
        }
        else
        {
            mess = new TextComponentTranslation("traineredit.info.invalidnature", value);
        }
        parent.mc.player.sendStatusMessage(mess, true);
    }

    private void setLevel(String value)
    {
        if (value.isEmpty())
        {
            String level = pokemob.getLevel() + "";
            textList.get(LEVEL).setText(level);
        }
        else
        {
            int level = Integer.parseInt(value);
            int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob.setExp(exp, false);
            ITextComponent mess = new TextComponentTranslation("traineredit.set.level", pokemob.getLevel());
            sendUpdate();
            parent.mc.player.sendStatusMessage(mess, true);
        }
    }

    private void setIV(int index, String value)
    {
        if (value.isEmpty())
        {
            value = pokemob.getIVs()[index] + "";
            textList.get(IV0 + index).setText(value);
        }
        else
        {
            int iv = Integer.parseInt(value);
            iv = Math.max(0, iv);
            iv = Math.min(31, iv);
            pokemob.getIVs()[index] = (byte) iv;
            ITextComponent mess = new TextComponentTranslation("traineredit.set.ivs");
            sendUpdate();
            parent.mc.player.sendStatusMessage(mess, true);
            value = pokemob.getIVs()[index] + "";
            textList.get(IV0 + index).setText(value);
        }
    }

    private void setEV(int index, String value)
    {
        if (value.isEmpty())
        {
            value = pokemob.getIVs()[index] + "";
            textList.get(EV0 + index).setText(value);
        }
        else
        {
            int ev = Integer.parseInt(value);
            ev = Math.max(0, ev);
            ev = Math.min(255, ev);
            pokemob.getEVs()[index] = (byte) (ev + Byte.MIN_VALUE);
            ITextComponent mess = new TextComponentTranslation("traineredit.set.evs");
            sendUpdate();
            parent.mc.player.sendStatusMessage(mess, true);
            value = (pokemob.getEVs()[index] - Byte.MIN_VALUE) + "";
            textList.get(EV0 + index).setText(value);
        }
    }

    private void setMove(int index, String move)
    {
        Move_Base attack = MovesUtils.moves.get(move.toLowerCase(Locale.ENGLISH).replaceAll("(\\W)", ""));
        ITextComponent mess;
        if (attack == null && !move.isEmpty())
        {
            mess = new TextComponentTranslation("traineredit.info.invalidmove", move);
        }
        else
        {
            move = pokemob.getMove(index);
            String[] moves = pokemob.getMoves();
            moves[index] = attack == null ? null : attack.name;
            pokemob.setMoves(moves);
            if (attack != null)
                mess = new TextComponentTranslation("traineredit.set.move", MovesUtils.getMoveName(attack.name));
            else if (move != null) mess = new TextComponentTranslation("traineredit.set.removemove", move);
            else return;
            sendUpdate();
        }
        parent.mc.player.sendStatusMessage(mess, true);
    }

    private void sendUpdate()
    {
        this.onPageClosed();
        PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATEMOB);
        NBTBase tag = PokecubeManager.pokemobToItem(pokemob).writeToNBT(new NBTTagCompound());
        if (tag != null)
        {
            packet.data.setTag("T", tag);
            packet.data.setInteger("I", pokemob.getEntity().getEntityId());
            if (pokemob.getEntity().isDead) packet.data.setBoolean("D", true);
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
        this.onPageOpened();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (pokemob != null)
        {
            int x = (parent.width - 256) / 2 + 80;
            int y = (parent.height - 160) / 2 + 40;
            pokemob.getEntity().ticksExisted = mc.player.ticksExisted;
            float theta = 0;
            pokemob.getEntity().prevRenderYawOffset = theta;
            pokemob.getEntity().renderYawOffset = theta;
            pokemob.getEntity().prevRotationYawHead = theta;
            pokemob.getEntity().rotationYawHead = theta;
            GuiPokemob.renderMob(pokemob, x, y, 0, 0, 0, theta, 0, 1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTitle(mouseX, mouseY, partialTicks);
    }
}

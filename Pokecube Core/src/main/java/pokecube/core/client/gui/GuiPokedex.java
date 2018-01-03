/**
 *
 */
package pokecube.core.client.gui;

import static pokecube.core.utils.PokeType.getTranslatedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.Resources;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;
import thut.api.terrain.BiomeDatabase;

public class GuiPokedex extends GuiScreen
{
    public static PokedexEntry                    pokedexEntry       = null;

    private static final int                      PAGECOUNT          = 4;

    private static HashMap<Integer, EntityLiving> entityToDisplayMap = new HashMap<Integer, EntityLiving>();

    /** to pass as last parameter when rendering the mob so that the render
     * knows the rendering is asked by the pokedex gui */
    public final static float                     POKEDEX_RENDER     = 1.5f;

    public IPokemob                               pokemob            = null;
    protected EntityPlayer                        entityPlayer       = null;
    protected GuiTextField                        nicknameTextField;
    protected GuiTextField                        pokemobTextField;
    /** The X size of the inventory window in pixels. */
    protected int                                 xSize;
    /** The Y size of the inventory window in pixels. */
    protected int                                 ySize;

    private float                                 xRenderAngle       = 0;
    private float                                 yHeadRenderAngle   = 10;
    private float                                 xHeadRenderAngle   = 0;
    private int                                   mouseRotateControl;
    private int                                   page               = 0;
    private int                                   index              = 0;
    private boolean                               mode               = false;

    int                                           prevX              = 0;
    int                                           prevY              = 0;

    /**
     *
     */
    public GuiPokedex(IPokemob pokemob, EntityPlayer entityPlayer)
    {
        xSize = 256;
        ySize = 197;
        this.pokemob = pokemob;
        this.entityPlayer = entityPlayer;
        ItemStack item = entityPlayer.getHeldItemMainhand();
        if (item != null)
        {
            page = item.getItemDamage();
            if (item.hasTagCompound())
            {
                mode = item.getTagCompound().getBoolean("M");
            }
        }

        if (pokemob != null)
        {
            pokedexEntry = pokemob.getPokedexEntry();
        }
        else
        {
            if (item.hasTagCompound())
            {
                pokedexEntry = Database.getEntry(item.getTagCompound().getString("F"));
            }
            if (pokedexEntry == null) pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }
        PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUEST);
        packet.data.setBoolean("M", mode);
        packet.data.setString("F", pokedexEntry.getName());
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    private boolean canEditPokemob()
    {
        return pokemob != null && pokedexEntry.getPokedexNb() == pokemob.getPokedexNb()
                && ((pokemob.getPokemonAIState(IMoveConstants.TAMED) && entityPlayer == pokemob.getPokemonOwner())
                        || entityPlayer.capabilities.isCreativeMode);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    /** Draws the background (i is always 0 as of 1.2.2) */
    public void drawBackground(int tint)
    {
        super.drawBackground(tint);
    }

    /** Draws the first page of the pokedex, this is the page with the pokemob's
     * stats and current moves, if no pokemob is selected, it tries to show an
     * arrow to the nearest village
     * 
     * @param xOffset
     * @param yOffset */
    private void drawPage0(int xOffset, int yOffset)
    {
        if (pokemob != null && pokemob.getPokedexNb() == pokedexEntry.getPokedexNb())
        {
            int genderColor = 0xBBBBBB;
            String gender = "";

            if (pokemob.getSexe() == IPokemob.MALE)
            {
                genderColor = 0x0011CC;
                gender = "\u2642";
            }
            else if (pokemob.getSexe() == IPokemob.FEMALE)
            {
                genderColor = 0xCC5555;
                gender = "\u2640";
            }

            String level = "L. " + pokemob.getLevel();
            drawString(fontRenderer, level, xOffset + 15, yOffset + 1, 0xffffff);
            drawCenteredString(fontRenderer, gender, xOffset - 20, yOffset + 122, genderColor);
            byte[] nature = pokemob.getNature().getStatsMod();

            if (canEditPokemob() || PokecubeMod.debug)
            {
                int HP = pokemob.getStat(Stats.HP, true);
                int ATT = pokemob.getStat(Stats.ATTACK, true);
                int DEF = pokemob.getStat(Stats.DEFENSE, true);
                int ATTSPE = pokemob.getStat(Stats.SPATTACK, true);
                int DEFSPE = pokemob.getStat(Stats.SPDEFENSE, true);
                int VIT = pokemob.getStat(Stats.VIT, true);
                int statYOffSet = yOffset + 15;
                String[] nat = new String[6];
                int[] colours = new int[6];
                for (int n = 0; n < 6; n++)
                {
                    nat[n] = "";
                    colours[n] = 0;
                    if (nature[n] == -1)
                    {
                        nat[n] = "-";
                        colours[n] = 0x6890F0;
                    }
                    if (nature[n] == 1)
                    {
                        nat[n] = "+";
                        colours[n] = 0xFF0000;
                    }
                }
                drawString(fontRenderer, "HP", xOffset + 20, statYOffSet + 18, 0xFF0000);
                drawString(fontRenderer, "ATT", xOffset + 20, statYOffSet + 27, 0xF08030);
                drawString(fontRenderer, "DEF", xOffset + 20, statYOffSet + 36, 0xF8D030);
                drawString(fontRenderer, "ATTSPE", xOffset + 20, statYOffSet + 45, 0x6890F0);
                drawString(fontRenderer, "DEFSPE", xOffset + 20, statYOffSet + 54, 0x78C850);
                drawString(fontRenderer, "VIT", xOffset + 20, statYOffSet + 63, 0xF85888);
                drawString(fontRenderer, ": " + HP, xOffset + 60, statYOffSet + 18, 0xFF0000);
                drawString(fontRenderer, ": " + ATT, xOffset + 60, statYOffSet + 27, 0xF08030);
                drawString(fontRenderer, ": " + DEF, xOffset + 60, statYOffSet + 36, 0xF8D030);
                drawString(fontRenderer, ": " + ATTSPE, xOffset + 60, statYOffSet + 45, 0x6890F0);
                drawString(fontRenderer, ": " + DEFSPE, xOffset + 60, statYOffSet + 54, 0x78C850);
                drawString(fontRenderer, ": " + VIT, xOffset + 60, statYOffSet + 63, 0xF85888);

                drawString(fontRenderer, nat[0], xOffset + 100, statYOffSet + 18, colours[0]);
                drawString(fontRenderer, nat[1], xOffset + 100, statYOffSet + 27, colours[1]);
                drawString(fontRenderer, nat[2], xOffset + 100, statYOffSet + 36, colours[2]);
                drawString(fontRenderer, nat[3], xOffset + 100, statYOffSet + 45, colours[3]);
                drawString(fontRenderer, nat[4], xOffset + 100, statYOffSet + 54, colours[4]);
                drawString(fontRenderer, nat[5], xOffset + 100, statYOffSet + 63, colours[5]);

                statYOffSet += 75;
                drawString(fontRenderer, "IV       EV", xOffset + 67, statYOffSet + 9, 0xFFFFFF);
                drawString(fontRenderer, "HP", xOffset + 20, statYOffSet + 18, 0xFF0000);
                drawString(fontRenderer, "ATT", xOffset + 20, statYOffSet + 27, 0xF08030);
                drawString(fontRenderer, "DEF", xOffset + 20, statYOffSet + 36, 0xF8D030);
                drawString(fontRenderer, "ATTSPE", xOffset + 20, statYOffSet + 45, 0x6890F0);
                drawString(fontRenderer, "DEFSPE", xOffset + 20, statYOffSet + 54, 0x78C850);
                drawString(fontRenderer, "VIT", xOffset + 20, statYOffSet + 63, 0xF85888);
                byte[] stats2 = pokemob.getIVs();
                HP = stats2[0];
                ATT = stats2[1];
                DEF = stats2[2];
                ATTSPE = stats2[3];
                DEFSPE = stats2[4];
                VIT = stats2[5];
                stats2 = pokemob.getEVs();
                int HP2 = stats2[0] + 128;
                int ATT2 = stats2[1] + 128;
                int DEF2 = stats2[2] + 128;
                int ATTSPE2 = stats2[3] + 128;
                int DEFSPE2 = stats2[4] + 128;
                int VIT2 = stats2[5] + 128;

                int shift = 60;
                drawString(fontRenderer, ": " + HP, xOffset + shift, statYOffSet + 18, 0xFF0000);
                drawString(fontRenderer, ": " + ATT, xOffset + shift, statYOffSet + 27, 0xF08030);
                drawString(fontRenderer, ": " + DEF, xOffset + shift, statYOffSet + 36, 0xF8D030);
                drawString(fontRenderer, ": " + ATTSPE, xOffset + shift, statYOffSet + 45, 0x6890F0);
                drawString(fontRenderer, ": " + DEFSPE, xOffset + shift, statYOffSet + 54, 0x78C850);
                drawString(fontRenderer, ": " + VIT, xOffset + shift, statYOffSet + 63, 0xF85888);
                shift = 105;
                drawString(fontRenderer, "" + HP2, xOffset + shift, statYOffSet + 18, 0xFF0000);
                drawString(fontRenderer, "" + ATT2, xOffset + shift, statYOffSet + 27, 0xF08030);
                drawString(fontRenderer, "" + DEF2, xOffset + shift, statYOffSet + 36, 0xF8D030);
                drawString(fontRenderer, "" + ATTSPE2, xOffset + shift, statYOffSet + 45, 0x6890F0);
                drawString(fontRenderer, "" + DEFSPE2, xOffset + shift, statYOffSet + 54, 0x78C850);
                drawString(fontRenderer, "" + VIT2, xOffset + shift, statYOffSet + 63, 0xF85888);
            }
        }
        else
        {
            int num = Pokedex.getInstance().getIndex(pokedexEntry) + 1;
            String level = "N. " + num;
            drawString(fontRenderer, level, xOffset + 15, yOffset + 1, 0xffffff);
        }
    }

    /** Draws the learnable moves page.
     * 
     * @param xOffset
     * @param yOffset */
    private void drawPage1(int xOffset, int yOffset)
    {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> levels = new ArrayList<String>();
        if (pokedexEntry != null)
        {
            for (int i = 0; i < 100; i++)
            {
                List<String> moves = pokedexEntry.getMovesForLevel(i, i - 1);
                for (String s : moves)
                {
                    levels.add("" + i);
                    names.add(s);
                }
            }
            List<String> allMoves = pokedexEntry.getMoves();
            for (String s : allMoves)
            {
                if (!names.contains(s))
                {
                    levels.add("TM");
                    names.add(s);
                }
            }
        }
        index = Math.max(0, Math.min(index, names.size() - 6));
        int n;
        drawString(fontRenderer, "Moves", xOffset + 16, yOffset + 15, 0xFFFFFF);
        for (n = 0; n < Math.min(names.size(), 6); n++)
        {
            drawString(fontRenderer, MovesUtils.getMoveName(names.get(n + index)).getFormattedText(), xOffset + 18,
                    yOffset + 30 + n * 10, 0xFF0000);
            drawString(fontRenderer, levels.get(n + index), xOffset + 92, yOffset + 30 + n * 10, 0xFF0000);
        }

        drawSelectedMoves(xOffset + 5, yOffset + 0);
    }

    /** Draws the biome info page, as well as the various statistics at the
     * bottom
     * 
     * @param xOffset
     * @param yOffset */
    private void drawPage2(int xOffset, int yOffset)
    {
        int listSize = mode ? 6 : 8;
        mode:
        if (!mode)
        {
            List<String> names = PacketPokedex.values;
            if (names.isEmpty()) break mode;
            int num = -1;
            try
            {
                num = Integer.parseInt(names.get(0));
            }
            catch (NumberFormatException e)
            {
                break mode;
            }
            index = Math.max(0, Math.min(index, names.size() - listSize));
            String title = BiomeDatabase.getReadableNameFromType(num);
            if (title.equals("none")) title = names.get(1);
            if (title.equalsIgnoreCase("mushroomislandshore")) title = "Mushroom Shore";
            if (title.equalsIgnoreCase("birch forest hills m")) title = "Birch ForestHills M";
            drawString(fontRenderer, title, xOffset + 16, yOffset + 15, 0xFFFFFF);
            for (int n = 2; n < Math.min(names.size(), listSize); n++)
            {
                int yO = yOffset + 10;
                String[] var = names.get((n + index)).split("`");
                String name = var[0];
                String numbers = "";
                if (var.length > 1)
                {
                    numbers = var[1];
                    float value;
                    try
                    {
                        value = Float.parseFloat(numbers);
                        if (value < 0.001)
                        {
                            numbers = ">0.01%";
                        }
                        else
                        {
                            numbers = numbers.substring(0, Math.min(5, numbers.length()));
                            numbers = numbers + "%";
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        numbers = numbers.substring(0, Math.min(5, numbers.length()));
                        if (numbers.equals(" 0.00"))
                        {
                            numbers = ">0.01%";
                        }
                        else
                        {
                            numbers = numbers + "%";
                        }
                    }
                }
                drawString(fontRenderer, I18n.format(name), xOffset + 18, yO + n * 10, 0xFF0000);
                drawString(fontRenderer, numbers, xOffset + 108 - fontRenderer.getStringWidth(numbers), yO + n * 10,
                        0xFF0000);
                String time = "";
                drawString(fontRenderer, time, xOffset + 85, yO + n * 10, 0xFF0000);
            }
        }
        else if (getEntityToDisplay() != null)
        {
            drawString(fontRenderer, getEntityToDisplay().getDisplayName().getFormattedText(), xOffset + 16,
                    yOffset + 15, 0xFFFFFF);
            List<String> biomes = PacketPokedex.values;
            try
            {
                Integer.parseInt(biomes.get(0));
                break mode;
            }
            catch (Exception e)
            {
            }
            index = Math.max(0, Math.min(index, biomes.size() - listSize));
            for (int n = 0; n < Math.min(biomes.size(), listSize); n++)
            {
                String s = biomes.get(n + index);
                int yO = yOffset + 30;
                int colour = 0xFF0000;
                if (n % 2 == 0) colour = 0xFF3300;
                s = fontRenderer.trimStringToWidth(s, 90);
                fontRenderer.drawString(s, xOffset + 18, yO + n * 10, colour);
            }
        }

        drawString(fontRenderer, "User Stats", xOffset + 19, yOffset + 99, 0xFFFFFF);

        int count = KillStats.getNumberUniqueKilledBy(entityPlayer.getUniqueID());
        if (mode)
        {
            count = KillStats.getTotalNumberOfPokemobKilledBy(entityPlayer.getUniqueID(), pokedexEntry);
        }
        int count2 = KillStats.getTotalNumberKilledBy(entityPlayer.getUniqueID());

        drawString(fontRenderer, "Kills", xOffset + 19, yOffset + 112, 0xFFFFFF);
        drawString(fontRenderer, count + "/" + count2,
                xOffset + 120 - fontRenderer.getStringWidth((count + "/" + count2)), yOffset + 112, 0xffffff);

        count = CaptureStats.getNumberUniqueCaughtBy(entityPlayer.getUniqueID());
        if (mode)
        {
            count = CaptureStats.getTotalNumberOfPokemobCaughtBy(entityPlayer.getUniqueID(), pokedexEntry);
        }
        count2 = CaptureStats.getTotalNumberCaughtBy(entityPlayer.getUniqueID());
        drawString(fontRenderer, "Captures", xOffset + 19, yOffset + 126, 0xFFFFFF);
        drawString(fontRenderer, count + "/" + count2,
                xOffset + 120 - fontRenderer.getStringWidth((count + "/" + count2)), yOffset + 126, 0xffffff);

        count = EggStats.getNumberUniqueHatchedBy(entityPlayer.getUniqueID());
        if (mode)
        {
            count = EggStats.getTotalNumberOfPokemobHatchedBy(entityPlayer.getUniqueID(), pokedexEntry);
        }
        count2 = EggStats.getTotalNumberHatchedBy(entityPlayer.getUniqueID());
        drawString(fontRenderer, "Hatched", xOffset + 19, yOffset + 140, 0xFFFFFF);
        drawString(fontRenderer, count + "/" + count2,
                xOffset + 120 - fontRenderer.getStringWidth((count + "/" + count2)), yOffset + 140, 0xffffff);
        World world = entityPlayer.getEntityWorld();
        List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
        count = 0;
        count2 = 0;
        for (Entity o : entities)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(o);
            if (pokemob != null)
            {
                if (!mode) count++;
                else if (pokemob.getPokedexEntry().getPokedexNb() == pokedexEntry.getPokedexNb()) count++;
            }
        }
        drawString(fontRenderer, "Around", xOffset + 19, yOffset + 154, 0xFFFFFF);
        drawString(fontRenderer, "" + count, xOffset + 120 - fontRenderer.getStringWidth((count + "")), yOffset + 154,
                0xffffff);
    }

    /** Draws the breeding information page
     * 
     * @param xOffset
     * @param yOffset */
    private void drawPage3(int xOffset, int yOffset)
    {
        ArrayList<String> names = new ArrayList<String>();
        if (pokedexEntry != null)
        {
            for (PokedexEntry p : pokedexEntry.getRelated())
            {
                if (p != null) names.add(I18n.format(p.getUnlocalizedName()));
            }
        }
        index = Math.max(0, Math.min(index, names.size() - 6));
        int n;
        drawString(fontRenderer, "Possible Mates", xOffset + 16, yOffset + 15, 0xFFFFFF);
        for (n = 0; n < Math.min(names.size(), 6); n++)
        {
            drawString(fontRenderer, names.get(n + index), xOffset + 18, yOffset + 30 + n * 10, 0xFF0000);
        }
        if (pokemob != null)
        {
            Ability ability = pokemob.getAbility();
            if (ability != null)
            {
                drawString(fontRenderer, "AB: " + I18n.format(ability.getName()), xOffset + 19, yOffset + 99, 0xFFFFFF);
            }
            int happiness = pokemob.getHappiness();
            String message = "";
            if (happiness == 0)
            {
                message = "pokemob.info.happy0";
            }
            if (happiness > 0)
            {
                message = "pokemob.info.happy1";
            }
            if (happiness > 49)
            {
                message = "pokemob.info.happy2";
            }
            if (happiness > 99)
            {
                message = "pokemob.info.happy3";
            }
            if (happiness > 149)
            {
                message = "pokemob.info.happy4";
            }
            if (happiness > 199)
            {
                message = "pokemob.info.happy5";
            }
            if (happiness > 254)
            {
                message = "pokemob.info.happy6";
            }
            message = I18n.format(message);
            fontRenderer.drawSplitString(message, xOffset + 19, yOffset + 145, 100, 0xFFFFFF);

            int dy = 122;
            message = "Size: " + pokemob.getSize();
            fontRenderer.drawSplitString(message, xOffset + 19, yOffset + dy, 100, 0xFFFFFF);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);

        Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();

        minecraft.renderEngine.bindTexture(Resources.GUI_POKEDEX);
        int j2 = (width - xSize) / 2;
        int k2 = (height - ySize) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, xSize, ySize);

        GL11.glPushMatrix();

        int yOffset = height / 2 - 80;
        int xOffset = width / 2;

        GL11.glPushMatrix();
        renderMob(getEntityToDisplay(), mc, 0, 0, 1, height, width, xSize, ySize, xHeadRenderAngle, yHeadRenderAngle,
                0);
        GL11.glPopMatrix();
        nicknameTextField.drawTextBox();
        int length = fontRenderer.getStringWidth(pokemobTextField.getText()) / 2;
        pokemobTextField.x -= length;
        pokemobTextField.drawTextBox();
        pokemobTextField.x += length;
        drawCenteredString(fontRenderer, "" + (page + 1), xOffset + 55, yOffset + 1, 0xffffff);
        if (page == 0)
        {
            drawPage0(xOffset, yOffset);
        }
        else if (page == 1)
        {
            drawPage1(xOffset, yOffset);
        }
        else if (page == 2)
        {
            drawPage2(xOffset, yOffset);
        }
        else if (page == 3)
        {
            drawPage3(xOffset, yOffset);
        }
        int nb = pokedexEntry != null ? pokedexEntry.getPokedexNb() : 0;
        PokeType type1 = pokemob != null && pokedexEntry == pokemob.getPokedexEntry() ? pokemob.getType1()
                : pokedexEntry != null ? pokedexEntry.getType1() : PokeType.unknown;
        PokeType type2 = pokemob != null && pokedexEntry == pokemob.getPokedexEntry() ? pokemob.getType2()
                : pokedexEntry != null ? pokedexEntry.getType2() : PokeType.unknown;
        drawCenteredString(fontRenderer, "#" + nb, xOffset - 28, yOffset + 02, 0xffffff);
        try
        {
            drawCenteredString(fontRenderer, getTranslatedName(type1), xOffset - 88, yOffset + 137, type1.colour);
            drawCenteredString(fontRenderer, getTranslatedName(type2), xOffset - 44, yOffset + 137, type2.colour);
        }
        catch (Exception e)
        {
            // System.out.println(pokedexEntry);
            // e.printStackTrace();
        }
        GL11.glPopMatrix();
    }

    /** Used to draw in the list of moves that the pokemob has in the bottom
     * section of some pages
     * 
     * @param xOffset
     * @param yOffset */
    public void drawSelectedMoves(int xOffset, int yOffset)
    {
        if (pokemob == null || !canEditPokemob()) return;

        Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(0));

        String pwr = "";

        if (move != null)
        {
            if (move.getPWR(pokemob, entityPlayer) > 0)
            {
                pwr = "" + move.getPWR(pokemob, entityPlayer);
            }
            else
            {
                pwr = "-";
            }
            drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), xOffset + 14,
                    yOffset + 99, move.getType(pokemob).colour);
            drawString(fontRenderer, "" + pwr, xOffset + 102, yOffset + 99, 0xffffff);
        }

        move = MovesUtils.getMoveFromName(pokemob.getMove(1));

        if (move != null)
        {
            if (move.getPWR(pokemob, entityPlayer) > 0)
            {
                pwr = "" + move.getPWR(pokemob, entityPlayer);
            }
            else
            {
                pwr = "-";
            }
            drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), xOffset + 14,
                    yOffset + 113, move.getType(pokemob).colour);
            drawString(fontRenderer, "" + pwr, xOffset + 102, yOffset + 113, 0xffffff);
        }

        move = MovesUtils.getMoveFromName(pokemob.getMove(2));

        if (move != null)
        {
            if (move.getPWR(pokemob, entityPlayer) > 0)
            {
                pwr = "" + move.getPWR(pokemob, entityPlayer);
            }
            else
            {
                pwr = "-";
            }
            drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), xOffset + 14,
                    yOffset + 127, move.getType(pokemob).colour);
            drawString(fontRenderer, "" + pwr, xOffset + 102, yOffset + 127, 0xffffff);
        }

        move = MovesUtils.getMoveFromName(pokemob.getMove(3));

        if (move != null)
        {
            if (move.getPWR(pokemob, entityPlayer) > 0)
            {
                pwr = "" + move.getPWR(pokemob, entityPlayer);
            }
            else
            {
                pwr = "-";
            }
            drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), xOffset + 14,
                    yOffset + 141, move.getType(pokemob).colour);
            drawString(fontRenderer, "" + pwr, xOffset + 102, yOffset + 141, 0xffffff);
        }

        move = MovesUtils.getMoveFromName(pokemob.getMove(4));

        if (move != null)
        {
            if (move.getPWR(pokemob, entityPlayer) > 0)
            {
                pwr = "" + move.getPWR(pokemob, entityPlayer);
            }
            else
            {
                pwr = "-";
            }
            drawString(fontRenderer, MovesUtils.getMoveName(move.getName()).getFormattedText(), xOffset + 14,
                    yOffset + 155, move.getType(pokemob).colour);
            drawString(fontRenderer, "" + pwr, xOffset + 102, yOffset + 155, 0xffffff);
        }
    }

    @Override
    public void drawWorldBackground(int tint)
    {
        super.drawWorldBackground(tint);
    }

    private int getButtonId(int x, int y)
    {
        int xConv = x - ((width - xSize) / 2) - 74;
        int yConv = y - ((height - ySize) / 2) - 107;
        int button = 0;

        if (xConv >= 37 && xConv <= 42 && yConv >= 63 && yConv <= 67)
        {
            button = 1;// Next
        }
        else if (xConv >= 25 && xConv <= 30 && yConv >= 63 && yConv <= 67)
        {
            button = 2;// Previous
        }
        else if (xConv >= 32 && xConv <= 36 && yConv >= 58 && yConv <= 63)
        {
            button = 3;// Next 10
        }
        else if (xConv >= 32 && xConv <= 36 && yConv >= 69 && yConv <= 73)
        {
            button = 4;// Previous 10
        }
        else if (xConv >= -65 && xConv <= -58 && yConv >= 65 && yConv <= 72)
        {
            button = 5;// Sound
        }
        else if (xConv >= 61 && xConv <= 67 && yConv >= 16 && yConv <= 27)
        {
            button = 6;// exchange Move 01
        }
        else if (xConv >= 61 && xConv <= 67 && yConv >= 30 && yConv <= 41)
        {
            button = 7;// exchange Move 12
        }
        else if (xConv >= 61 && xConv <= 67 && yConv >= 44 && yConv <= 55)
        {
            button = 8;// exchange Move 23
        }
        else if (xConv >= 61 && xConv <= 67 && yConv >= 57 && yConv <= 69)
        {
            button = 9;// exchange Move 34
        }
        else if (xConv >= -55 && xConv <= 30 && yConv >= -60 && yConv <= 15)
        {
            button = 10;// Rotate Mouse control
        }
        else if (xConv >= 167 && xConv <= 176 && yConv <= -53 && yConv >= -60)
        {
            button = 11;// swap page
        }
        else if (xConv >= 167 && xConv <= 176 && yConv <= -41 && yConv >= -46)
        {
            button = 12;// swap page
        }
        else if (xConv >= 169 && xConv <= 175 && yConv <= 5 && yConv >= 0)
        {
            button = 13;// single dot button above bottom window.
        }
        else if (xConv >= -65 && xConv <= -51 && yConv <= -88 && yConv >= -101)
        {
            button = 14;// Inspect Pokedex
        }
        return button;
    }

    private EntityLiving getEntityToDisplay()
    {
        EntityLiving pokemob = entityToDisplayMap.get(pokedexEntry.getPokedexNb());

        if (pokemob == null)
        {
            pokemob = (EntityLiving) PokecubeMod.core.createPokemob(pokedexEntry, entityPlayer.getEntityWorld());
            if (pokemob != null)
            {
                CapabilityPokemob.getPokemobFor(pokemob).specificSpawnInit();
                entityToDisplayMap.put(pokedexEntry.getPokedexNb(), pokemob);
            }
        }

        return pokemob;
    }

    public void handleGuiButton(int button)
    {
        if ((page != 2 || mode) && button == 1)
        {
            pokedexEntry = Pokedex.getInstance().getNext(pokedexEntry, 1);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
        }
        else if ((page != 2 || mode) && button == 2)
        {
            pokedexEntry = Pokedex.getInstance().getPrevious(pokedexEntry, 1);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
        }
        else if (page == 0 && button == 3)
        {
            pokedexEntry = Pokedex.getInstance().getNext(pokedexEntry, 10);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
        }
        else if (page == 0 && button == 4)
        {
            pokedexEntry = Pokedex.getInstance().getPrevious(pokedexEntry, 10);
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
            PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
        }
        if (page == 0 && button >= 1 && button <= 5 || button == 5)
        {
            float volume = 0.2F;

            if (button == 5)
            {
                volume = 1F;
            }
            mc.player.playSound(pokedexEntry.getSoundEvent(), volume, 1.0F);

            nicknameTextField.setVisible(true);
            if (canEditPokemob())
            {
                nicknameTextField.setText(pokemob.getPokemonDisplayName().getFormattedText());
            }
            nicknameTextField.setEnabled(true);
        }
        else if (canEditPokemob() && page == 1)
        {
            if (button == 6)
            {
                pokemob.exchangeMoves(0, 1);
            }
            else if (button == 7)
            {
                pokemob.exchangeMoves(1, 2);
            }
            else if (button == 8)
            {
                pokemob.exchangeMoves(2, 3);
            }
            else if (button == 9)
            {
                pokemob.exchangeMoves(3, 4);
            }
            else if (button == 13)
            {
                pokemob.exchangeMoves(4, 5);
            }
        }
        if (button == 11)
        {
            page = (page + 1) % PAGECOUNT;

            if (entityPlayer.getHeldItemMainhand() != null
                    && entityPlayer.getHeldItemMainhand().getItem() == PokecubeItems.pokedex)
            {
                PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
                if (page == 2)
                {
                    nicknameTextField.setText("");
                    nicknameTextField.setEnabled(false);
                }
            }

        }
        if (button == 12)
        {
            page = (page - 1) % PAGECOUNT;
            if (page < 0) page = PAGECOUNT - 1;
            if (entityPlayer.getHeldItemMainhand() != null
                    && entityPlayer.getHeldItemMainhand().getItem() == PokecubeItems.pokedex)
            {
                PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
            }
        }
        if (button == 13)
        {
            mode = !mode;
            PacketPokedex packet = new PacketPokedex(PacketPokedex.REQUEST);
            packet.data.setBoolean("M", mode);
            packet.data.setString("F", pokedexEntry.getName());
            PokecubeMod.packetPipeline.sendToServer(packet);
            PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
        }

        if (page != 0)
        {

            if (page == 1)
            {

            }
            if (page != 4)
            {
                nicknameTextField.setText("");
                nicknameTextField.setEnabled(false);
                if (button == 3)
                {
                    index--;
                }
                if (button == 4)
                {
                    index++;
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        this.handleMouseMove(x, y, Mouse.getEventButton());
        super.handleMouseInput();
    }

    private void handleMouseMove(int x, int y, int mouseButton)
    {
        // System.out.println("handleMouseMove("+x+", "+y+",
        // "+mouseButton+")");
        if (mouseButton != -1)
        {
            mouseRotateControl = -1;
        }

        if (mouseRotateControl == 0)
        {
            prevX = x;
            xRenderAngle += y - prevY;
            prevY = y;

            if (xRenderAngle > 20)
            {
                xRenderAngle = 20;
            }

            if (xRenderAngle < -30)
            {
                xRenderAngle = -30;
            }
        }

        if (mouseRotateControl == 1)
        {
            yHeadRenderAngle += (prevX - x) * 2;
            prevX = x;
            xHeadRenderAngle += y - prevY;
            prevY = y;

            if (xHeadRenderAngle > 20)
            {
                xHeadRenderAngle = 20;
            }

            if (xHeadRenderAngle < -30)
            {
                xHeadRenderAngle = -30;
            }

            if (yHeadRenderAngle > 40)
            {
                yHeadRenderAngle = 40;
            }

            if (yHeadRenderAngle < -40)
            {
                yHeadRenderAngle = -40;
            }
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        buttonList.clear();
        int yOffset = height / 2 - 80;
        int xOffset = width / 2;
        nicknameTextField = new GuiTextField(0, fontRenderer, xOffset + 14, yOffset + 14, 110, 10);
        nicknameTextField.setMaxStringLength(20);
        nicknameTextField.setEnableBackgroundDrawing(false);
        nicknameTextField.setFocused(false);
        nicknameTextField.setEnabled(false);

        if (canEditPokemob() && page == 0)
        {
            String name = pokemob.getPokemonDisplayName().getUnformattedComponentText().trim();
            nicknameTextField.setText(name);
            nicknameTextField.setEnabled(true);
        }
        pokemobTextField = new GuiTextField(0, fontRenderer, xOffset - 65, yOffset + 123, 110, 10);
        pokemobTextField.setEnableBackgroundDrawing(false);
        pokemobTextField.setFocused(false);
        pokemobTextField.setEnabled(true);

        if (pokedexEntry != null)
        {
            pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
        }

        if (page == 2)
        {
            nicknameTextField.setText("");
            nicknameTextField.setEnabled(false);
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        if (isAltKeyDown()) return;
        boolean b = nicknameTextField.textboxKeyTyped(par1, par2);
        boolean b2 = pokemobTextField.textboxKeyTyped(par1, par2) || pokemobTextField.isFocused();
        if (b2)
        {
            if ((par2 == Keyboard.KEY_RETURN))
            {
                PokedexEntry entry = Database.getEntry(pokemobTextField.getText());
                if (entry == null)
                {
                    for (PokedexEntry e : Database.getSortedFormes())
                    {
                        String translated = I18n.format(e.getUnlocalizedName());
                        if (translated.equalsIgnoreCase(pokemobTextField.getText()))
                        {
                            Database.data2.put(translated, e);
                            entry = e;
                            break;
                        }
                    }
                    // If the pokedex entry is not actually registered, use old
                    // entry.
                    if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
                }
                if (entry != null)
                {
                    pokedexEntry = entry;
                    PacketPokedex.sendChangePagePacket((byte) page, mode, pokedexEntry);
                }
                else
                {
                    pokemobTextField.setText(I18n.format(pokedexEntry.getUnlocalizedName()));
                }
            }
        }
        else if (!nicknameTextField.isFocused() && par2 == Keyboard.KEY_LEFT)
        {
            handleGuiButton(2);
        }
        else if (!nicknameTextField.isFocused() && par2 == Keyboard.KEY_RIGHT)
        {
            handleGuiButton(1);
        }
        else if (par2 == Keyboard.KEY_UP)
        {
            handleGuiButton(3);
        }
        else if (par2 == Keyboard.KEY_DOWN)
        {
            handleGuiButton(4);
        }
        else if (par2 == Keyboard.KEY_PRIOR)
        {
            handleGuiButton(11);
        }
        else if (par2 == Keyboard.KEY_NEXT)
        {
            handleGuiButton(12);
        }
        else if (par2 != 54 && par2 != 58 && par2 != 42 && page == 0 && !b)
        {
            if (!(nicknameTextField.isFocused() && par2 == 28))
            {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
                return;
            }
            String nickname = nicknameTextField.getText();
            if (canEditPokemob() && page == 0)
            {
                pokemob.setPokemonNickname(nickname);
            }
            return;
        }
        if (!b2 && !nicknameTextField.isFocused()) super.keyTyped(par1, par2);
    }

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
        nicknameTextField.mouseClicked(x, y, mouseButton);
        pokemobTextField.mouseClicked(x, y, mouseButton);
        // System.out.println(mouseButton);
        int button = getButtonId(x, y);

        if (button != 0)
        {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (button == 14)
        {
            PacketPokedex.sendInspectPacket(true, FMLClientHandler.instance().getCurrentLanguage());
            return;
        }

        if (button == 10)
        {
            mouseRotateControl = mouseButton;
            prevX = x;
            prevY = y;
        }
        else
        {
            handleGuiButton(button);
        }

        if (page == 0)
        {
            if (!canEditPokemob())
            {
                nicknameTextField.setText("");
                nicknameTextField.setEnabled(false);
            }
            else
            {
                nicknameTextField.setEnabled(true);
                String name = pokemob.getPokemonDisplayName().getUnformattedComponentText().trim();
                nicknameTextField.setText(name);
            }
        }
    }

    public static void renderMob(EntityLiving entity, Minecraft mc, int dx, int dy, float scale, int height, int width,
            int xSize, int ySize, float xHeadRenderAngle, float yHeadRenderAngle, float yaw)
    {
        try
        {

            float size = 0;
            int j = 0;
            int k = 0;

            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob == null) { return; }
            PokedexEntry pokedexEntry = pokemob.getPokedexEntry();
            PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance()
                    .getPlayerData(Minecraft.getMinecraft().player).getData(PokecubePlayerStats.class);
            if ((StatsCollector.getCaptured(pokedexEntry, Minecraft.getMinecraft().player) > 0
                    || StatsCollector.getHatched(pokedexEntry, Minecraft.getMinecraft().player) > 0)
                    || mc.player.capabilities.isCreativeMode)
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);
            }
            else if (stats.hasInspected(pokedexEntry))
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(127, 127, 127, 255);
            }
            else
            {
                if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(15, 15, 15, 255);
            }

            pokemob.setPokemonAIState(IMoveConstants.EXITINGCUBE, false);

            float mobScale = pokemob.getSize();
            Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            j = (width - xSize) / 2 + dx;
            k = (height - ySize) / 2 + dy;

            GL11.glPushMatrix();
            GL11.glTranslatef(j + 60, k + 100, 50F);
            float zoom = (float) (25F / Math.sqrt(size)) * scale;
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.prevRenderYawOffset = yaw;
            entity.renderYawOffset = yaw;
            entity.rotationYaw = yaw;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = yHeadRenderAngle;
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.prevRotationPitch = entity.rotationPitch;
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);

            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.prevLimbSwingAmount = 0;
            PokeType flying = PokeType.getType("flying");
            entity.onGround = !pokemob.isType(flying);

            if (isAltKeyDown())
            {
                entity.onGround = true;
                entity.limbSwingAmount = 0.05f;
                entity.prevLimbSwingAmount = entity.limbSwingAmount - 0.5f;
            }
            int i = 15728880;
            int j2 = i % 65536;
            int k2 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j2 / 1.0F, k2 / 1.0F);

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, POKEDEX_RENDER, false);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);

            GL11.glPopMatrix();

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuiClosed()
    {
        PacketPokedex.sendInspectPacket(false, FMLClientHandler.instance().getCurrentLanguage());
    }
}

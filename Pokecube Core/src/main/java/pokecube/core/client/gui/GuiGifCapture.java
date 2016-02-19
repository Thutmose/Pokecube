/**
 *
 */
package pokecube.core.client.gui;

import static pokecube.core.utils.PokeType.flying;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.terrain.BiomeType;

public class GuiGifCapture extends GuiScreen
{
    protected IPokemob     pokemob      = null;
    protected EntityPlayer entityPlayer = null;

    public static PokedexEntry pokedexEntry = null;

    /** The X size of the inventory window in pixels. */
    protected int xSize = 127;

    /** The Y size of the inventory window in pixels. */
    protected int ySize            = 180;// old:166
    private float yRenderAngle     = 10;
    private float xRenderAngle     = 0;
    private float yHeadRenderAngle = 10;
    private float xHeadRenderAngle = 0;
    private int   mouseRotateControl;

    private int   page   = 0;
    List<Integer> biomes = new ArrayList<Integer>();

    /**
     *
     */
    public GuiGifCapture(IPokemob pokemob, EntityPlayer entityPlayer)
    {
        this.pokemob = pokemob;
        this.entityPlayer = entityPlayer;
        ItemStack item = entityPlayer.getHeldItem();
        if (item != null)
        {
            page = item.getItemDamage();
        }

        if (pokemob != null)
        {
            pokedexEntry = pokemob.getPokedexEntry();
        }
        else if (pokedexEntry == null)
        {
            pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }

        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
        {
            if (b != null)
            {
                biomes.add(b.biomeID);
            }
        }
        for (BiomeType b : BiomeType.values())
        {
            biomes.add(b.getType());
        }
    }

    @Override
    public void initGui()
    {
        x = this.width;
        y = this.height;

        buttonList.clear();

    }

    private boolean canEditPokemob()
    {
        return pokemob != null && pokedexEntry.getPokedexNb() == pokemob.getPokedexNb()
                && ((pokemob.getPokemonAIState(IPokemob.TAMED) && entityPlayer == pokemob.getPokemonOwner())
                        || entityPlayer.capabilities.isCreativeMode);
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        if (par2 != 54 && par2 != 58 && par2 != 42)
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }

    }

    private int getButtonId(int x, int y)
    {
        int xConv = x - ((width - xSize) / 2) - 74;
        int yConv = y - ((height - ySize) / 2) - 107;
        // System.out.println("("+xConv+","+yConv+")");
        int button = 0;

        if (xConv >= 35 && xConv <= 42 && yConv >= 52 && yConv <= 58)
        {
            button = 1;// Next
        }
        else if (xConv >= 20 && xConv <= 27 && yConv >= 52 && yConv <= 58)
        {
            button = 2;// Previous
        }
        else if (xConv >= 28 && xConv <= 34 && yConv >= 43 && yConv <= 51)
        {
            button = 3;// Next 10
        }
        else if (xConv >= 28 && xConv <= 34 && yConv >= 59 && yConv <= 65)
        {
            button = 4;// Previous 10
        }
        else if (xConv >= -67 && xConv <= -57 && yConv >= 56 && yConv <= 65)
        {
            button = 5;// Sound
        }
        else if (xConv >= 56 && xConv <= 60 && yConv >= 7 && yConv <= 16)
        {
            button = 6;// exchange Move 01
        }
        else if (xConv >= 56 && xConv <= 60 && yConv >= 21 && yConv <= 30)
        {
            button = 7;// exchange Move 12
        }
        else if (xConv >= 56 && xConv <= 60 && yConv >= 35 && yConv <= 45)
        {
            button = 8;// exchange Move 23
        }
        else if (xConv >= 56 && xConv <= 60 && yConv >= 49 && yConv <= 59)
        {
            button = 9;// exchange Move 34
        }
        else if ((xConv >= -67 && xConv <= 41 && yConv >= -39 && yConv <= 26)
                || (xConv >= -67 && xConv <= 15 && yConv >= 26 && yConv <= 38)
                || (xConv >= -53 && xConv <= 15 && yConv >= 38 && yConv <= 52))
        {
            button = 10;// Rotate Mouse control
        }
        else if (xConv >= 167 && xConv <= 172 && yConv <= -40 && yConv >= -52)
        {
            button = 11;// swap page
        }
        else if (xConv >= 167 && xConv <= 172 && yConv <= -22 && yConv >= -34)
        {
            button = 12;// swap page
        }

        return button;
    }

    int prevX = 0;
    int prevY = 0;

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
        // System.out.println("handleMouseMove("+x+", "+y+", "+mouseButton+")");
        if (mouseButton != -1)
        {
            mouseRotateControl = -1;
        }

        if (mouseRotateControl == 0)
        {
            yRenderAngle += (x - prevX) * 2;
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

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
        int button = getButtonId(x, y);

        if (page == 0 && button == 3)
        {
            pokedexEntry = Pokedex.getInstance().getNext(pokedexEntry, 10);
        }
        else if (page == 0 && button == 4)
        {
            pokedexEntry = Pokedex.getInstance().getPrevious(pokedexEntry, 10);
        }
        else if (button == 10)
        {
            mouseRotateControl = mouseButton;
            prevX = x;
            prevY = y;
        }

        if (page == 0 && button >= 1 && button <= 5 || button == 5)
        {
            float volume = 0.2F;

            if (button == 5)
            {
                volume = 1F;
            }

            // mod_Pokecube.getWorld().playSoundAtEntity(entityPlayer,
            // pokedexEntry.getSound(), volume, 1.0F);
            mc.theWorld.playSoundAtEntity(mc.thePlayer, pokedexEntry.getSound(), volume, 1.0F);// .playSoundFX(pokedexEntry.getSound(),
                                                                                               // volume,
                                                                                               // 1.0F);
            mc.thePlayer.playSound(pokedexEntry.getSound(), volume, 1.0F);
        }
        else if (canEditPokemob())
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
        }
    }

    private static final ResourceLocation GUIIMG = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/" + "wikiCapture.png");

    @Override
    public void drawScreen(int i, int j, float f)
    {
        Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        int j3 = 0xF0F0F0;// 61680;
        int k = j3 % 0x000100;
        int l = j3 / 0xFFFFFF;
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, k / 1.0F, l / 1.0F);
        minecraft.renderEngine.bindTexture(GUIIMG);
        int j2 = (width - xSize) / 2;
        int k2 = (height - ySize) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, xSize, ySize);
        GL11.glPushMatrix();
        // GL11.glScalef(2.0F, 2.0F, 2.0F);

        renderMob();

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        super.drawScreen(i, j, f);
    }

    @Override
    public void drawBackground(int n)
    {
        super.drawBackground(n);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private static HashMap<Integer, EntityLiving> entityToDisplayMap = new HashMap<Integer, EntityLiving>();

    private EntityLiving getEntityToDisplay()
    {
        EntityLiving pokemob = entityToDisplayMap.get(pokedexEntry.getPokedexNb());

        if (pokemob == null)
        {
            // int entityId =
            // mod_Pokecube.getEntityIdFromPokedexNumber(pokedexEntry.getPokedexNb());
            pokemob = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(pokedexEntry.getPokedexNb(),
                    entityPlayer.worldObj);

            if (pokemob != null)
            {
                entityToDisplayMap.put(pokedexEntry.getPokedexNb(), pokemob);
            }
        }

        return pokemob;
    }

    public static int x;
    public static int y;

    private void renderMob()
    {
        try
        {
            EntityLiving entity = getEntityToDisplay();

            float size = 0;
            int j = 0;
            int k = 0;

            if (entity instanceof IPokemob)
            {
            }

            if (entity instanceof EntityPokemob)
            {
                ((EntityPokemob) entity).setRGBA(255, 255, 255, 255);
                ((EntityPokemob) entity).scale = 1;
                ((EntityPokemob) entity).shiny = false;
            }
            size = Math.max(entity.width, entity.height);
            j = (width - xSize) / 2;
            k = (height - ySize) / 2;

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(j + 55, k + 120, 50F);
            float zoom = 23f / size;// (float)(23F / Math.sqrt(size + 0.6));
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = 0F;
            entity.rotationYaw = yHeadRenderAngle;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = entity.rotationYaw;
            // GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
            float offset = 0.4f;
            float f, f1, f2;
            f = entityPlayer.rotationPitch;
            f1 = entityPlayer.rotationYaw;
            f2 = entityPlayer.rotationYawHead;

            entityPlayer.rotationPitch = 00;
            entityPlayer.renderYawOffset = 50;
            entityPlayer.rotationYawHead = 40;
            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entityPlayer, offset + entity.width,
                    1.6D, 0.0D, 1.0F, 1.0F);

            entityPlayer.rotationPitch = f;
            entityPlayer.renderYawOffset = f1;
            entityPlayer.rotationYawHead = f2;

            yRenderAngle = -30;
            xRenderAngle = 0;

            GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);
            ((EntityPokemob) entity).setPokemonAIState(IPokemob.SITTING, false);
            entity.setPosition(entityPlayer.posX, entityPlayer.posY + 1, entityPlayer.posZ);
            // System.err.println(""+triangle);
            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.onGround = ((EntityPokemob) entity).getType1() != flying
                    && ((EntityPokemob) entity).getType2() != flying;
            int i = 15728880;
            int j1 = i % 65536;
            int k1 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0, 0, 0, 0, POKEDEX_RENDER);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            float time = MathHelper.cos(((EntityPokemob) entity).worldObj.getWorldTime() / 3f * 0.6662f);

            if (time != lastTime)
            {
                // WikiWriter.doCapturePokemobGif();//TODO re-instate this
            }
            lastTime = time;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    static float              lastTime       = 0;
    /** to pass as last parameter when rendering the mob so that the render
     * knows the rendering is asked by the pokedex gui */
    public final static float POKEDEX_RENDER = 1.5f;
}

package pokecube.core.client.gui.blocks;

import static pokecube.core.utils.PokeType.flying;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.core.blocks.tradingTable.ContainerTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PCPacketHandler;
import pokecube.core.network.PCPacketHandler.MessageServer;
import pokecube.core.network.PokecubePacketHandler;

public class GuiTradingTable extends GuiContainer
{
    public final static float    POKEDEX_RENDER   = 1.5f;
    private float                yRenderAngle     = 10;

    private float                xRenderAngle     = 0;
    private float                yHeadRenderAngle = 10;

    private float                xHeadRenderAngle = 0;

    protected EntityPlayer       entityPlayer     = null;

    final TileEntityTradingTable table;

    public GuiTradingTable(InventoryPlayer player_inventory, TileEntityTradingTable table)
    {
        super(new ContainerTradingTable(table, player_inventory));
        entityPlayer = player_inventory.player;
        this.table = table;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 1)
        {
            MessageServer packet = PCPacketHandler.makeServerPacket(MessageServer.TRADE, "0".getBytes());
            PokecubePacketHandler.sendToServer(packet);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/tradingTableGui.png"));
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
    {
        ItemStack stack = table.getStackInSlot(0);

        if (PokecubeManager.isFilled(stack))
        {
            renderMob(0);
        }
        stack = table.getStackInSlot(1);
        if (PokecubeManager.isFilled(stack))
        {
            renderMob(1);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);

        int x = width / 2 - 73;
        int y = height / 2 - 71;

        boolean red = false, green = false, blue = false;

        if (table.player1 != null)
        {
            green = true;
            if (table.player1 == entityPlayer) blue = true;
        }

        {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(red ? 1 : 0, green ? 1 : 0, blue ? 1 : 0, 1);
            mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/hologram.png"));
            drawTexturedModalRect(x, y, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
        red = green = blue = false;
        if (table.player2 != null)
        {
            green = true;
            if (table.player2 == entityPlayer) blue = true;
        }
        x += 125;
        {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(red ? 1 : 0, green ? 1 : 0, blue ? 1 : 0, 1);
            mc.renderEngine.bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/hologram.png"));
            drawTexturedModalRect(x, y, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

    }

    private EntityLiving getEntityToDisplay(int index)
    {
        EntityLiving pokemob = (EntityLiving) PokecubeManager.itemToPokemob(table.getStackInSlot(index),
                table.getWorld());
        return pokemob;
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        String trade = I18n.format("tile.tradingtable.trade");
        buttonList.add(new GuiButton(1, width / 2 - 30, height / 2 - 40, 60, 20, trade));
        super.initGui();
    }

    /** Called when the screen is unloaded. Used to disable keyboard repeat
     * events */
    @Override
    public void onGuiClosed()
    {
        table.player1 = null;
        table.player2 = null;
        MessageServer packet = PCPacketHandler.makeServerPacket(MessageServer.TRADE,
                ("5," + table.getPos().getX() + "," + table.getPos().getY() + "," + table.getPos().getZ()).getBytes());
        PokecubePacketHandler.sendToServer(packet);
    }

    private void renderMob(int index)
    {
        EntityLiving entity = getEntityToDisplay(index);

        if (entity == null) return;

        float size = 0;
        int j = index == 0 ? 20 : 150;
        int k = -30;

        IPokemob pokemob = null;
        if (entity instanceof IPokemob)
        {
            pokemob = (IPokemob) entity;
        }

        // if (entity instanceof EntityPokemob)
        // {
        // ((EntityPokemob) entity).setSizes();//TODO see if needed
        // }
        size = Math.max(entity.width, entity.height);
        pokemob.setPokemonAIState(IMoveConstants.EXITINGCUBE, false);
        // }
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(j + 0, k + 90, 50F);

        float zoom = 23F / size;
        // System.out.println(zoom);
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f5 = (float) ((k + 75) - 50) - ySize;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = 0F;
        entity.rotationYaw = yHeadRenderAngle;
        entity.rotationPitch = xHeadRenderAngle;
        entity.rotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
        yRenderAngle = yRenderAngle + 0.15F;
        GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);

        // LoggerPokecube.logError(""+triangle);
        entity.limbSwing = 0;
        entity.limbSwingAmount = 0;
        entity.onGround = ((IPokemob) entity).getType1() != flying && ((IPokemob) entity).getType2() != flying;

        Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, POKEDEX_RENDER, false);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

    }

}

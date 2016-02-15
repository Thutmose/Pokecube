package pokecube.core.events.handlers;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.megastuff.ItemMegaring;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

@SideOnly(Side.CLIENT)
public class EventsHandlerClient
{
    /** Rain X coords */
    float[] rainXCoords;
    /** Rain Y coords */
    float[] rainYCoords;

    public EventsHandlerClient()
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void ClientTick(ClientTickEvent evt)
    {
        if (evt.phase == Phase.END)
        {
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void FogRenderTick(EntityViewRenderEvent.FogDensity evt)
    {
        if (evt.entity instanceof EntityPlayer && evt.entity.ridingEntity != null
                && evt.entity.ridingEntity instanceof IPokemob)
        {
            IPokemob mount = (IPokemob) evt.entity.ridingEntity;
            if (evt.entity.isInWater() && mount.canUseDive())
            {
                evt.density = 0.05f;
                evt.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void ClientRenderTick(RenderWorldLastEvent evt)
    {
        // TODO fix the terrain effects, I don't think this event is good for it
        // anymore
    }

    static long eventTime = 0;
    static long counter   = 0;

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        int key = Keyboard.getEventKey();

        if (Keyboard.getEventNanoseconds() == eventTime) return;

        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        
        eventTime = Keyboard.getEventNanoseconds();
        if (key == Keyboard.KEY_SPACE && player.ridingEntity instanceof IPokemob)
        {
            MessageServer packet = new MessageServer(MessageServer.JUMP, player.ridingEntity.getEntityId());
            PokecubePacketHandler.sendToServer(packet);
        }
        if (key == ClientProxyPokecube.mobMegavolve.getKeyCode())
        {
            InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
            boolean ring = false;
            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                {
                    Item item = stack.getItem();
                    if (item instanceof ItemMegaring)
                    {
                        ring = true;
                        break;
                    }
                }
            }
            IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && ring && !current.getPokemonAIState(IMoveConstants.EVOLVING))
            {
                current.setPokemonAIState(IMoveConstants.EVOLVING, true);
                MessageServer message = new MessageServer(MessageServer.CHANGEFORM, current.getPokemonUID());
                PokecubePacketHandler.sendToServer(message);
            }
        }

        if (GameSettings.isKeyDown(ClientProxyPokecube.nextMob))
        {
            if (GuiScreen.isAltKeyDown())
            {
                int num = GuiScreen.isShiftKeyDown() ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(num, 0);
            }
            else
            {
                GuiDisplayPokecubeInfo.instance().nextPokemob();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.previousMob))
        {
            if (GuiScreen.isAltKeyDown())
            {
                int num = GuiScreen.isShiftKeyDown() ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(-num, 0);
            }
            else
            {
                GuiDisplayPokecubeInfo.instance().previousPokemob();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.nextMove))
        {
            if (GuiScreen.isAltKeyDown())
            {
                int num = GuiScreen.isShiftKeyDown() ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(0, num);
            }
            else
            {
                if (GuiTeleport.instance().getState()) GuiTeleport.instance().nextMove();
                else GuiDisplayPokecubeInfo.instance().nextMove();
            }

        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.previousMove))
        {
            if (GuiScreen.isAltKeyDown())
            {
                int num = GuiScreen.isShiftKeyDown() ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(0, -num);
            }
            else
            {
                if (GuiTeleport.instance().getState()) GuiTeleport.instance().previousMove();
                else GuiDisplayPokecubeInfo.instance().previousMove();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobBack))
        {

            if (GuiTeleport.instance().getState())
            {
                GuiTeleport.instance().setState(false);
            }
            else
            {
                GuiDisplayPokecubeInfo.instance().pokemobBack();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobAttack))
        {
            GuiDisplayPokecubeInfo.instance().pokemobAttack();
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobStance))
        {
            GuiDisplayPokecubeInfo.instance().pokemobStance();
        }

        if (GameSettings.isKeyDown(ClientProxyPokecube.mobMove1))
        {
            GuiDisplayPokecubeInfo.instance().setMove(0);
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobMove2))
        {
            GuiDisplayPokecubeInfo.instance().setMove(1);
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobMove3))
        {
            GuiDisplayPokecubeInfo.instance().setMove(2);
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobMove4))
        {
            GuiDisplayPokecubeInfo.instance().setMove(3);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGUIScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        try
        {
            if ((event.gui instanceof GuiContainer))
            {
                GuiContainer gui = (GuiContainer) event.gui;
                if (gui.mc.thePlayer == null || !GuiScreen.isAltKeyDown()) { return; }

                if (rainXCoords == null)
                {
                    rainXCoords = new float[] { 0 };
                }

                if (rainXCoords[0] == event.renderPartialTicks) return;
                rainXCoords[0] = event.renderPartialTicks;
                List<Slot> slots = gui.inventorySlots.inventorySlots;
                int w = gui.width;
                int h = gui.height;
                int xSize = gui.xSize;
                int ySize = gui.ySize;
                float zLevel = 800;
                GL11.glPushMatrix();
                GlStateManager.translate(0, 0, zLevel);
                for (Slot slot : slots)
                {
                    if (slot.getHasStack() && PokecubeManager.isFilled(slot.getStack()))
                    {
                        IPokemob pokemob = getPokemobForRender(slot.getStack(), gui.mc.theWorld);
                        if(pokemob==null) continue;
                        int x = (w - xSize) / 2;
                        int y = (h - ySize) / 2;
                        int i, j;
                        i = slot.xDisplayPosition + 8;
                        j = slot.yDisplayPosition + 10;
                        GL11.glPushMatrix();
                        GL11.glTranslatef(i + x, j + y, 0F);
                        EntityLiving entity = (EntityLiving) pokemob;
                        entity.rotationYaw = 0;
                        entity.rotationPitch = 0;
                        entity.rotationYawHead = 0;
                        pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
                        entity.onGround = true;
                        renderMob(pokemob, event.renderPartialTicks);
                        GL11.glPopMatrix();
                    }
                }
                GL11.glPopMatrix();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderHotbar(RenderGameOverlayEvent.Post event)
    {
        if (event.type == ElementType.HOTBAR)
        {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player == null || !GuiScreen.isAltKeyDown()
                    || Minecraft.getMinecraft().currentScreen != null) { return; }

            int w = event.resolution.getScaledWidth();
            int h = event.resolution.getScaledHeight();

            int i, j;
            i = -80;
            j = -9;

            int xSize = 0;
            int ySize = 0;
            float zLevel = 800;
            GL11.glPushMatrix();
            GlStateManager.translate(0, 0, zLevel);

            for (int l = 0; l < 9; l++)
            {
                ItemStack stack = player.inventory.mainInventory[l];
                if (stack != null && PokecubeManager.isFilled(stack))
                {
                    IPokemob pokemob = getPokemobForRender(stack, player.worldObj);
                    if (pokemob == null)
                    {
                        continue;
                    }
                    int x = (w - xSize) / 2;
                    int y = (h - ySize);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(i + x + 20 * l, j + y, 0F);
                    EntityLiving entity = (EntityLiving) pokemob;
                    entity.rotationYaw = 0;
                    entity.rotationPitch = 0;
                    entity.rotationYawHead = 0;
                    pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
                    entity.onGround = true;
                    renderMob(pokemob, event.partialTicks);
                    GL11.glPopMatrix();
                }
            }
            GL11.glPopMatrix();
        }

        debug = event.type == ElementType.DEBUG;

    }

    boolean debug = false;

    @SubscribeEvent
    public void textOverlay(RenderGameOverlayEvent.Text event)
    {
        if (!debug) return;
        TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getMinecraft().thePlayer);
        Vector3 v = Vector3.getNewVectorFromPool().set(Minecraft.getMinecraft().thePlayer);
        String msg = "Sub-Biome: " + BiomeDatabase.getReadableNameFromType(t.getBiome(v));
        v.freeVectorFromPool();
        // Until forge stops sending the same event, with the same list 8 times,
        // this is needed
        for (String s : event.left)
        {
            if (s != null && s.equals(msg)) return;
        }
        debug = false;
        event.left.add("");
        event.left.add(msg);
    }

    public static HashMap<PokedexEntry, IPokemob> renderMobs = new HashMap<PokedexEntry, IPokemob>();

    public static IPokemob getPokemobForRender(ItemStack itemStack, World world)
    {
        if (!itemStack.hasTagCompound()) return null;

        int num = PokecubeManager.getPokedexNb(itemStack);
        if (num != 0)
        {
            PokedexEntry entry = Database.getEntry(num);
            IPokemob pokemob = renderMobs.get(entry);
            if (pokemob == null)
            {
                pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(num, world);
                if (pokemob == null) return null;
                renderMobs.put(entry, pokemob);
            }
            NBTTagCompound pokeTag = itemStack.getTagCompound().getCompoundTag("Pokemob");
            EventsHandler.setFromNBT(pokemob, pokeTag);
            pokemob.popFromPokecube();
            pokemob.setPokecubeId(PokecubeItems.getCubeId(itemStack));
            ((EntityLivingBase) pokemob).setHealth(
                    Tools.getHealth((int) ((EntityLivingBase) pokemob).getMaxHealth(), itemStack.getItemDamage()));
            pokemob.setStatus(PokecubeManager.getStatus(itemStack));
            ((EntityLivingBase) pokemob).extinguish();
            return pokemob;
        }

        return null;
    }

    public static void renderMob(IPokemob pokemob, float tick)
    {
        renderMob(pokemob, tick, true);
    }

    public static void renderMob(IPokemob pokemob, float tick, boolean rotates)
    {
        if (pokemob == null) return;

        EntityLiving entity = (EntityLiving) pokemob;

        float size = 0;

        float mobScale = pokemob.getSize();
        size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                Math.max(pokemob.getPokedexEntry().height * mobScale, pokemob.getPokedexEntry().length * mobScale));

        GL11.glPushMatrix();
        float zoom = (float) (10f / Math.sqrt(size));
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        Minecraft.getMinecraft();
        long time = Minecraft.getSystemTime();
        if (rotates) GL11.glRotatef((time + tick) / 20f, 0, 1, 0);
        RenderHelper.enableStandardItemLighting();

        GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);

        int i = 15728880;
        int j1 = i % 65536;
        int k1 = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0, -0.123456, 0, 0, 1.5F);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();

    }
}

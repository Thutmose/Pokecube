package pokecube.core.events.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerEntityOnShoulder;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiArranger;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.RenderHealth;
import pokecube.core.client.render.entity.RenderPokemobOnShoulder;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketMountedControl;
import pokecube.core.utils.Tools;
import thut.core.client.ClientProxy;

@SideOnly(Side.CLIENT)
public class EventsHandlerClient
{
    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        private ITextComponent getInfoMessage(CheckResult result, String name)
        {
            String linkName = "[" + TextFormatting.GREEN + name + " " + PokecubeMod.VERSION + TextFormatting.WHITE;
            String link = "" + result.url;
            String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + link + "\"}}";
            String info = "\"" + TextFormatting.GOLD + "Currently Running " + "\"";
            String mess = "[" + info + "," + linkComponent + ",\"]\"]";
            return ITextComponent.Serializer.jsonToComponent(mess);
        }

        private ITextComponent getIssuesMessage(CheckResult result)
        {
            String linkName = "[" + TextFormatting.GREEN + "Clicking Here." + TextFormatting.WHITE;
            String link = "https://github.com/Thutmose/Pokecube/issues";
            String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + link + "\"}}";
            String info = "\"" + TextFormatting.GOLD
                    + "If you find any bugs, please report them at the Github Issue tracker, you can find that by "
                    + "\"";
            String mess = "[" + info + "," + linkComponent + ",\"]\"]";
            return ITextComponent.Serializer.jsonToComponent(mess);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.getEntityWorld().isRemote
                    && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeMod.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    ITextComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Core");
                    (event.player).sendMessage(mess);
                }
                else if (PokecubeMod.core.getConfig().loginmessage)
                {
                    ITextComponent mess = getInfoMessage(result, "Pokecube Core");
                    (event.player).sendMessage(mess);
                    mess = getIssuesMessage(result);
                    (event.player).sendMessage(mess);
                }
            }
        }
    }

    public static HashMap<PokedexEntry, IPokemob>        renderMobs = new HashMap<PokedexEntry, IPokemob>();
    private static Map<PokedexEntry, ResourceLocation[]> icons      = Maps.newHashMap();
    static boolean                                       notifier   = false;

    public static IPokemob getPokemobForRender(ItemStack itemStack, World world)
    {
        if (!itemStack.hasTagCompound()) return null;

        int num = PokecubeManager.getPokedexNb(itemStack);
        if (num != 0)
        {
            PokedexEntry entry = Database.getEntry(num);
            IPokemob pokemob = getRenderMob(entry, world);
            if (pokemob == null) return null;
            NBTTagCompound pokeTag = itemStack.getTagCompound();
            EventsHandler.setFromNBT(pokemob, pokeTag);
            pokemob.setPokecube(itemStack);
            pokemob.getEntity()
                    .setHealth(Tools.getHealth((int) pokemob.getEntity().getMaxHealth(), itemStack.getItemDamage()));
            pokemob.setStatus(PokecubeManager.getStatus(itemStack));
            pokemob.getEntity().extinguish();
            return pokemob;
        }

        return null;
    }

    public static IPokemob getRenderMob(PokedexEntry entry, World world)
    {
        IPokemob pokemob = renderMobs.get(entry);
        if (pokemob != null) pokemob = pokemob.setPokedexEntry(entry);
        if (pokemob == null || pokemob != renderMobs.get(entry))
        {
            pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, world));
            if (pokemob == null) return null;
            pokemob.specificSpawnInit();
            renderMobs.put(entry, pokemob);
        }
        return pokemob;
    }

    public static void renderMob(IPokemob pokemob, float tick)
    {
        renderMob(pokemob, tick, true);
    }

    public static void renderMob(IPokemob pokemob, float tick, boolean rotates)
    {
        if (pokemob == null) return;
        EntityLiving entity = pokemob.getEntity();
        float size = 0;
        float mobScale = pokemob.getSize();
        Vector3f dims = pokemob.getPokedexEntry().getModelSize();
        size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
        GL11.glPushMatrix();
        float zoom = (float) (12f / size);
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        long time = Minecraft.getSystemTime();
        if (rotates) GL11.glRotatef((time + tick) / 20f, 0, 1, 0);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, 0, 0, 0, 1.5F, false);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glPopMatrix();

    }

    private Set<RenderPlayer> addedLayers = Sets.newHashSet();
    long                      lastSetTime = 0;

    public EventsHandlerClient()
    {
        if (!notifier)
        {
            new UpdateNotifier();
            MinecraftForge.EVENT_BUS.register(new RenderHealth());
            MinecraftForge.EVENT_BUS.register(new GuiArranger());
            MinecraftForge.EVENT_BUS.register(this);
        }
        notifier = true;

    }

    @SubscribeEvent
    public void clientTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getMinecraft().player) return;
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob != null && PokecubeMod.core.getConfig().autoSelectMoves)
        {
            Entity target = pokemob.getEntity().getAttackTarget();
            if (target != null && !pokemob.getGeneralState(GeneralStates.MATING))
            {
                setMostDamagingMove(pokemob, target);
            }
        }
        if (PokecubeMod.core.getConfig().autoRecallPokemobs)
        {
            IPokemob mob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (mob != null && !(mob.getEntity().isDead) && mob.getEntity().addedToChunk && event.player
                    .getDistance(mob.getEntity()) > PokecubeMod.core.getConfig().autoRecallDistance)
            {
                mob.returnToPokecube();
            }
        }
        control:
        if (event.player.isRiding() && Minecraft.getMinecraft().currentScreen == null)
        {
            Entity e = event.player.getRidingEntity();
            pokemob = CapabilityPokemob.getPokemobFor(e);
            if (pokemob != null)
            {
                LogicMountedControl controller = pokemob.getController();
                if (controller == null) break control;
                controller.backInputDown = ((EntityPlayerSP) event.player).movementInput.backKeyDown;
                controller.forwardInputDown = ((EntityPlayerSP) event.player).movementInput.forwardKeyDown;
                controller.leftInputDown = ((EntityPlayerSP) event.player).movementInput.leftKeyDown;
                controller.rightInputDown = ((EntityPlayerSP) event.player).movementInput.rightKeyDown;

                boolean up = false;
                if (ClientProxyPokecube.mobUp.getKeyCode() == Keyboard.KEY_NONE)
                {
                    up = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
                }
                else
                {
                    up = GameSettings.isKeyDown(ClientProxyPokecube.mobUp);
                }
                boolean down = false;
                if (ClientProxyPokecube.mobDown.getKeyCode() == Keyboard.KEY_NONE)
                {
                    down = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
                }
                else
                {
                    down = GameSettings.isKeyDown(ClientProxyPokecube.mobDown);
                }
                controller.upInputDown = up;
                controller.downInputDown = down;
                controller.followOwnerLook = PokecubeMod.core.getConfig().riddenMobsTurnWithLook;

                if (GameSettings.isKeyDown(ClientProxyPokecube.throttleDown))
                {
                    controller.throttle -= 0.05;
                    controller.throttle = Math.max(controller.throttle, 0.01);
                }
                else if (GameSettings.isKeyDown(ClientProxyPokecube.throttleUp))
                {
                    controller.throttle += 0.05;
                    controller.throttle = Math.min(controller.throttle, 1);
                }
                PacketMountedControl.sendControlPacket(e, controller);
            }
        }
        lastSetTime = System.currentTimeMillis() + 500;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void FogRenderTick(EntityViewRenderEvent.FogDensity evt)
    {
        IPokemob mount;
        if (evt.getEntity() instanceof EntityPlayer && evt.getEntity().getRidingEntity() != null
                && (mount = CapabilityPokemob.getPokemobFor(evt.getEntity().getRidingEntity())) != null)
        {
            if (evt.getEntity().isInWater() && mount.canUseDive())
            {
                evt.setDensity(0.05f);
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        if (ClientProxyPokecube.mobMegavolve.isPressed())
        {
            IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && !current.getGeneralState(GeneralStates.EVOLVING))
            {
                PacketChangeForme.sendPacketToServer(current.getEntity(), null);
            }
        }
        if (ClientProxyPokecube.arrangeGui.isPressed())
        {
            GuiArranger.toggle = !GuiArranger.toggle;
        }
        if (ClientProxyPokecube.noEvolve.isPressed() && GuiDisplayPokecubeInfo.instance().getCurrentPokemob() != null)
        {
            GuiDisplayPokecubeInfo.instance().getCurrentPokemob().cancelEvolve();
        }
        if (ClientProxyPokecube.nextMob.isPressed())
        {
            GuiDisplayPokecubeInfo.instance().nextPokemob();
        }
        if (ClientProxyPokecube.previousMob.isPressed())
        {
            GuiDisplayPokecubeInfo.instance().previousPokemob();
        }
        if (ClientProxyPokecube.nextMove.isPressed())
        {
            int num = GuiScreen.isCtrlKeyDown() ? 2 : 1;
            if (GuiScreen.isShiftKeyDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().nextMove();
            else GuiDisplayPokecubeInfo.instance().nextMove(num);
        }
        if (ClientProxyPokecube.previousMove.isPressed())
        {
            int num = GuiScreen.isCtrlKeyDown() ? 2 : 1;
            if (GuiScreen.isShiftKeyDown()) num++;
            if (GuiTeleport.instance().getState()) GuiTeleport.instance().previousMove();
            else GuiDisplayPokecubeInfo.instance().previousMove(num);
        }
        if (ClientProxyPokecube.mobBack.isPressed())
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
        if (ClientProxyPokecube.mobAttack.isPressed())
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

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event)
    {
        if (addedLayers.contains(event.getRenderer())) { return; }
        List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "layerRenderers", "field_177097_h", "i");
        for (int i = 0; i < layerRenderers.size(); i++)
        {
            LayerRenderer<?> layer = layerRenderers.get(i);
            if (layer instanceof LayerEntityOnShoulder)
            {
                layerRenderers.add(i, new RenderPokemobOnShoulder(event.getRenderer().getRenderManager(),
                        (LayerEntityOnShoulder) layer));
                layerRenderers.remove(layer);
                break;
            }
        }
        addedLayers.add(event.getRenderer());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGUIScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event)
    {
        try
        {
            if ((event.getGui() instanceof GuiContainer))
            {
                GuiContainer gui = (GuiContainer) event.getGui();
                if (gui.mc.player == null || !GuiScreen.isAltKeyDown()) { return; }

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
                        IPokemob pokemob = getPokemobForRender(slot.getStack(), gui.mc.world);
                        if (pokemob == null) continue;
                        int x = (w - xSize) / 2;
                        int y = (h - ySize) / 2;
                        int i, j;
                        i = slot.xPos + 8;
                        j = slot.yPos + 10;
                        GL11.glPushMatrix();
                        GL11.glTranslatef(i + x, j + y, 0F);
                        renderIcon(pokemob, -8, -12, 16, 16);
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
        if (event.getType() == ElementType.HOTBAR)
        {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null || !GuiScreen.isAltKeyDown()
                    || Minecraft.getMinecraft().currentScreen != null) { return; }

            int w = event.getResolution().getScaledWidth();
            int h = event.getResolution().getScaledHeight();

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
                ItemStack stack = player.inventory.mainInventory.get(l);
                if (stack != null && PokecubeManager.isFilled(stack))
                {
                    IPokemob pokemob = getPokemobForRender(stack, player.getEntityWorld());
                    if (pokemob == null)
                    {
                        continue;
                    }
                    int x = (w - xSize) / 2;
                    int y = (h - ySize);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(i + x + 20 * l, j + y, 0F);
                    renderIcon(pokemob, -8, -12, 16, 16);
                    GL11.glPopMatrix();
                }
            }
            GL11.glPopMatrix();
        }
    }

    private void setMostDamagingMove(IPokemob outMob, Entity target)
    {
        int index = outMob.getMoveIndex();
        int max = 0;
        String[] moves = outMob.getMoves();
        for (int i = 0; i < 4; i++)
        {
            String s = moves[i];
            if (s != null)
            {
                int temp = Tools.getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        if (index != outMob.getMoveIndex())
        {
            GuiDisplayPokecubeInfo.instance().setMove(index);
        }
    }

    public static void renderIcon(PokedexEntry entry, int left, int top, int width, int height, boolean shiny)
    {
        ResourceLocation[] texs = icons.get(entry);
        ResourceLocation tex = null;
        if (texs != null) tex = texs[shiny ? 1 : 0];
        if (tex == null)
        {
            texs = new ResourceLocation[2];
            icons.put(entry, texs);
            String texture = entry.getModId() + ":" + entry.getTexture((byte) 0).replace("/entity/", "/entity_icon/");
            String textureS = entry.hasShiny ? texture.replace(".png", "s.png") : texture;
            tex = new ResourceLocation(texture);
            texs[0] = tex;
            try
            {
                Minecraft.getMinecraft().getResourceManager().getResource(tex).getInputStream().close();
                try
                {
                    ResourceLocation tex2 = new ResourceLocation(textureS);
                    Minecraft.getMinecraft().getResourceManager().getResource(tex2).getInputStream().close();
                    texs[1] = tex2;
                }
                catch (IOException e)
                {
                    texs[1] = tex;
                }
            }
            catch (IOException e)
            {
                PokecubeMod.log(Level.WARNING, "no Icon for " + entry, e);
            }
        }
        int colour = 0xFFFFFFFF;

        int right = left + width;
        int bottom = top + height;

        if (left < right)
        {
            int i1 = left;
            left = right;
            right = i1;
        }

        if (top < bottom)
        {
            int j1 = top;
            top = bottom;
            bottom = j1;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
        float f3 = (float) (colour >> 24 & 255) / 255.0F;
        float f = (float) (colour >> 16 & 255) / 255.0F;
        float f1 = (float) (colour >> 8 & 255) / 255.0F;
        float f2 = (float) (colour & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double) left, (double) bottom, 0.0D).tex(0, 0).endVertex();
        bufferbuilder.pos((double) right, (double) bottom, 0.0D).tex(1, 0).endVertex();
        bufferbuilder.pos((double) right, (double) top, 0.0D).tex(1, 1).endVertex();
        bufferbuilder.pos((double) left, (double) top, 0.0D).tex(0, 1).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
    }

    public static void renderIcon(IPokemob realMob, int left, int top, int width, int height)
    {
        PokedexEntry entry = realMob.getPokedexEntry();
        renderIcon(entry, left, top, width, height, realMob.isShiny());
    }

}

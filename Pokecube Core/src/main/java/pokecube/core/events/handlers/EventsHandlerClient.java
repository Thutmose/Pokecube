package pokecube.core.events.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.entity.RenderHeldPokemobs;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob.MountState;
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
import thut.core.client.ClientProxy;

@SideOnly(Side.CLIENT)
public class EventsHandlerClient
{
    public static interface RingChecker
    {
        boolean hasRing(EntityPlayer player);
    }

    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        private IChatComponent getInfoMessage(CheckResult result, String name)
        {
            String linkName = "[" + EnumChatFormatting.GREEN + name + " " + PokecubeMod.VERSION
                    + EnumChatFormatting.WHITE;
            String link = "" + result.url;
            String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + link + "\"}}";

            String info = "\"" + EnumChatFormatting.GOLD + "Currently Running " + "\"";
            String mess = "[" + info + "," + linkComponent + ",\"]\"]";
            return IChatComponent.Serializer.jsonToComponent(mess);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeMod.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    IChatComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Core");
                    (event.player).addChatMessage(mess);
                }
                else if (PokecubeMod.core.getConfig().loginmessage)
                {
                    IChatComponent mess = getInfoMessage(result, "Pokecube Core");
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }

    static long                                   eventTime   = 0;
    public static boolean                         renderBlock = false;
    static long                                   counter     = 0;

    public static HashMap<PokedexEntry, IPokemob> renderMobs  = new HashMap<PokedexEntry, IPokemob>();

    public static RingChecker                     checker     = new RingChecker()
                                                              {
                                                                  @Override
                                                                  public boolean hasRing(EntityPlayer player)
                                                                  {
                                                                      for (int i = 0; i < player.inventory
                                                                              .getSizeInventory(); i++)
                                                                      {
                                                                          ItemStack stack = player.inventory
                                                                                  .getStackInSlot(i);
                                                                          if (stack != null)
                                                                          {
                                                                              Item item = stack.getItem();
                                                                              if (item instanceof ItemMegaring) { return true; }
                                                                          }
                                                                      }
                                                                      return false;
                                                                  }
                                                              };

    static boolean            notifier    = false;

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

        if (renderBlock)
        {
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.5F, 1.125F, 0.5F);
            float f7 = 1.0F;
            GlStateManager.scale(-f7, -f7, f7);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            blockrendererdispatcher.renderBlockBrightness(Blocks.glass.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            renderBlock = false;
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();

    }

    private Set<RenderPlayer> addedLayers = Sets.newHashSet();
    boolean                   debug       = false;
    long                      lastSetTime = 0;

    public EventsHandlerClient()
    {
        if (!notifier) new UpdateNotifier();
        notifier = true;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.PlayerTickEvent event)
    {
        if (!PokecubeMod.core.getConfig().autoSelectMoves || event.phase == Phase.START
                || lastSetTime >= System.currentTimeMillis())
            return;
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob != null)
        {
            Entity target = ((EntityLiving) pokemob).getAttackTarget();
            if (target != null && !pokemob.getPokemonAIState(IMoveConstants.MATING))
            {
                if (target != null)
                {
                    setMostDamagingMove(pokemob, target);
                    lastSetTime = System.currentTimeMillis() + 1000;
                }
            }
        }
        if (PokecubeMod.core.getConfig().autoRecallPokemobs)
        {
            IPokemob[] pokemobs = GuiDisplayPokecubeInfo.instance().getPokemobsToDisplay();
            for (IPokemob mob : pokemobs)
            {
                if (event.player.getDistanceToEntity((Entity) mob) > PokecubeMod.core.getConfig().autoRecallDistance)
                {
                    mob.returnToPokecube();
                }
            }
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
                evt.density = 0.005f;
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        int key = Keyboard.getEventKey();

        if (Keyboard.getEventNanoseconds() == eventTime) return;

        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();

        eventTime = Keyboard.getEventNanoseconds();
        if (key == Keyboard.KEY_SPACE && player.ridingEntity instanceof IPokemob)
        {
            boolean state = Keyboard.getEventKeyState();
            MountState newState = state ? EntityMountablePokemob.MountState.UP : EntityMountablePokemob.MountState.NONE;
            if (newState != ((EntityMountablePokemob) player.ridingEntity).state)
            {
                ((EntityMountablePokemob) player.ridingEntity).state = newState;
                byte mess = (byte) EntityMountablePokemob.MountState.NONE.ordinal();
                if (state) mess = (byte) EntityMountablePokemob.MountState.UP.ordinal();
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                buffer.writeByte(MessageServer.MOUNTDIR);
                buffer.writeInt(player.ridingEntity.getEntityId());
                buffer.writeByte(mess);
                MessageServer packet = new MessageServer(buffer);
                PokecubePacketHandler.sendToServer(packet);
            }
        }
        else if (key == Keyboard.KEY_LCONTROL && player.ridingEntity instanceof IPokemob)
        {
            boolean state = Keyboard.getEventKeyState();
            MountState newState = state ? EntityMountablePokemob.MountState.DOWN
                    : EntityMountablePokemob.MountState.NONE;
            if (newState != ((EntityMountablePokemob) player.ridingEntity).state)
            {
                ((EntityMountablePokemob) player.ridingEntity).state = newState;
                byte mess = (byte) EntityMountablePokemob.MountState.NONE.ordinal();
                if (state) mess = (byte) EntityMountablePokemob.MountState.DOWN.ordinal();
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
                buffer.writeByte(MessageServer.MOUNTDIR);
                buffer.writeInt(player.ridingEntity.getEntityId());
                buffer.writeByte(mess);
                MessageServer packet = new MessageServer(buffer);
                PokecubePacketHandler.sendToServer(packet);
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobMegavolve))
        {
            boolean ring = checker.hasRing(player);

            IPokemob current = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (current != null && ring && !current.getPokemonAIState(IMoveConstants.EVOLVING)
                    && System.currentTimeMillis() > counter + 500)
            {
                counter = System.currentTimeMillis();
                MessageServer message = new MessageServer(MessageServer.CHANGEFORM, ((Entity) current).getEntityId());
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
                int num = GuiScreen.isCtrlKeyDown() ? 2 : 1;
                if (GuiScreen.isShiftKeyDown()) num++;
                if (GuiTeleport.instance().getState()) GuiTeleport.instance().nextMove();
                else GuiDisplayPokecubeInfo.instance().nextMove(num);
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
                int num = GuiScreen.isCtrlKeyDown() ? 2 : 1;
                if (GuiScreen.isShiftKeyDown()) num++;
                if (GuiTeleport.instance().getState()) GuiTeleport.instance().previousMove();
                else GuiDisplayPokecubeInfo.instance().previousMove(num);
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

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event)
    {
        if (addedLayers.contains(event.renderer)) { return; }
        event.renderer.addLayer(new RenderHeldPokemobs(event.renderer));
        addedLayers.add(event.renderer);
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
                        if (pokemob == null) continue;
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

    @SubscribeEvent
    public void textOverlay(RenderGameOverlayEvent.Text event)
    {
        if (!debug) return;
        TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getMinecraft().thePlayer);
        Vector3 v = Vector3.getNewVector().set(Minecraft.getMinecraft().thePlayer);
        String msg = "Sub-Biome: " + BiomeDatabase.getReadableNameFromType(t.getBiome(v));
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
}

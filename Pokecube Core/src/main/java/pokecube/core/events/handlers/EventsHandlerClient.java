package pokecube.core.events.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiArranger;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.client.render.RenderHealth;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
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
                    (event.player).addChatMessage(mess);
                }
                else if (PokecubeMod.core.getConfig().loginmessage)
                {
                    ITextComponent mess = getInfoMessage(result, "Pokecube Core");
                    (event.player).addChatMessage(mess);
                    mess = getIssuesMessage(result);
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }

    public static HashMap<PokedexEntry, IPokemob> renderMobs = new HashMap<PokedexEntry, IPokemob>();
    static boolean                                notifier   = false;

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
        if (pokemob == null)
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
        size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                Math.max(pokemob.getPokedexEntry().height * mobScale, pokemob.getPokedexEntry().length * mobScale));
        GL11.glPushMatrix();
        float zoom = (float) (12f / Math.sqrt(size));
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        long time = Minecraft.getSystemTime();
        if (rotates) GL11.glRotatef((time + tick) / 20f, 0, 1, 0);
        RenderHelper.enableStandardItemLighting();
        GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, 1.5F, false);
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
        if (event.phase == Phase.START || event.player != Minecraft.getMinecraft().thePlayer) return;
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob != null && PokecubeMod.core.getConfig().autoSelectMoves)
        {
            Entity target = pokemob.getEntity().getAttackTarget();
            if (target != null && !pokemob.getPokemonAIState(IMoveConstants.MATING))
            {
                setMostDamagingMove(pokemob, target);
            }
        }
        if (PokecubeMod.core.getConfig().autoRecallPokemobs)
        {
            IPokemob mob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
            if (mob != null && !(mob.getEntity().isDead) && mob.getEntity().addedToChunk && event.player
                    .getDistanceToEntity(mob.getEntity()) > PokecubeMod.core.getConfig().autoRecallDistance)
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
            if (current != null && !current.getPokemonAIState(IMoveConstants.EVOLVING))
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
                        EntityLiving entity = pokemob.getEntity();
                        entity.rotationYaw = -40;
                        entity.rotationPitch = 0;
                        entity.rotationYawHead = 0;
                        pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
                        entity.onGround = true;
                        GL11.glScaled(0.5, 0.5, 0.5);
                        renderMob(pokemob, event.getRenderPartialTicks(), false);
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
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
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
                ItemStack stack = player.inventory.mainInventory[l];
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
                    EntityLiving entity = pokemob.getEntity();
                    entity.rotationYaw = -40;
                    entity.rotationPitch = 0;
                    entity.rotationYawHead = 0;
                    pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
                    entity.onGround = true;
                    GL11.glScaled(0.5, 0.5, 0.5);
                    renderMob(pokemob, event.getPartialTicks(), false);
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

}

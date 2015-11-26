package pokecube.core.events.handlers;

import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_POISON;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_POISON2;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_TERRAIN_MISTY;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_WEATHER_HAIL;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_WEATHER_RAIN;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_WEATHER_SAND;
import static pokecube.core.moves.PokemobTerrainEffects.EFFECT_WEATHER_SUN;
import static thut.api.terrain.TerrainSegment.getWind;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiScrollableLists;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.megastuff.ItemMegaring;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.WorldTerrain;

@SideOnly(Side.CLIENT)
public class EventsHandlerClient
{
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
    private static ISound                 rain            = PositionedSoundRecord
            .create(new ResourceLocation("ambient.weather.rain"), 0.1F);
    private DynamicTexture                lightmapTexture = new DynamicTexture(16, 16);

    /** Rain X coords */
    float[] rainXCoords;
    /** Rain Y coords */
    float[] rainYCoords;

    private int rendererUpdateCount;

    private Random random = new Random();
    private int    rainSoundCounter;
    private float  last;

    public EventsHandlerClient()
    {
    }

    private Vector3 v  = Vector3.getNewVectorFromPool();
    private Vector3 v1 = Vector3.getNewVectorFromPool();

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
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();

        if (player == null || player.worldObj == null || evt.partialTicks == last) return;
        last = evt.partialTicks;
        WorldTerrain worldTerrain = TerrainManager.getInstance().getTerrain(player.worldObj);
        rendererUpdateCount++;
        int x = ((int) player.posX) >> 4;
        int y = ((int) player.posY) >> 4;
        int z = ((int) player.posZ) >> 4;
        int j0 = Math.max(0, y - 2);
        int j1 = Math.min(15, y + 2);

        for (int i = x - 2; i <= x + 2; i++)
        {
            for (int j = j0; j <= j1; j++)
            {
                for (int k = z - 2; k <= z + 2; k++)
                {
                    TerrainSegment terrain = worldTerrain.getTerrain(i, j, k);

                    PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
                    if (effect == null)
                    {
                        terrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
                    }

                    World worldObj = player.worldObj;
                    v.x = terrain.chunkX * 16 + 8;
                    v.y = terrain.chunkY * 16 + 8;
                    v.z = terrain.chunkZ * 16 + 8;

                    if (effect.effects[EFFECT_WEATHER_SAND] > 0)
                    {
                        doSandstorm(v, worldObj, terrain);
                    }

                    if (effect.effects[EFFECT_WEATHER_SUN] > 0)
                    {

                    }

                    if (effect.effects[EFFECT_WEATHER_RAIN] > 0)
                    {
                        doRain(v, worldObj, terrain, evt.partialTicks);
                    }

                    if (effect.effects[EFFECT_WEATHER_HAIL] > 0)
                    {

                    }

                    if (effect.effects[EFFECT_TERRAIN_MISTY] > 0)
                    {

                    }

                    if (effect.effects[EFFECT_TERRAIN_ELECTRIC] > 0)
                    {

                    }

                    if (effect.effects[EFFECT_POISON] > 0)
                    {
                        doToxicHaze(v, worldObj, terrain);
                    }

                    if (effect.effects[EFFECT_POISON2] > 0)
                    {
                        doToxicHaze(v, worldObj, terrain);
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void livingRender(RenderLivingEvent.Post evt)
    {
    }

    void doSandstorm(Vector3 v, World worldObj, TerrainSegment terrain)
    {
        for (int j = 0; j < 100; j++)
        {
            double x = v.x + (0.5 - Math.random()) * 16;
            double y = v.y + (0.5 - Math.random()) * 16;
            double z = v.z + (0.5 - Math.random()) * 16;
            if (terrain.isInTerrainSegment(x, y, z))
            {
                Vector3 wind = getWind(worldObj, x, y, z);
                int biome = terrain.getBiome(v.set(x, y, z));
                if (biome == BiomeType.CAVE.getType()) wind.clear();

                mod_Pokecube.spawnParticle("powder.brown", x, y, z, wind.x, wind.y, wind.z);
            }
        }
    }

    void doToxicHaze(Vector3 v, World worldObj, TerrainSegment terrain)
    {
        for (int j = 0; j < 100; j++)
        {
            double x = v.x + (0.5 - Math.random()) * 16;
            double y = v.y + (0.5 - Math.random()) * 16;
            double z = v.z + (0.5 - Math.random()) * 16;

            mod_Pokecube.spawnParticle("powder.purple", x, y, z, 0, 0, 0);
        }
    }

    @SideOnly(Side.CLIENT)
    void doRain(Vector3 v, World worldObj, TerrainSegment terrain, float partialTicks)
    {
        renderRainSnow(partialTicks, v);

        int l = 0;// weather particle effects
        for (int j = 0; j < 100; j++)
        {
            double x = v.x + (0.5 - Math.random()) * 16;
            double y = v.y + (0.5 - Math.random()) * 16;
            double z = v.z + (0.5 - Math.random()) * 16;
            BlockPos pos = new BlockPos(MathHelper.floor_double(x), (int) y, MathHelper.floor_double(z));

            Block b = worldObj.getBlockState(pos).getBlock();

            if (!b.isAir(worldObj, pos))
            {
                l++;
                // Minecraft.getMinecraft().effectRenderer.addEffect(new
                // net.minecraft.client.particle.EntityRainFX(
                // worldObj, x, (int) y + b.getBlockBoundsMaxY(), z));//TODO
                // figure out rain again
            }
            else
            {
                // Minecraft.getMinecraft().effectRenderer.addEffect(
                // new net.minecraft.client.particle.EntitySplashFX(worldObj, x,
                // y, z, 0, -0.5, 0));
            }
        }
        if (l > 0 && random.nextInt(300) < this.rainSoundCounter++)
        {
            this.rainSoundCounter = 0;
            // Minecraft.getMinecraft().getSoundHandler().playSound(rain);
            worldObj.playSound(v.x, v.y, v.z, "ambient.weather.rain", .1F, 0.5F, false);

        }
    }

    protected void renderRainSnow(float p_78474_1_, Vector3 v)
    {
        // TODO redo rain from raindance

    }

    static long eventTime = 0;

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        int key = Keyboard.getEventKey();

        if (Keyboard.getEventNanoseconds() == eventTime) return;

        eventTime = Keyboard.getEventNanoseconds();

        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if (key == Keyboard.KEY_SPACE && player.ridingEntity instanceof IPokemob)
        {
            String toSend = player.ridingEntity.getEntityId() + "`j`";

            byte[] message = toSend.getBytes();
            PokecubeServerPacket packet = PokecubePacketHandler
                    .makeServerPacket(PokecubePacketHandler.CHANNEL_ID_EntityPokemob, message);
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
            if (current != null && ring && !current.getPokemonAIState(current.EVOLVING))
            {
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                System.out.println(current);
                buf.writeByte(17);
                buf.writeInt(current.getPokemonUID());

                PokecubeServerPacket packet = new PokecubeServerPacket(buf);
                PokecubePacketHandler.sendToServer(packet);
            }
        }

        if (GameSettings.isKeyDown(ClientProxyPokecube.nextMob))
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            {
                int num = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(num, 0);
            }
            else
            {
                GuiDisplayPokecubeInfo.instance().nextPokemob();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.previousMob))
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            {
                int num = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(-num, 0);
            }
            else
            {
                GuiDisplayPokecubeInfo.instance().previousPokemob();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.nextMove))
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            {
                int num = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(0, num);
            }
            else
            {
                if (GuiScrollableLists.instance().getState()) GuiScrollableLists.instance().nextMove();
                else GuiDisplayPokecubeInfo.instance().nextMove();
            }

        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.previousMove))
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            {
                int num = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1;
                GuiDisplayPokecubeInfo.instance().moveGui(0, -num);
            }
            else
            {
                if (GuiScrollableLists.instance().getState()) GuiScrollableLists.instance().previousMove();
                else GuiDisplayPokecubeInfo.instance().previousMove();
            }
        }
        if (GameSettings.isKeyDown(ClientProxyPokecube.mobBack))
        {

            if (GuiScrollableLists.instance().getState())
            {
                GuiScrollableLists.instance().setState(false);
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
                if (gui.mc.thePlayer == null || !Keyboard.isKeyDown(Keyboard.KEY_LMENU)) { return; }
                List<Slot> slots = gui.inventorySlots.inventorySlots;
                int w = gui.width;
                int h = gui.height;
                int num = 0;

                int xSize = gui.xSize;
                int ySize = gui.ySize;
                float yang = 30;
                float xang = 30;
                float zang = 15;
                float zLevel = 800;
                GL11.glPushMatrix();
                GlStateManager.translate(0, 0, zLevel);
                for (Slot slot : slots)
                {
                    if (slot.getHasStack() && PokecubeManager.isFilled(slot.getStack()))
                    {
                        IPokemob pokemob = getPokemobForRender(slot.getStack(), gui.mc.theWorld);

                        int x = (w - xSize) / 2;
                        int y = (h - ySize) / 2;
                        int i, j;
                        i = slot.xDisplayPosition + 8;
                        j = slot.yDisplayPosition + 10;
                        GL11.glPushMatrix();
                        GL11.glTranslatef(i + x, j + y, 0F);
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
            if (player == null || !Keyboard.isKeyDown(Keyboard.KEY_LMENU)
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

                    int x = (w - xSize) / 2;
                    int y = (h - ySize);
                    GL11.glPushMatrix();
                    GL11.glTranslatef(i + x + 20 * l, j + y, 0F);
                    renderMob(pokemob, event.partialTicks);
                    GL11.glPopMatrix();
                }
            }
            GL11.glPopMatrix();
        }
    }

    static HashMap<PokedexEntry, IPokemob> renderMobs = new HashMap();

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
                renderMobs.put(entry, pokemob);
                // System.out.println(entry);
                if (pokemob == null) return null;
            }
            Entity poke = (Entity) pokemob;
            NBTTagCompound pokeTag = itemStack.getTagCompound().getCompoundTag("Pokemob");
            poke.readFromNBT(pokeTag);
            pokemob.popFromPokecube();// should reinit status
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
        EntityLiving entity = (EntityLiving) pokemob;

        float size = 0;

        size = Math.max(entity.width, entity.height) * 4;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        float zoom = 30f / size;
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        long time = Minecraft.getMinecraft().getSystemTime();
        GL11.glRotatef((time + tick) / 20f, 0, 1, 0);
        RenderHelper.enableStandardItemLighting();

        GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
        float offset = 0.4f;
        float f, f1, f2;

        entity.rotationYaw = 0;
        entity.rotationPitch = 0;
        entity.rotationYawHead = 0;

        pokemob.setPokemonAIState(pokemob.SITTING, true);
        entity.onGround = true;

        int i = 15728880;
        int j1 = i % 65536;
        int k1 = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
        Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0, -0.123456, 0, 0, 1.5F);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);

    }
}

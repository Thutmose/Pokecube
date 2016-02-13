package pokecube.adventures.events;

import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.client.render.item.BagRenderer;
import pokecube.adventures.client.render.item.RingRenderer;
import pokecube.adventures.handlers.PlayerAsPokemobManager;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.items.bags.ItemBag;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

@SideOnly(Side.CLIENT)
public class RenderHandler
{

    public static float   partialTicks = 0.0F;
//    public static boolean BOTANIA      = false;
    private Set<RenderPlayer> addedLayers = Sets.newHashSet();

    public RenderHandler()
    {
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event)
    {
        if (addedLayers.contains(event.renderer)) { return; }
        event.renderer.addLayer(new RingRenderer(event.renderer));
        event.renderer.addLayer(new BagRenderer(event.renderer));
        addedLayers.add(event.renderer);
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
        boolean bag = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                Item item = stack.getItem();
                if (item instanceof ItemBag)
                {
                    bag = true;
                    break;
                }
            }
        }
        if (bag && Keyboard.getEventKey() == ClientProxy.bag.getKeyCode())
        {
            PacketPokeAdv.sendBagOpenPacket(false, Vector3.empty);
        }

        IPokemob entity = PlayerAsPokemobManager.getInstance().getTransformed(player);
        if (entity != null && Keyboard.getEventKey() == ClientProxyPokecube.mobAttack.getKeyCode())
        {
            Vector3 here = Vector3.getNewVectorFromPool().set(player, false);
            Entity hit = here.firstEntityExcluding(16, Vector3.getNewVectorFromPool().set(player.getLookVec()),
                    player.worldObj, false, player);
            if (hit != null)
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeByte(10);
                buffer.writeInt(hit.getEntityId());
                MessageServer message = new MessageServer(buffer);
                PokecubeMod.packetPipeline.sendToServer(message);
            }
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent evt)
    {
        EntityPlayer player = evt.entityPlayer;
        ItemStack stack = evt.itemStack;
        if (player == null || player.openContainer == null || stack == null) return;
        if (player.openContainer instanceof ContainerCloner && stack.getItem() instanceof ItemPokemobEgg)
        {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
            {
                evt.toolTip.add("" + stack.getTagCompound().getLong("ivs") + ":"
                        + stack.getTagCompound().getFloat("size") + ":" + stack.getTagCompound().getByte("nature"));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void ClientRenderTick(RenderFogEvent evt)
    {
        if (TeamEventsHandler.shouldRenderVolume)
        {
            GL11.glPushMatrix();
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(player);
            Vector3 v = Vector3.getNewVectorFromPool();// .set(evt.entity);
            Vector3 source = Vector3.getNewVectorFromPool().set(player);
            Vector3 target = t.getCentre();
            source.set(target.subtract(source));
            Vector3 diff = Vector3.getNewVectorFromPool();
            diff.x = player.prevPosX - player.posX;
            diff.y = player.prevPosY - player.posY;
            diff.z = player.prevPosZ - player.posZ;
            diff.scalarMultBy(evt.renderPartialTicks);
            source.addTo(diff);
            GL11.glTranslated(source.x, source.y, source.z);
            int rgba = 0xFFFFFFFF;
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(t.getCentre().getPos(), player.dimension);
            if (TeamManager.getInstance().isOwned(c))
            {
                if (!TeamManager.getInstance().isTeamLand(c, player.getTeam().getRegisteredName()))
                {
                    rgba = 0xFFFF0000;
                }
                else
                {
                    rgba = 0xFF00FF00;
                }
            }
            renderDebugBoundingBox(v.getAABB().expand(8, 4, 8), rgba);
            renderDebugBoundingBox(v.getAABB().expand(8, 8, 8), rgba);
            renderDebugBoundingBox(v.getAABB().expand(4, 8, 8), rgba);
            renderDebugBoundingBox(v.getAABB().expand(8, 8, 4), rgba);
            renderDebugBoundingBox(v.getAABB().expand(8, 8, 0), rgba);
            renderDebugBoundingBox(v.getAABB().expand(0, 8, 8), rgba);
            renderDebugBoundingBox(v.getAABB().expand(8, 0, 8), rgba);
            GL11.glPopMatrix();
        }
    }

    /** Renders the bounding box around an entity when F3+B is pressed */
    private void renderDebugBoundingBox(AxisAlignedBB box, int rgba)
    {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        int alpha = ((rgba >> 24) & 255);
        int red = ((rgba >> 16) & 255);
        int green = ((rgba >> 8) & 255);
        int blue = (rgba & 255);
        RenderGlobal.drawOutlinedBoundingBox(box, red, green, blue, alpha);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}

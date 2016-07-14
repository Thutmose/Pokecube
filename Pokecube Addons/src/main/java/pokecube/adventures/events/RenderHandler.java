package pokecube.adventures.events;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.client.ClientProxy;
import pokecube.adventures.client.render.item.BagRenderer;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

@SideOnly(Side.CLIENT)
public class RenderHandler
{
    public static float partialTicks = 0.0F;

    public RenderHandler()
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void ClientRenderTick(RenderFogEvent evt)
    {
        if (TeamEventsHandler.shouldRenderVolume)
        {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            Vector3 source = Vector3.getNewVector();

            World world = player.getEntityWorld();
            int size = 3;
            Vector3 r = Vector3.getNewVector();
            for (int i = 0; i < size * size * size; i++)
            {
                Cruncher.indexToVals(i, r);
                GL11.glPushMatrix();
                TerrainSegment t = TerrainManager.getInstance().getTerrain(world, player.posX + 16 * r.x,
                        player.posY + 16 * r.y, player.posZ + 16 * r.z);
                Vector3 v = Vector3.getNewVector();
                source.set(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
                Vector3 target = t.getCentre();
                source.set(target.subtract(source));
                Vector3 diff = Vector3.getNewVector();
                diff.x = player.lastTickPosX - player.posX;
                diff.y = player.lastTickPosY - player.posY;
                diff.z = player.lastTickPosZ - player.posZ;
                diff.scalarMultBy(evt.getRenderPartialTicks());
                source.addTo(diff);
                GL11.glTranslated(source.x, source.y, source.z);
                int rgba = 0xFFFFFFFF;
                ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(t.getCentre().getPos(),
                        player.dimension);
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
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        boolean bag = BagRenderer.getChecker().isWearingBag(player);
        if (bag && Keyboard.getEventKey() == ClientProxy.bag.getKeyCode())
        {
            PacketPokeAdv.sendBagOpenPacket();
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent evt)
    {
        EntityPlayer player = evt.getEntityPlayer();
        ItemStack stack = evt.getItemStack();
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean("isapokebag"))
        {
            evt.getToolTip().add("PokeBag");
        }
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            String colour = I18n.format(
                    EnumDyeColor.byDyeDamage(stack.getTagCompound().getInteger("dyeColour")).getUnlocalizedName());
            boolean has = false;
            for (String s : evt.getToolTip())
            {
                has = s.equals(colour);
                if (has) break;
            }
            if (!has) evt.getToolTip().add(colour);
        }
        if (player == null || player.openContainer == null || stack == null) return;
        if (player.openContainer instanceof ContainerCloner && stack.getItem() instanceof ItemPokemobEgg)
        {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
            {
                evt.getToolTip().add("" + stack.getTagCompound().getLong("ivs") + ":"
                        + stack.getTagCompound().getFloat("size") + ":" + stack.getTagCompound().getByte("nature"));
            }
        }
    }

    /** Renders the bounding box around an entity when F3+B is pressed */
    private void renderDebugBoundingBox(AxisAlignedBB boundingBox, int rgba)
    {
        GlStateManager.disableTexture2D();
        int alpha = ((rgba >> 24) & 255);
        int red = ((rgba >> 16) & 255);
        int green = ((rgba >> 8) & 255);
        int blue = (rgba & 255);
        if (rgba == 0xFFFFFFFF) GlStateManager.glLineWidth(0.5f);
        else GlStateManager.glLineWidth(5f);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        tessellator.draw();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        tessellator.draw();
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha)
                .endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }
}

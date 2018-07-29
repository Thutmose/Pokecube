package pokecube.adventures.items;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class ItemSubbiomeSetter extends Item
{
    public ItemSubbiomeSetter()
    {
        super();
        this.setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void RenderBounds(DrawBlockHighlightEvent event)
    {
        ItemStack held;
        EntityPlayer player = event.getPlayer();
        if ((held = player.getHeldItemMainhand()) != null || (held = player.getHeldItemOffhand()) != null)
        {
            BlockPos pos = event.getTarget().getBlockPos();
            if (pos == null) return;
            if (!player.getEntityWorld().getBlockState(pos).getMaterial().isSolid())
            {
                Vec3d loc = player.getPositionVector().addVector(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(2));
                pos = new BlockPos(loc);
            }

            if (held.getItem() == this && held.getTagCompound() != null && held.getTagCompound().hasKey("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTagCompound().getCompoundTag("min"), "").getPos();
                BlockPos max = pos;
                AxisAlignedBB box = new AxisAlignedBB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ).add(1, 1, 1);
                box = new AxisAlignedBB(min, max);
                float partialTicks = event.getPartialTicks();
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                box = box.offset(-d0, -d1, -d2);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.color(1.0F, 0.0F, 0.0F, 1F);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder vertexbuffer = tessellator.getBuffer();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                tessellator.draw();
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if (world.isRemote && !player.isSneaking())
        {
            player.openGui(PokecubeAdv.instance, 5, player.getEntityWorld(), 0, 0, 0);
        }
        else if (player.isSneaking() && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("min"))
        {
            String s = itemstack.getTagCompound().getString("biome");
            BiomeType type = BiomeType.getBiome(s);
            TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(player);

            Vector3 pos1 = Vector3.readFromNBT(itemstack.getTagCompound().getCompoundTag("min"), "");
            itemstack.getTagCompound().removeTag("min");
            Vec3d loc = player.getPositionVector().addVector(0, player.getEyeHeight(), 0)
                    .add(player.getLookVec().scale(2));
            Vector3 hit = Vector3.getNewVector().set(loc);
            Vector3 pos2 = hit;

            double xMin = Math.min(pos1.x, pos2.x);
            double yMin = Math.min(pos1.y, pos2.y);
            double zMin = Math.min(pos1.z, pos2.z);
            double xMax = Math.max(pos1.x, pos2.x);
            double yMax = Math.max(pos1.y, pos2.y);
            double zMax = Math.max(pos1.z, pos2.z);
            double x, y, z;

            for (x = xMin; x <= xMax; x++)
                for (y = yMin; y <= yMax; y++)
                    for (z = zMin; z <= zMax; z++)
                    {
                        pos1.set(x, y, z);
                        if (!world.isAreaLoaded(pos1.getPos(), 0))
                        {
                            world.getChunkFromBlockCoords(pos1.getPos());
                        }
                        if (!world.isAreaLoaded(pos1.getPos(), 0))
                        {
                            System.err.println("not loadted");
                            continue;
                        }
                        t = TerrainManager.getInstance().getTerrian(world, pos1);
                        t.setBiome(pos1, type.getType());
                        t.toSave = true;
                    }
            try
            {
                if (!world.isRemote) player.sendMessage(
                        new TextComponentString("Second Position " + hit + ", setting all in between to " + s));
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Vector3 hit = Vector3.getNewVector().set(pos);
        if (stack.hasTagCompound())
        {
            if (!playerIn.isSneaking())
            {
                NBTTagCompound minTag = new NBTTagCompound();
                hit.writeToNBT(minTag, "");
                stack.getTagCompound().setTag("min", minTag);
                if (!worldIn.isRemote)
                    playerIn.sendMessage(new TextComponentString("First Position " + hit.set(hit.getPos())));
            }
            else if (playerIn.isSneaking() && stack.getTagCompound().hasKey("min"))
            {
                String s = stack.getTagCompound().getString("biome");
                BiomeType type = BiomeType.getBiome(s);
                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(playerIn);

                Vector3 pos1 = Vector3.readFromNBT(stack.getTagCompound().getCompoundTag("min"), "");
                stack.getTagCompound().removeTag("min");
                Vector3 pos2 = hit;

                double xMin = Math.min(pos1.x, pos2.x);
                double yMin = Math.min(pos1.y, pos2.y);
                double zMin = Math.min(pos1.z, pos2.z);
                double xMax = Math.max(pos1.x, pos2.x);
                double yMax = Math.max(pos1.y, pos2.y);
                double zMax = Math.max(pos1.z, pos2.z);
                double x, y, z;

                for (x = xMin; x <= xMax; x++)
                    for (y = yMin; y <= yMax; y++)
                        for (z = zMin; z <= zMax; z++)
                        {
                            pos1.set(x, y, z);
                            if (!worldIn.isAreaLoaded(pos1.getPos(), 0))
                            {
                                worldIn.getChunkFromBlockCoords(pos1.getPos());
                            }
                            if (!worldIn.isAreaLoaded(pos1.getPos(), 0))
                            {
                                System.err.println("not loadted");
                                continue;
                            }
                            t = TerrainManager.getInstance().getTerrian(worldIn, pos1);
                            t.setBiome(pos1, type.getType());
                            t.toSave = true;
                        }
                try
                {
                    if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentString(
                            "Second Position " + hit.set(hit.getPos()) + ", setting all in between to " + s));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.SUCCESS;
    }
}

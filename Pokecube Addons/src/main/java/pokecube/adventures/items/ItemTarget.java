package pokecube.adventures.items;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatItem;
import thut.lib.CompatWrapper;

public class ItemTarget extends CompatItem
{
    public ItemTarget()
    {
        super();
        this.setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void interact(EntityInteract event)
    {
        ItemStack stack = event.getItemStack();
        if (!CompatWrapper.isValid(stack) || stack.getItem() != this) return;
        EntityPlayer playerIn = event.getEntityPlayer();
        Entity target = event.getTarget();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(target);
        if (stack.getItemDamage() == 1 && pokemob != null)
        {
            if (stack.hasTagCompound() && playerIn == pokemob.getPokemonOwner())
            {
                Vector4 pos = new Vector4(stack.getTagCompound().getCompoundTag("link"));
                pokemob.setHome(MathHelper.floor(pos.x), MathHelper.floor(pos.y - 1),
                        MathHelper.floor(pos.z), 16);
                // TODO localize this message.
                playerIn.sendMessage(new TextComponentString("Set Home to " + pos));
                event.setCanceled(true);
            }
        }
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

    @SideOnly(Side.CLIENT)
    @Override
    protected List<ItemStack> getTabItems(Item itemIn, CreativeTabs tab)
    {
        List<ItemStack> subItems = Lists.newArrayList();
        if (!this.isInCreativeTab(tab)) return subItems;
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(new ItemStack(itemIn, 1, 1));
        subItems.add(new ItemStack(itemIn, 1, 3));
        return subItems;
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getItemDamage();

        if (i == 1) return "item.warplinker";
        if (i == 3) { return "item.biomeSetter"; }

        return super.getUnlocalizedName();
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        int meta = itemstack.getItemDamage();
        if (meta == 3)
        {

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
        }

        if (world.isRemote) { return new ActionResult<>(EnumActionResult.PASS, itemstack); }

        if (player.isSneaking() && meta == 10)
        {
            TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(player);
            t.refresh(world);
        }
        else if (meta == 1)
        {
        }
        else if (meta == 2)
        {
        }
        else if (meta == 3)
        {
        }
        else if (!player.isSneaking())
        {
            Vector3 location = Vector3.getNewVector().set(player).add(Vector3.getNewVector().set(player.getLookVec()))
                    .add(0, 1.62, 0);
            EntityTarget t = new EntityTarget(world);
            location.moveEntity(t);
            world.spawnEntity(t);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Vector3 hit = Vector3.getNewVector().set(pos);
        Block block = hit.getBlock(worldIn);
        int meta = stack.getItemDamage();
        if (meta == 1 && !worldIn.isRemote)
        {
            if (block instanceof BlockWarpPad && playerIn.isSneaking() && stack.hasTagCompound())
            {
                TileEntityWarpPad pad = (TileEntityWarpPad) hit.getTileEntity(worldIn);
                if (pad.canEdit(playerIn))
                {
                    pad.link = new Vector4(stack.getTagCompound().getCompoundTag("link"));
                    playerIn.sendMessage(new TextComponentString("linked pad to " + pad.link));
                }
            }
            else
            {
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound linkTag = new NBTTagCompound();
                Vector4 link = new Vector4(hit.x + 0.5, hit.y + 1, hit.z + 0.5, playerIn.dimension);
                link.writeToNBT(linkTag);
                stack.getTagCompound().setTag("link", linkTag);
                playerIn.sendMessage(new TextComponentString("Saved location " + link));
            }
        }
        if (meta == 2 && !worldIn.isRemote)
        {

            if (playerIn.isSneaking())
            {

            }
            else
            {

            }
        }
        if (meta == 3)
        {
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
        return EnumActionResult.FAIL;
    }
}

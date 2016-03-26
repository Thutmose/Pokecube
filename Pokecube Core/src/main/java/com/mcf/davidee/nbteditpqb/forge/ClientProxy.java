package com.mcf.davidee.nbteditpqb.forge;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbteditpqb.NBTEdit;
import com.mcf.davidee.nbteditpqb.gui.GuiEditNBTTree;
import com.mcf.davidee.nbteditpqb.nbt.SaveStates;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pokecube.core.client.render.PTezzelator;

public class ClientProxy extends CommonProxy
{

    private static class GuiOpener
    {
        final Object         target;
        final NBTTagCompound tag;

        public GuiOpener(Object target, NBTTagCompound tag)
        {
            this.tag = tag;
            this.target = target;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(ClientTickEvent event)
        {
            if (target instanceof BlockPos)
                Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree((BlockPos) target, tag));
            else if (target instanceof Integer)
                Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree((int) target, tag));
            MinecraftForge.EVENT_BUS.unregister(this);
        }

    }

    private void drawBoundingBox(RenderGlobal r, float f, AxisAlignedBB aabb)
    {
        if (aabb == null) return;

        Entity player = Minecraft.getMinecraft().getRenderViewEntity();

        double var8 = player.lastTickPosX + (player.posX - player.lastTickPosX) * f;
        double var10 = player.lastTickPosY + (player.posY - player.lastTickPosY) * f;
        double var12 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * f;

        aabb = aabb.addCoord(-var8, -var10, -var12);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 0.0F, 0.0F, .5F);
        GL11.glLineWidth(3.5F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        PTezzelator tez = PTezzelator.instance;

        tez.begin(3);
        tez.vertex(aabb.minX, aabb.minY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.minY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.minY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.minY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.minY, aabb.minZ);
        tez.end();
        tez.begin(3);
        tez.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.maxY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        tez.end();
        tez.begin(1);
        tez.vertex(aabb.minX, aabb.minY, aabb.minZ);
        tez.vertex(aabb.minX, aabb.maxY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.minY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.maxY, aabb.minZ);
        tez.vertex(aabb.maxX, aabb.minY, aabb.maxZ);
        tez.vertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.minY, aabb.maxZ);
        tez.vertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tez.end();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    @Override
    public File getMinecraftDirectory()
    {
        return FMLClientHandler.instance().getClient().mcDataDir;
    }

    @Override
    public void openEditGUI(BlockPos pos, NBTTagCompound tag)
    {
        new GuiOpener(pos, tag);
    }

    @Override
    public void openEditGUI(int entityID, NBTTagCompound tag)
    {
        new GuiOpener(entityID, tag);
    }

    @Override
    public void registerInformation()
    {
        MinecraftForge.EVENT_BUS.register(this);
        SaveStates save = NBTEdit.getSaveStates();
        save.load();
        save.save();
    }

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent event)
    {
        GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
        if (curScreen instanceof GuiEditNBTTree)
        {
            GuiEditNBTTree screen = (GuiEditNBTTree) curScreen;
            Entity e = screen.getEntity();

            if (e != null && e.isEntityAlive())
                drawBoundingBox(event.getContext(), event.getPartialTicks(), e.getEntityBoundingBox());
            else if (screen.isTileEntity())
            {
                int x = screen.getBlockX();
                int y = screen.y;
                int z = screen.z;
                World world = Minecraft.getMinecraft().theWorld;
                BlockPos pos = new BlockPos(x, y, z);
                IBlockState state = world.getBlockState(pos);
                if (state != null)
                {
                    drawBoundingBox(event.getContext(), event.getPartialTicks(), state.getSelectedBoundingBox(world, pos));
                }
            }
        }
    }
}

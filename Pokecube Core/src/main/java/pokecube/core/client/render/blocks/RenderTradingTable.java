package pokecube.core.client.render.blocks;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.interfaces.IPokecube;
import thut.core.common.blocks.BlockRotatable;

@SuppressWarnings({ "rawtypes" })
public class RenderTradingTable extends TileEntitySpecialRenderer
{
    public void renderItem(double x, double y, double z, ItemStack item, int time)
    {
        if (item.getItem() instanceof IPokecube)
        {
            glPushMatrix();

            glPopMatrix();
        }
    }

    @Override
    public void render(TileEntity tileentity, double x, double y, double z, float f, int i1, float f1)
    {
        TileEntityTradingTable table = (TileEntityTradingTable) tileentity;
        IBlockState state = table.getWorld().getBlockState(table.getPos());
        if (!(state.getBlock() instanceof BlockTradingTable)) return;

        glPushMatrix();
        glTranslated(x, y, z);

        glPushMatrix();
        glTranslated(0.5, 0.375, 0.5);
        float scale = 0.1f;
        glScalef(scale, scale * 0.75f, scale);

        glPopMatrix();

        EnumFacing dir = state.getValue(BlockRotatable.FACING);
        switch (dir)
        {
        case EAST:
            glTranslatef(0, 0, 1);
            glRotatef(90, 0.0F, 1.0F, 0.0F);
            break;
        case NORTH:
            glTranslatef(1, 0, 1);
            glRotatef(180, 0.0F, 1.0F, 0.0F);
            break;
        case SOUTH:
            break;
        case WEST:
            glTranslatef(1, 0, 0);
            glRotatef(270, 0.0F, 1.0F, 0.0F);
            break;
        default:
            break;
        }
        if (!table.getStackInSlot(0).isEmpty())
        {
            glPushMatrix();
            glTranslatef(-0.375f, 0.2f, 0);
            glTranslatef(0.5F, 0.8f, 0.5F);
            glScalef(0.15f, 0.15f, 0.15f);
            glRotatef(-90, 0, 1, 0);
            if (table.getStackInSlot(0).getItem() instanceof IPokecube) Minecraft.getMinecraft().getItemRenderer()
                    .renderItem(Minecraft.getMinecraft().player, table.getStackInSlot(0), TransformType.NONE);
            glPopMatrix();
        }
        if (!table.getStackInSlot(1).isEmpty())
        {
            glPushMatrix();
            glTranslatef(0.375f, 0.2f, 0);
            glTranslatef(0.5F, 0.8f, 0.5F);
            glScalef(0.15f, 0.15f, 0.15f);
            glRotatef(90, 0, 1, 0);
            if (table.getStackInSlot(1).getItem() instanceof IPokecube) Minecraft.getMinecraft().getItemRenderer()
                    .renderItem(Minecraft.getMinecraft().player, table.getStackInSlot(1), TransformType.NONE);
            glPopMatrix();
        }

        glPopMatrix();

    }
}

package pokecube.core.world.gen.template;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.annotation.Nullable;

import jeresources.util.ReflectionHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;

public class TemplateProcessor extends BlockRotationProcessor
{
    private static final Method GETBIOMEBLOCK = ReflectionHelper.findMethod(StructureVillagePieces.Village.class, null,
            new String[] { "func_175847_a", "getBiomeSpecificBlockState" }, IBlockState.class);
    static
    {
        GETBIOMEBLOCK.setAccessible(true);
    }

    final StructureVillagePieces.Start init;

    public TemplateProcessor(World worldIn, BlockPos pos, PlacementSettings settings)
    {
        super(pos, settings);
        init = new Start(worldIn.getBiomeProvider(), 0, worldIn.rand, (pos.getX() << 4) + 2, (pos.getZ() << 4) + 2,
                new ArrayList<StructureVillagePieces.PieceWeight>(), 0);
    }

    @Nullable
    public Template.BlockInfo func_189943_a(World world, BlockPos pos, Template.BlockInfo info)
    {
        info = super.func_189943_a(world, pos, info);
        if (info != null)
        {
            try
            {
                IBlockState newstate = (IBlockState) GETBIOMEBLOCK.invoke(init, info.blockState);
                info = new BlockInfo(info.pos, newstate, info.tileentityData);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
        return info;
    }

}

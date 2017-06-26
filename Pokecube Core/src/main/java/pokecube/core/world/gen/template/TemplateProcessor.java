package pokecube.core.world.gen.template;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Well;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class TemplateProcessor extends BlockRotationProcessor
{
    private static final Method GETBIOMEBLOCK = ReflectionHelper.findMethod(StructureVillagePieces.Village.class,
            "getBiomeSpecificBlockState", "func_175847_a", IBlockState.class);
    static
    {
        GETBIOMEBLOCK.setAccessible(true);
    }

    final StructureVillagePieces.Well init;

    public TemplateProcessor(World worldIn, BlockPos pos, PlacementSettings settings)
    {
        super(pos, settings);
        int x = pos.getX();
        int z = pos.getZ();
        Start start = new Start(worldIn.getBiomeProvider(), 0, worldIn.rand, x, z,
                new ArrayList<StructureVillagePieces.PieceWeight>(), 0);
        init = new Well(start, 0, worldIn.rand, x, z);
    }

    @Nullable
    public Template.BlockInfo processBlock(World world, BlockPos pos, Template.BlockInfo info)
    {
        info = super.processBlock(world, pos, info);
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

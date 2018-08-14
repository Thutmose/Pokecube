package toughasnails.temperature;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;

public class BlockTemperatureData {

    public IBlockState state;
    public float blockTemperature;
    public String[] useProperties;

    public BlockTemperatureData(@Nonnull IBlockState state, String[] useProperties, float blockTemperature) {
        this.state = state;
        this.useProperties = useProperties;
        this.blockTemperature = blockTemperature;
    }

}

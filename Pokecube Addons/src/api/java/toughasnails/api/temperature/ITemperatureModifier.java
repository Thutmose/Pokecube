package toughasnails.api.temperature;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITemperatureModifier
{
    Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor);
    Temperature applyPlayerModifiers(@Nonnull EntityPlayer player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor);

    boolean isPlayerSpecific();
    @Nonnull String getId();
}

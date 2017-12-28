package pokecube.core.interfaces;

import java.io.File;
import java.util.UUID;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.api.maths.Vector3;

@SuppressWarnings("rawtypes")
public abstract class CommonProxy
{
    public void initClient()
    {
    }

    public boolean isSoundPlaying(Vector3 location)
    {
        return false;
    }

    public void toggleSound(SoundEvent sound, BlockPos location)
    {

    }

    public void registerPokecubeRenderer(ResourceLocation cubeId, Render renderer, Object mod)
    {
    }

    public StatisticsManager getManager(UUID player)
    {
        MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        StatisticsManagerServer manager = mcServer.getPlayerList().playerStatFiles.get(player);
        if (manager == null)
        {
            File file1 = new File(mcServer.getWorld(0).getSaveHandler().getWorldDirectory(), "stats");
            File file2 = new File(file1, player + ".json");
            manager = new StatisticsManagerServer(mcServer, file2);
            manager.readStatFile();
            mcServer.getPlayerList().playerStatFiles.put(player, manager);
        }
        return manager;
    }

    public EntityPlayer getPlayer(UUID player)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(player);
    }

    public abstract void registerPokemobModel(String name, ModelBase model, Object mod);

    public abstract void registerPokemobRenderer(String name, IRenderFactory renderer, Object mod);;
}

package pokecube.core.moves.animations;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.maths.Vector3;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatParser.ClassFinder;

public class MoveAnimationHelper
{
    static Map<String, Class<? extends MoveAnimationBase>> presets = Maps.newHashMap();

    static
    {
        List<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(MoveAnimationHelper.class.getPackage().getName());
            for (Class<?> candidateClass : foundClasses)
            {
                AnimPreset preset = candidateClass.getAnnotation(AnimPreset.class);
                if (preset != null && MoveAnimationBase.class.isAssignableFrom(candidateClass))
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends MoveAnimationBase> presetClass = (Class<? extends MoveAnimationBase>) candidateClass;
                    presets.put(preset.getPreset(), presetClass);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static IMoveAnimation getAnimationPreset(String anim)
    {
        IMoveAnimation animation = null;
        if (anim == null || anim.isEmpty()) return animation;
        String preset = anim.split(":")[0];
        Class<? extends MoveAnimationBase> presetClass = presets.get(preset);
        if (presetClass != null)
        {
            try
            {
                animation = presetClass.newInstance();
                ((MoveAnimationBase) animation).init(anim);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return animation;
    }

    private static MoveAnimationHelper instance;

    public static MoveAnimationHelper Instance()
    {
        if (instance == null)
        {
            instance = new MoveAnimationHelper();
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    final Vector3 source = Vector3.getNewVector();
    final Vector3 target = Vector3.getNewVector();
    final int     index;

    public MoveAnimationHelper()
    {
        TerrainSegment dummy = new TerrainSegment(0, 0, 0);
        int found = -1;
        for (int i = 0; i < dummy.effectArr.length; i++)
        {
            if (dummy.effectArr[i] instanceof PokemobTerrainEffects)
            {
                found = i;
                break;
            }
        }
        index = found;
    }

    private int effects = 0;

    public void clear()
    {
        effects = 0;
    }

    public void addEffect()
    {
        effects++;
    }

    public void clearEffect()
    {
        effects = Math.max(0, effects - 1);
    }

    Map<BlockPos, TerrainSegment> terrainMap = Maps.newHashMap();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void worldLoad(WorldEvent.Load evt)
    {
        if (!evt.getWorld().isRemote) return;
        terrainMap.clear();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void chunkUnload(ChunkEvent.Unload evt)
    {
        if (!evt.getWorld().isRemote) return;
        for (int i = 0; i < 16; i++)
        {
            terrainMap.remove(new BlockPos(evt.getChunk().x, i, evt.getChunk().z));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCapabilityAttach(AttachCapabilitiesEvent<Chunk> event)
    {
        if (!event.getObject().getWorld().isRemote) return;
        if (event.getCapabilities().containsKey(TerrainManager.TERRAINCAP))
        {
            ITerrainProvider provider = (ITerrainProvider) event.getCapabilities().get(TerrainManager.TERRAINCAP);
            for (int i = 0; i < 16; i++)
            {
                terrainMap.put(new BlockPos(event.getObject().x, i, event.getObject().z),
                        provider.getTerrainSegment(i));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        if (effects == 0) return;
        int num = 0;
        try
        {
            if (index == -1) return;
            EntityPlayer player = Minecraft.getMinecraft().player;
            source.set(player);
            int range = 4;
            MutableBlockPos pos = new MutableBlockPos();
            for (int i = -range; i <= range; i++)
            {
                for (int j = -range; j <= range; j++)
                {
                    for (int k = -range; k <= range; k++)
                    {
                        source.set(player);
                        pos.setPos(player.chunkCoordX + i, player.chunkCoordY + j, player.chunkCoordZ + k);
                        TerrainSegment segment = terrainMap.get(pos);
                        if (segment == null) continue;
                        PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.effectArr[index];
                        if (teffect == null || !teffect.hasEffects()) continue;
                        target.set(segment.getCentre());
                        source.set(target.subtractFrom(source));
                        // Clear out the jitteryness from rendering
                        double d0 = (-player.posX + player.lastTickPosX) * event.getRenderPartialTicks();
                        double d1 = (-player.posY + player.lastTickPosY) * event.getRenderPartialTicks();
                        double d2 = (-player.posZ + player.lastTickPosZ) * event.getRenderPartialTicks();
                        source.addTo(d0, d1, d2);
                        GL11.glPushMatrix();
                        GL11.glTranslated(source.x, source.y, source.z);
                        teffect.renderTerrainEffects(event);
                        GL11.glPopMatrix();
                        num++;
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        effects = num;
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
    }
}

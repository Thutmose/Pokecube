package pokecube.core.moves.animations;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.maths.Vector3;
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

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        if (effects == 0) return;
        try
        {
            if (index == -1) return;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            source.set(player);
            GL11.glPushMatrix();
            int range = 4;
            for (int i = -range; i <= range; i++)
            {
                for (int j = -range; j <= range; j++)
                {
                    for (int k = -range; k <= range; k++)
                    {
                        source.set(player);
                        TerrainSegment segment = TerrainManager.getInstance().getTerrain(player.getEntityWorld(),
                                player.posX + i * 16, player.posY + j * 16, player.posZ + k * 16);
                        PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.effectArr[index];
                        if (teffect == null) continue;
                        target.set(segment.getCentre());
                        GL11.glPushMatrix();
                        source.set(target.subtractFrom(source));
                        GL11.glTranslated(source.x, source.y, source.z);
                        // Clear out the jitteryness from rendering
                        double d0 = (-player.posX + player.lastTickPosX) * event.getRenderPartialTicks();
                        double d1 = (-player.posY + player.lastTickPosY) * event.getRenderPartialTicks();
                        double d2 = (-player.posZ + player.lastTickPosZ) * event.getRenderPartialTicks();
                        source.set(d0, d1, d2);
                        GL11.glTranslated(source.x, source.y, source.z);
                        teffect.renderTerrainEffects(event);
                        GL11.glPopMatrix();
                    }
                }
            }
            GL11.glPopMatrix();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
    }
}

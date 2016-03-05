package pokecube.modelloader.client.render.tabula.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.modelloader.client.render.model.IPartTexturer;
import pokecube.modelloader.client.render.tabula.json.JsonTabulaModel;
import pokecube.modelloader.client.render.tabula.model.modelbase.MowzieModelBase;
import pokecube.modelloader.client.render.tabula.model.modelbase.MowzieModelRenderer;

/** @author gegy1000
 * @since 0.1.0 */
@SideOnly(Side.CLIENT)
public class ModelJson extends MowzieModelBase
{
    private JsonTabulaModel                      tabulaModel;

    public Map<String, MowzieModelRenderer>      nameMap       = Maps.newHashMap();
    public Map<String, MowzieModelRenderer>      identifierMap = Maps.newHashMap();

    /** This is an ordered list of CubeGroup Identifiers. It is used to ensure
     * that translucent parts render in the correct order. */
    ArrayList<String>                            groupIdents   = Lists.newArrayList();
    /** Map of CubeGroup Identifiers to Sets of Root parts on the group. Uses
     * the above list to get keys */
    public Map<String, Set<MowzieModelRenderer>> groupMap      = Maps.newHashMap();

    public ArrayList<Animation>                  animations    = Lists.newArrayList();
    /** Map of names to animations, used to get animations for rendering more
     * easily */
    public HashMap<String, Animation>            animationMap  = Maps.newHashMap();

    public Set<Animation>                        playing       = Sets.newHashSet();

    public IPartTexturer                         texturer;
    public Animation                             playingAnimation;
    private float                                animationTimer;
    private int                                  animationLength;

    public ModelJson(JsonTabulaModel model)
    {
        tabulaModel = model;

        textureWidth = model.getTextureWidth();
        textureHeight = model.getTextureHeight();

        animations = model.getAnimations();

        for (Animation animation : animations)
        {
            animationMap.put(animation.name, animation);
        }
        for (CubeInfo c : model.getCubes())
        {
            cube(c, null, "null");
        }

        for (CubeGroup g : model.getCubeGroups())
        {
            cubeGroup(g);
        }
        // The groups come in in the opposite order from what is needed here, so
        // reverse it
        Collections.reverse(groupIdents);

        setInitPose();
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotation, float rotationYaw,
            float rotationPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, rotation, rotationYaw, rotationPitch, scale, entity);

        double[] scales = tabulaModel.getScale();
        GL11.glScaled(scales[0], scales[1], scales[2]);

        // Render based on the group, this ensures they are correctly rendered.
        for (String s : groupIdents)
        {
            Set<MowzieModelRenderer> cubes = groupMap.get(s);
            for (MowzieModelRenderer cube : cubes)
            {
                if (cube != null)
                {
                    if (texturer != null) texturer.bindObject(entity);
                    cube.setTexturer(texturer);
                    cube.render(0.0625f, entity);
                }
            }
        }
    }

    /** Sets the model's various rotation angles. For bipeds, limbSwing and
     * limbSwingAmount are used for animating the movement of arms and legs,
     * where limbSwing represents the time(so that arms and legs swing back and
     * forth) and limbSwingAmount represents how "far" arms and legs can swing
     * at most.
     *
     * @see net.minecraft.entity.Entity
     * @since 0.1.0 */
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float rotation, float rotationYaw,
            float rotationPitch, float partialTicks, Entity entity)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, rotation, rotationYaw, rotationPitch, partialTicks, entity);

        if (!Minecraft.getMinecraft().isGamePaused())
        {
            this.setToInitPose();

            if (playingAnimation != null || !playing.isEmpty())
            {
                updateAnimation(entity, partialTicks);
            }
        }
    }

    private void cubeGroup(CubeGroup group)
    {
        for (CubeInfo cube : group.cubes)
        {
            cube(cube, null, group.identifier);
        }

        for (CubeGroup c : group.cubeGroups)
        {
            cubeGroup(c);
        }
    }

    private void cube(CubeInfo cube, MowzieModelRenderer parent, String group)
    {
        MowzieModelRenderer modelRenderer = createModelRenderer(cube);

        nameMap.put(cube.name, modelRenderer);
        identifierMap.put(cube.identifier, modelRenderer);

        if (parent != null)
        {
            parent.addChild(modelRenderer);
        }

        // Only add root parts to the group set.
        if (parent == null)
        {
            Set<MowzieModelRenderer> cubes;
            if (groupMap.containsKey(group))
            {
                cubes = groupMap.get(group);
            }
            else
            {
                cubes = Sets.newHashSet();
                groupMap.put(group, cubes);
                groupIdents.add(group);
            }
            cubes.add(modelRenderer);
        }
        for (CubeInfo c : cube.children)
        {
            cube(c, modelRenderer, group);
        }
    }

    /** Starts an animation with the id from the Tabula model.
     *
     * @see net.ilexiconn.llibrary.client.model.tabula.ModelJson
     * @see net.ilexiconn.llibrary.client.model.tabula.Animation
     * @see net.ilexiconn.llibrary.client.model.tabula.AnimationComponent
     * @since 0.1.0 */
    public void startAnimation(int id)
    {
        if (playingAnimation == null)
        {
            playingAnimation = animations.get(id);

            for (Entry<String, ArrayList<AnimationComponent>> entry : playingAnimation.sets.entrySet())
            {
                for (AnimationComponent component : entry.getValue())
                {
                    if (component.startKey + component.length > animationLength)
                    {
                        animationLength = component.startKey + component.length;
                    }
                }
            }
        }
    }

    /** Starts an animation with the id from the Tabula model.
     *
     * @see net.ilexiconn.llibrary.client.model.tabula.ModelJson
     * @see net.ilexiconn.llibrary.client.model.tabula.Animation
     * @see net.ilexiconn.llibrary.client.model.tabula.AnimationComponent
     * @since 0.1.0 */
    public void startAnimation(String id)
    {
        if (playingAnimation != null && !id.equals(playingAnimation.name)) stopAnimation();

        if (playingAnimation == null)
        {
            playingAnimation = animationMap.get(id);
            animationLength = 0;
            for (Entry<String, ArrayList<AnimationComponent>> entry : playingAnimation.sets.entrySet())
            {
                for (AnimationComponent component : entry.getValue())
                {
                    if (component.startKey + component.length > animationLength)
                    {
                        animationLength = component.startKey + component.length;
                    }
                }
            }
        }
    }

    /** Starts an animation with the id from the Tabula model. if this is called
     * when an animation is running, it will stop it, and start the new one.
     *
     * @since 0.1.0 */
    public void startAnimation(Animation animation)
    {
        if (!canRunConcurrent(animation)) stopAnimation();

        if (playingAnimation == null)
        {
            playingAnimation = animation;
            animationLength = 0;

            if (animation.getLength() < 0)
            {
                animation.initLength();
            }
            animationLength = animation.getLength();
        }
    }

    /** Stop all current running animations.
     *
     * @since 0.1.0 */
    public void stopAnimation()
    {
        playingAnimation = null;
    }

    public void stopAnimation(Animation toStop)
    {
        playing.remove(toStop);
    }

    public void updateAnimation(Entity entity, float partialTick)
    {
        float time = entity.worldObj.getTotalWorldTime() + partialTick;
        time = time % animationLength;
        for (Entry<String, ArrayList<AnimationComponent>> entry : playingAnimation.sets.entrySet())
        {
            MowzieModelRenderer animating = identifierMap.get(entry.getKey());

            for (AnimationComponent component : entry.getValue())
            {
                if (time >= component.startKey)
                {
                    float componentTimer = time - component.startKey;

                    if (componentTimer > component.length)
                    {
                        componentTimer = component.length;
                    }
                    //// TODO See that this works.
                    animating.scaleX += (float) (component.scaleChange[0] / component.length * componentTimer);
                    animating.scaleY += (float) (component.scaleChange[1] / component.length * componentTimer);
                    animating.scaleZ += (float) (component.scaleChange[2] / component.length * componentTimer);

                    animating.rotationPointX += component.posChange[0] / component.length * componentTimer;
                    animating.rotationPointY += component.posChange[1] / component.length * componentTimer;
                    animating.rotationPointZ += component.posChange[2] / component.length * componentTimer;

                    animating.rotateAngleX += Math
                            .toRadians(component.rotChange[0] / component.length * componentTimer);
                    animating.rotateAngleY += Math
                            .toRadians(component.rotChange[1] / component.length * componentTimer);
                    animating.rotateAngleZ += Math
                            .toRadians(component.rotChange[2] / component.length * componentTimer);

                    animating.rotationPointX += component.posOffset[0];
                    animating.rotationPointY += component.posOffset[1];
                    animating.rotationPointZ += component.posOffset[2];

                    // Rotate by the Rotation Offset of the animation.
                    animating.rotateAngleX += Math.toRadians(component.rotOffset[0]);
                    animating.rotateAngleY += Math.toRadians(component.rotOffset[1]);
                    animating.rotateAngleZ += Math.toRadians(component.rotOffset[2]);

                }
            }
        }

        animationTimer = (animationTimer + partialTick) % animationLength;

        if (animationTimer > animationLength)
        {
            if (playingAnimation.loops)
            {
                animationTimer = 0;
            }
            else
            {
                stopAnimation();
            }
        }
    }

    private boolean canRunConcurrent(Animation toRun)
    {
        return toRun == playingAnimation;
    }

    private MowzieModelRenderer createModelRenderer(CubeInfo cubeInfo)
    {
        MowzieModelRenderer cube = new MowzieModelRenderer(this, cubeInfo.txOffset[0], cubeInfo.txOffset[1]);
        cube.name = cubeInfo.name;
        cube.identifier = cubeInfo.identifier;
        cube.setRotationPoint((float) cubeInfo.position[0], (float) cubeInfo.position[1], (float) cubeInfo.position[2]);
        // Use cubeInfo.mcScale as the scale, this lets it work properly.
        cube.addBox((float) cubeInfo.offset[0], (float) cubeInfo.offset[1], (float) cubeInfo.offset[2],
                cubeInfo.dimensions[0], cubeInfo.dimensions[1], cubeInfo.dimensions[2], (float) cubeInfo.mcScale);
        cube.rotateAngleX = (float) Math.toRadians((float) cubeInfo.rotation[0]);
        cube.rotateAngleY = (float) Math.toRadians((float) cubeInfo.rotation[1]);
        cube.rotateAngleZ = (float) Math.toRadians((float) cubeInfo.rotation[2]);

        if (Math.abs(cube.rotateAngleX) < 1e-6) cube.rotateAngleX = 0;
        if (Math.abs(cube.rotateAngleY) < 1e-6) cube.rotateAngleY = 0;
        if (Math.abs(cube.rotateAngleZ) < 1e-6) cube.rotateAngleZ = 0;

        // Allows scaling the cube with the cubeinfo scale.
        cube.scaleX = (float) cubeInfo.scale[0];
        cube.scaleY = (float) cubeInfo.scale[1];
        cube.scaleZ = (float) cubeInfo.scale[2];

        // Allows mirrored textures
        cube.mirror = cubeInfo.txMirror;
        // Allows hidden textures
        cube.isHidden = cubeInfo.hidden;
        return cube;
    }

    public MowzieModelRenderer getCube(String name)
    {
        return nameMap.get(name);
    }

    public boolean isAnimationInProgress()
    {
        return playingAnimation != null || !playing.isEmpty();
    }

    public int getAnimationLength()
    {
        return animationLength;
    }

    public float getAnimationTimer()
    {
        return animationTimer;
    }
}

package pokecube.modelloader.common;

/**
 * Interface used for animating the entity<br>
 * If an entity implements this then when<br>
 * being rendered, it will be used to get<br>
 * the phase for animation.
 *
 */
public interface IEntityAnimator
{
    public String getAnimation(float partialTick);
}

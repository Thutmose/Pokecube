package pokecube.core.client.render.particle;

public interface IParticle
{
    int getDuration();
    void kill();
    long lastTick();
    void render(double renderPartialTicks);
    void setDuration(int duration);
    void setLastTick(long tick);
    void setLifetime(int ticks);
    void setColour(int colour);
    void setSize(float size);
}

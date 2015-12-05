package pokecube.core.client.render.particle;

public interface IParticle
{
    int getDuration();
    void setDuration(int duration);
    void setLifetime(int ticks);
    long lastTick();
    void setLastTick(long tick);
    void render(double renderPartialTicks);
    void kill();
}

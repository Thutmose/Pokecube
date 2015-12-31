package pokecube.modelloader.client.custom;

public interface IPartTexturer
{
    void applyTexture(String part);

    void bindObject(Object thing);

    boolean shiftUVs(String part, double[] toFill);
}

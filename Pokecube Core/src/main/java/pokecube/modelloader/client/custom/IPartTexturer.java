package pokecube.modelloader.client.custom;

public interface IPartTexturer
{
    void applyTexture(String part);

    void bindObject(Object thing);

    void shiftUVs(String part, double[] toFill);
}
